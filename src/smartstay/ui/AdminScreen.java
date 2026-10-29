package smartstay.ui;

import smartstay.exception.GoBackException;

import java.util.List;

/**
 * Admin login and the admin main menu. Each menu option opens its own
 * screen class, so no single class gets too long.
 */
public class AdminScreen {

    // Hardcoded on purpose (see ASSUMPTIONS.md). A real system would store a
    // hashed password, but this coursework has no database or file storage.
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private final ConsoleIO io;
    private final AdminRoomScreen roomScreen;
    private final AdminGuestScreen guestScreen;
    private final AdminBookingScreen bookingScreen;
    private final AdminInvoiceScreen invoiceScreen;

    public AdminScreen(ConsoleIO io, AdminRoomScreen roomScreen,
                                AdminGuestScreen guestScreen,
                                AdminBookingScreen bookingScreen,
                                AdminInvoiceScreen invoiceScreen) {
        this.io = io;
        this.roomScreen = roomScreen;
        this.guestScreen = guestScreen;
        this.bookingScreen = bookingScreen;
        this.invoiceScreen = invoiceScreen;
    }

    public void show() {
        if (!login()) {
            return;
        }
        Menu menu = new Menu(io, "Admin Menu", List.of(
                "Room Management", "Guest Management", "Booking Management",
                "Invoice Management", "Back (log out)"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                io.printInfo("Admin logged out.");
                return;
            }
            switch (choice) {
                case 0 -> roomScreen.show();
                case 1 -> guestScreen.show();
                case 2 -> bookingScreen.show();
                case 3 -> invoiceScreen.show();
                default -> io.printError("Unknown option.");
            }
        }
    }

    /** Gives the admin 3 tries, then goes back to the main menu. # goes back at once. */
    private boolean login() {
        System.out.println();
        Display.printBox("Admin Login");
        io.printInfo("Type " + ConsoleIO.GO_BACK_KEY + " to go back.");
        try {
            return tryLogin();
        } catch (GoBackException e) {
            io.printInfo("Login cancelled.");
            return false;
        }
    }

    private boolean tryLogin() {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            String username = io.readText("  Username: ");
            String password = io.readPassword("  Password: ");
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                io.printSuccess("Welcome, admin.");
                return true;
            }
            int left = MAX_LOGIN_ATTEMPTS - attempt;
            io.printError("Wrong username or password. " + left + " attempt(s) left.");
        }
        io.printError("Too many failed attempts. Returning to the main menu.");
        return false;
    }
}
