package smartstay.ui;

import smartstay.exception.HotelException;
import smartstay.model.Booking;
import smartstay.model.Invoice;
import smartstay.service.BillingManager;
import smartstay.service.BookingManager;

import java.util.List;

/**
 * Admin > Invoice Management: create the final invoice from a booking ID
 * (this closes the booking) and view all invoices made so far.
 */
public class AdminInvoiceScreen {

    private final ConsoleIO io;
    private final BookingManager bookingManager;
    private final BillingManager billingManager;

    public AdminInvoiceScreen(ConsoleIO io, BookingManager bookingManager,
                                       BillingManager billingManager) {
        this.io = io;
        this.bookingManager = bookingManager;
        this.billingManager = billingManager;
    }

    public void show() {
        Menu menu = new Menu(io, "Admin > Invoice Management", List.of(
                "Create Invoice", "View Invoice History", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            switch (choice) {
                case 0 -> io.runAction(this::createInvoice);
                case 1 -> io.runAction(this::viewHistory);
                default -> io.printError("Unknown option.");
            }
        }
    }

    private void createInvoice() throws HotelException {
        Display.printBookings(bookingManager.getAllBookingsSorted());
        String bookingId = io.readText("  Booking ID (e.g. BK-0001, " + ConsoleIO.GO_BACK_KEY
                + " to go back): ");
        Booking booking = bookingManager.findBooking(bookingId);
        io.printInfo(Display.describeBooking(booking));
        if (!io.confirm("  Create the final invoice? The booking will be marked Completed.")) {
            io.printInfo("Invoice not created.");
            return;
        }
        Invoice invoice = billingManager.createInvoice(booking.getBookingId());
        Display.printInvoice(invoice);
        io.printSuccess("Invoice " + invoice.getInvoiceId() + " created.");
    }

    private void viewHistory() {
        Display.printInvoiceHistory(billingManager.getInvoiceHistory());
    }
}
