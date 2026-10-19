package smartstay.ui;

import smartstay.exception.HotelException;
import smartstay.model.Guest;
import smartstay.service.GuestManager;
import smartstay.util.Validator;

import java.util.List;

/**
 * Admin > Guest Management: add, remove, update and view guests.
 */
public class AdminGuestScreen {

    private final ConsoleIO io;
    private final SharedForms forms;
    private final GuestManager guestManager;

    public AdminGuestScreen(ConsoleIO io, SharedForms forms,
                                     GuestManager guestManager) {
        this.io = io;
        this.forms = forms;
        this.guestManager = guestManager;
    }

    public void show() {
        Menu menu = new Menu(io, "Admin > Guest Management", List.of(
                "Add Guest", "Remove Guest", "Update Guest", "View Guests", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            switch (choice) {
                case 0 -> io.runAction(forms::registerGuestForm);
                case 1 -> io.runAction(this::removeGuest);
                case 2 -> io.runAction(this::updateGuest);
                case 3 -> io.runAction(this::viewGuests);
                default -> io.printError("Unknown option.");
            }
        }
    }

    private void removeGuest() throws HotelException {
        Display.printGuests(guestManager.getAllGuests());
        Guest guest = forms.findGuestForm();
        if (io.confirm("  Remove guest " + guest.getFullName() + " (" + guest.getNic() + ")?")) {
            guestManager.removeGuest(guest.getNic());
            io.printSuccess("Guest " + guest.getFullName() + " removed.");
        } else {
            io.printInfo("Guest was not removed.");
        }
    }

    /** NIC cannot change (it is the key). Press ENTER to keep any other value. */
    private void updateGuest() throws HotelException {
        Display.printGuests(guestManager.getAllGuests());
        Guest guest = forms.findGuestForm();
        io.printInfo("Press ENTER to keep the value shown in [ ].");
        String name = io.readValidOrKeep("  Full name", guest.getFullName(), Validator::validateName);
        String phone = io.readValidOrKeep("  Phone", guest.getPhone(), Validator::validatePhone);
        String email = io.readValidOrKeep("  Email", guest.getEmail(), Validator::validateEmail);
        guestManager.updateGuest(guest.getNic(), name, phone, email);
        io.printSuccess("Guest " + guest.getNic() + " updated.");
    }

    private void viewGuests() {
        Display.printGuests(guestManager.getAllGuests());
    }
}
