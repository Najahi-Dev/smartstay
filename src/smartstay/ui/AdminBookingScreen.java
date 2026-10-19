package smartstay.ui;

import smartstay.exception.HotelException;
import smartstay.model.Guest;
import smartstay.service.BookingManager;

import java.util.List;

/**
 * Admin > Booking Management: create a booking for a registered guest and
 * view every booking sorted by check-in date.
 */
public class AdminBookingScreen {

    private final ConsoleIO io;
    private final SharedForms forms;
    private final BookingManager bookingManager;

    public AdminBookingScreen(ConsoleIO io, SharedForms forms,
                                       BookingManager bookingManager) {
        this.io = io;
        this.forms = forms;
        this.bookingManager = bookingManager;
    }

    public void show() {
        Menu menu = new Menu(io, "Admin > Booking Management", List.of(
                "Create Booking", "View Bookings", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            switch (choice) {
                case 0 -> io.runAction(this::createBooking);
                case 1 -> io.runAction(this::viewBookings);
                default -> io.printError("Unknown option.");
            }
        }
    }

    private void createBooking() throws HotelException {
        io.printInfo("Type " + ConsoleIO.GO_BACK_KEY + " at any question to go back.");
        Guest guest = forms.findGuestForm();
        forms.bookingForm(guest);
    }

    private void viewBookings() {
        io.printInfo("All bookings, sorted by check-in date:");
        Display.printBookings(bookingManager.getAllBookingsSorted());
    }
}
