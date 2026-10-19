package smartstay.ui;

import java.util.List;

/**
 * The first menu the user sees: Admin, Guest, Exit.
 */
public class MainScreen {

    private final ConsoleIO io;
    private final AdminScreen adminScreen;
    private final GuestScreen guestScreen;

    public MainScreen(ConsoleIO io, AdminScreen adminScreen,
                               GuestScreen guestScreen) {
        this.io = io;
        this.adminScreen = adminScreen;
        this.guestScreen = guestScreen;
    }

    public void show() {
        printWelcome();
        Menu menu = new Menu(io, "SmartStay - Main Menu", List.of("Admin", "Guest", "Exit"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                io.printInfo("Thank you for using SmartStay. Goodbye!");
                return;
            }
            if (choice == 0) {
                adminScreen.show();
            } else {
                guestScreen.show();
            }
        }
    }

    private void printWelcome() {
        System.out.println();
        Display.printBox("SMARTSTAY HOTEL BOOKING SYSTEM");
        if (io.isArrowMode()) {
            io.printInfo("Arrow key menus are on.");
        } else {
            io.printInfo("Numbered menus are on (this terminal does not support arrow keys).");
        }
        io.printInfo("Tip: in any form, type " + ConsoleIO.GO_BACK_KEY + " to go back.");
    }
}
