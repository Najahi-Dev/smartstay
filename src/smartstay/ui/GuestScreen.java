package smartstay.ui;

import smartstay.exception.GoBackException;
import smartstay.exception.HotelException;
import smartstay.exception.ValidationException;
import smartstay.model.Booking;
import smartstay.model.Guest;
import smartstay.model.Invoice;
import smartstay.service.BillingManager;
import smartstay.service.BookingManager;
import smartstay.service.GuestManager;
import smartstay.util.MoneyFormatter;
import smartstay.util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything a guest can do: register / log in, then the guest menu, which
 * is split into categories: Rooms (search), Reservations (make, search,
 * view, modify, cancel) and Invoices (view printed invoices).
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

    /** Top level guest menu: one entry per category, each opens its own sub menu. */
    private void showGuestMenu() {
        Menu menu = new Menu(io, "Guest Menu - " + currentGuest.getFullName(), List.of(
                "Rooms", "Reservations", "Invoices", "Back (log out)"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                io.printInfo("Logged out. Goodbye, " + currentGuest.getFullName() + ".");
                return;
            }
            switch (choice) {
                case 0 -> showRoomsMenu();
                case 1 -> showReservationsMenu();
                case 2 -> showInvoicesMenu();
                default -> io.printError("Unknown option.");
            }
        }
    }

    private void showRoomsMenu() {
        Menu menu = new Menu(io, "Guest > Rooms", List.of("Search Room", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            io.runAction(forms::searchRoomsForm);
        }
    }

    private void showReservationsMenu() {
        Menu menu = new Menu(io, "Guest > Reservations", List.of(
                "Make Reservation", "Search Reservation", "View by Check-in Date",
                "Modify Reservation", "Cancel Reservation", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            switch (choice) {
                case 0 -> io.runAction(() -> forms.bookingForm(currentGuest));
                case 1 -> io.runAction(this::searchReservation);
                case 2 -> io.runAction(this::viewByCheckInDate);
                case 3 -> io.runAction(this::modifyReservation);
                case 4 -> io.runAction(this::cancelReservation);
                default -> io.printError("Unknown option.");
            }
        }
    }

    private void showInvoicesMenu() {
        Menu menu = new Menu(io, "Guest > Invoices", List.of("View Printed Invoice", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            io.runAction(this::viewPrintedInvoice);
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
        Booking booking = chooseOwnBooking("Modify which reservation?");
        bookingManager.checkCanModify(booking); // check the 24 hour rule before asking anything
        forms.changeBookingForm(booking);
    }

    private void cancelReservation() throws HotelException {
        Booking booking = chooseOwnBooking("Cancel which reservation?");
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
     * Shows an invoice the admin has already printed for one of the guest's
     * bookings. Invoices are made at check-out, so a booking that is still
     * Confirmed has no invoice yet.
     */
    private void viewPrintedInvoice() throws HotelException {
        List<Invoice> invoices = billingManager.getInvoicesForGuest(currentGuest.getNic());
        if (invoices.isEmpty()) {
            throw new HotelException("You have no printed invoices yet. "
                    + "The hotel prints the invoice at check-out.");
        }
        List<String> options = new ArrayList<>();
        for (Invoice invoice : invoices) {
            options.add(invoice.getInvoiceId() + "  " + invoice.getIssuedOn() + "  "
                    + Display.describeBooking(invoice.getBooking()));
        }
        options.add("Back");
        Menu menu = new Menu(io, "View which invoice?", options);
        int choice = menu.show();
        if (menu.isLast(choice)) {
            throw new GoBackException();
        }
        Display.printInvoice(invoices.get(choice));
    }

    /**
     * Lets the guest pick one of their own active bookings from a menu.
     * Only Confirmed bookings are shown, because only they can be
     * changed or cancelled.
     */
    private Booking chooseOwnBooking(String title) throws HotelException {
        List<Booking> choices = new ArrayList<>();
        for (Booking booking : bookingManager.getBookingsForGuest(currentGuest.getNic())) {
            if (booking.isActive()) {
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
