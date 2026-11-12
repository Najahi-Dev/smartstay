package smartstay.ui;

import smartstay.exception.GoBackException;
import smartstay.exception.HotelException;
import smartstay.exception.ValidationException;
import smartstay.model.Booking;
import smartstay.model.BookingStatus;
import smartstay.model.Guest;
import smartstay.model.Invoice;
import smartstay.service.BillingManager;
import smartstay.service.BookingManager;
import smartstay.service.GuestManager;
import smartstay.util.MoneyFormatter;
import smartstay.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Everything a guest can do: register / log in, then the guest menu
 * (search rooms, reserve, search/view/modify/cancel reservations, print invoice).
 * The logged in guest is kept in a field while the guest menu is open.
 */
public class GuestScreen {

    private final ConsoleIO io;
    private final SharedForms forms;
    private final GuestManager guestManager;
    private final BookingManager bookingManager;
    private final BillingManager billingManager;

    private Guest currentGuest; // null when nobody is logged in

    public GuestScreen(ConsoleIO io, SharedForms forms,
                                GuestManager guestManager,
                                BookingManager bookingManager,
                                BillingManager billingManager) {
        this.io = io;
        this.forms = forms;
        this.guestManager = guestManager;
        this.bookingManager = bookingManager;
        this.billingManager = billingManager;
    }

    /** Guest entry: Register, Login, Back. */
    public void show() {
        Menu menu = new Menu(io, "Guest", List.of("Register", "Login", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            if (choice == 0) {
                io.runAction(() -> currentGuest = forms.registerGuestForm());
            } else {
                io.runAction(this::login);
            }
            // after a successful register or login, open the guest menu straight away
            if (currentGuest != null) {
                showGuestMenu();
                currentGuest = null; // logged out
            }
        }
    }

    /** Guest login is just the NIC/Passport number (no password, see ASSUMPTIONS.md). */
    private void login() throws HotelException {
        String nic = io.readValid("  NIC/Passport (" + ConsoleIO.GO_BACK_KEY + " to go back): ",
                Validator::validateNic);
        currentGuest = guestManager.findGuest(nic);
        io.printSuccess("Welcome, " + currentGuest.getFullName() + ".");
    }

    private void showGuestMenu() {
        Menu menu = new Menu(io, "Guest Menu - " + currentGuest.getFullName(), List.of(
                "Search Room", "Make Reservation", "Search Reservation", "View by Check-in Date",
                "Modify Reservation", "Cancel Reservation", "Print Invoice", "Back (log out)"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                io.printInfo("Logged out. Goodbye, " + currentGuest.getFullName() + ".");
                return;
            }
            switch (choice) {
                case 0 -> io.runAction(forms::searchRoomsForm);
                case 1 -> io.runAction(() -> forms.bookingForm(currentGuest));
                case 2 -> io.runAction(this::searchReservation);
                case 3 -> io.runAction(this::viewByCheckInDate);
                case 4 -> io.runAction(this::modifyReservation);
                case 5 -> io.runAction(this::cancelReservation);
                case 6 -> io.runAction(this::printInvoice);
                default -> io.printError("Unknown option.");
            }
        }
    }

    /** Search by guest name (partial, any case) or by exact booking ID. */
    private void searchReservation() throws HotelException {
        Menu menu = new Menu(io, "Search Reservation",
                List.of("By guest name", "By booking ID", "Back"));
        int choice = menu.show();
        if (menu.isLast(choice)) {
            throw new GoBackException();
        }
        if (choice == 0) {
            String name = io.readValid("  Guest name (or part of it): ", this::checkSearchText);
            Display.printBookings(bookingManager.searchByGuestName(name));
        } else {
            String bookingId = io.readText("  Booking ID (e.g. BK-0001): ");
            Display.printBookings(List.of(bookingManager.findBooking(bookingId)));
        }
    }

    private void viewByCheckInDate() {
        io.printInfo("Your reservations, sorted by check-in date:");
        Display.printBookings(bookingManager.getBookingsForGuest(currentGuest.getNic()));
    }

    /** Change the room, dates or guest count of one of the guest's own active bookings. */
    private void modifyReservation() throws HotelException {
        Booking booking = chooseOwnBooking("Modify which reservation?", true);
        bookingManager.checkCanModify(booking); // check the 24 hour rule before asking anything
        forms.changeBookingForm(booking);
    }

    private void cancelReservation() throws HotelException {
        Booking booking = chooseOwnBooking("Cancel which reservation?", true);
        bookingManager.checkCanCancel(booking); // check the 24 hour rule before asking Y/N
        if (!io.confirm("  Cancel " + booking.getBookingId() + "?")) {
            io.printInfo("Reservation kept.");
            return;
        }
        bookingManager.cancelBooking(booking.getBookingId());
        io.printSuccess("Reservation " + booking.getBookingId() + " cancelled. Your advance of "
                + MoneyFormatter.format(Booking.ADVANCE_AMOUNT) + " will be refunded.");
    }

    /**
     * Shows the real invoice if the admin already made one, otherwise an
     * estimate using the same calculation (nothing is saved).
     */
    private void printInvoice() throws HotelException {
        Booking booking = chooseOwnBooking("Print invoice for which reservation?", false);
        Optional<Invoice> invoice = billingManager.findInvoiceForBooking(booking.getBookingId());
        Display.printInvoice(invoice.orElseGet(() -> billingManager.estimateInvoice(booking)));
    }

    /**
     * Lets the guest pick one of their own bookings from a menu.
     * activeOnly = true shows only Confirmed bookings (for cancelling);
     * otherwise every booking except Cancelled ones (for invoices).
     */
    private Booking chooseOwnBooking(String title, boolean activeOnly)
            throws HotelException {
        List<Booking> choices = new ArrayList<>();
        for (Booking booking : bookingManager.getBookingsForGuest(currentGuest.getNic())) {
            boolean cancelled = booking.getStatus() == BookingStatus.CANCELLED;
            boolean wanted = activeOnly ? booking.isActive() : !cancelled;
            if (wanted) {
                choices.add(booking);
            }
        }
        if (choices.isEmpty()) {
            throw new HotelException("You have no reservations for this option.");
        }
        List<String> options = new ArrayList<>();
        for (Booking booking : choices) {
            options.add(Display.describeBooking(booking));
        }
        options.add("Back");
        Menu menu = new Menu(io, title, options);
        int choice = menu.show();
        if (menu.isLast(choice)) {
            throw new GoBackException();
        }
        return choices.get(choice);
    }

    private String checkSearchText(String raw) throws ValidationException {
        if (raw.isBlank()) {
            throw new ValidationException("Please type at least one letter of the name.");
        }
        return raw;
    }
}
