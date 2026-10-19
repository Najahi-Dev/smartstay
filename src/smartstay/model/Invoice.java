package smartstay.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The final bill for a booking.
 *
 * All amounts are worked out by the BillingManager and passed in once, so an
 * invoice is read-only after it is made (no setters). This matches real life:
 * you do not edit an invoice, you issue a new one.
 * BigDecimal is used for money because double cannot store values like 0.1
 * exactly and small rounding errors would show up in the totals.
 */
public class Invoice {

    private final String invoiceId;
    private final Booking booking;
    private final LocalDate issuedOn;
    private final int extraGuests;
    private final BigDecimal baseCharge;
    private final BigDecimal extraGuestCharge;
    private final BigDecimal discount;
    private final BigDecimal tax;
    private final BigDecimal totalAmount;      // base + extra - discount + tax
    private final BigDecimal advanceDeducted;
    private final BigDecimal finalPayable;     // total - advance

    public Invoice(String invoiceId, Booking booking, LocalDate issuedOn,
                            int extraGuests, BigDecimal baseCharge, BigDecimal extraGuestCharge,
                            BigDecimal discount, BigDecimal tax, BigDecimal totalAmount,
                            BigDecimal advanceDeducted, BigDecimal finalPayable) {
        this.invoiceId = invoiceId;
        this.booking = booking;
        this.issuedOn = issuedOn;
        this.extraGuests = extraGuests;
        this.baseCharge = baseCharge;
        this.extraGuestCharge = extraGuestCharge;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.advanceDeducted = advanceDeducted;
        this.finalPayable = finalPayable;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Booking getBooking() {
        return booking;
    }

    public LocalDate getIssuedOn() {
        return issuedOn;
    }

    public int getExtraGuests() {
        return extraGuests;
    }

    public BigDecimal getBaseCharge() {
        return baseCharge;
    }

    public BigDecimal getExtraGuestCharge() {
        return extraGuestCharge;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAdvanceDeducted() {
        return advanceDeducted;
    }

    public BigDecimal getFinalPayable() {
        return finalPayable;
    }
}
