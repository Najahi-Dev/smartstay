package smartstay.service;

import smartstay.exception.BusinessRuleException;
import smartstay.exception.NotFoundException;
import smartstay.model.Booking;
import smartstay.model.Invoice;
import smartstay.model.Room;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Works out bills and keeps the invoice history.
 *
 * The bill always follows the same five steps (see calculateInvoice).
 * Creating an invoice also marks the booking as Completed. Guests can view
 * the invoices that were made for their own bookings.
 */
public class BillingManager {

    public static final int DISCOUNT_MIN_NIGHTS = 5;
    public static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.12");

    private final List<Invoice> invoices = new ArrayList<>(); // kept in issue order
    private final BookingManager bookingManager;
    private int lastInvoiceNumber = 0;

    public BillingManager(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    /**
     * Admin creates the final invoice for a booking (at check-out).
     * Only a Confirmed booking can be invoiced, and only once.
     */
    public Invoice createInvoice(String bookingId)
            throws NotFoundException, BusinessRuleException {
        Booking booking = bookingManager.findBooking(bookingId);
        Optional<Invoice> existing = findInvoiceForBooking(booking.getBookingId());
        if (existing.isPresent()) {
            throw new BusinessRuleException("Invoice " + existing.get().getInvoiceId()
                    + " already exists for booking " + booking.getBookingId() + ".");
        }
        if (!booking.isActive()) {
            throw new BusinessRuleException("Booking " + booking.getBookingId() + " is "
                    + booking.getStatus() + ", so no invoice can be created.");
        }
        Invoice invoice = calculateInvoice(booking, nextInvoiceId());
        invoices.add(invoice);
        bookingManager.completeBooking(booking);
        return invoice;
    }

    /** Invoices already made for one guest's bookings, in issue order. */
    public List<Invoice> getInvoicesForGuest(String nic) {
        List<Invoice> list = new ArrayList<>();
        for (Invoice invoice : invoices) {
            if (invoice.getBooking().getGuest().getNic().equals(nic)) {
                list.add(invoice);
            }
        }
        return list;
    }

    public Optional<Invoice> findInvoiceForBooking(String bookingId) {
        for (Invoice invoice : invoices) {
            if (invoice.getBooking().getBookingId().equals(bookingId)) {
                return Optional.of(invoice);
            }
        }
        return Optional.empty();
    }

    /** A copy of the list, so other classes cannot add or delete invoices directly. */
    public List<Invoice> getInvoiceHistory() {
        return new ArrayList<>(invoices);
    }

    /**
     * The five step bill calculation (order matters):
     *   1. base   = daily rate x nights
     *   2. extra  = extra guests x extra guest charge x nights
     *              (extra guests = guests above the room capacity, never below 0)
     *   3. discount = 12% of the BASE charge only, if the stay is 5+ nights
     *   4. tax    = tax rate x (base + extra - discount)
     *   5. final  = (base + extra - discount + tax) - LKR 10,000 advance
     * Each amount is rounded to 2 decimal places (cents) as soon as it is
     * worked out, the same way a printed bill would show it.
     */
    private Invoice calculateInvoice(Booking booking, String invoiceId) {
        Room room = booking.getRoom(); // the subclass gives the right rates
        BigDecimal nights = BigDecimal.valueOf(booking.getNights());

        // Step 1
        BigDecimal base = money(room.getDailyRate().multiply(nights));

        // Step 2
        int extraGuests = Math.max(0, booking.getNumberOfGuests() - room.getCapacity());
        BigDecimal extra = money(room.getExtraGuestCharge()
                .multiply(BigDecimal.valueOf(extraGuests))
                .multiply(nights));

        // Step 3
        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        if (booking.getNights() >= DISCOUNT_MIN_NIGHTS) {
            discount = money(base.multiply(DISCOUNT_RATE));
        }

        // Step 4
        BigDecimal taxable = base.add(extra).subtract(discount);
        BigDecimal tax = money(taxable.multiply(room.getTaxRate()));
        BigDecimal total = taxable.add(tax);

        // Step 5
        BigDecimal advance = Booking.ADVANCE_AMOUNT;
        BigDecimal finalPayable = total.subtract(advance);

        return new Invoice(invoiceId, booking, LocalDate.now(), extraGuests,
                base, extra, discount, tax, total, advance, finalPayable);
    }

    /** Rounds to 2 decimal places, 0.005 goes up (normal money rounding). */
    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** INV-0001, INV-0002 ... */
    private String nextInvoiceId() {
        lastInvoiceNumber++;
        return String.format("INV-%04d", lastInvoiceNumber);
    }
}
