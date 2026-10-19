package smartstay;

import smartstay.exception.HotelException;
import smartstay.service.BillingManager;
import smartstay.service.BookingManager;
import smartstay.service.GuestManager;
import smartstay.service.RoomManager;
import smartstay.service.SampleDataLoader;
import smartstay.ui.AdminBookingScreen;
import smartstay.ui.AdminGuestScreen;
import smartstay.ui.AdminInvoiceScreen;
import smartstay.ui.AdminRoomScreen;
import smartstay.ui.AdminScreen;
import smartstay.ui.ConsoleIO;
import smartstay.ui.GuestScreen;
import smartstay.ui.MainScreen;
import smartstay.ui.SharedForms;

/**
 * Starting point of SmartStay.
 *
 * main() creates every object once and connects them ("wiring"):
 * the managers first (business logic), then the screens (UI) that use them.
 * Passing objects in through constructors keeps classes loosely joined and
 * makes it easy to see in one place what depends on what.
 *
 * Run with --numbered to force numbered menus even in a terminal that
 * supports arrow keys.
 */
public class SmartStayApp {

    public static void main(String[] args) {
        boolean forceNumbered = args.length > 0 && args[0].equalsIgnoreCase("--numbered");

        // ----- business layer -----
        BookingManager bookingManager = new BookingManager();
        RoomManager roomManager = new RoomManager(bookingManager);
        GuestManager guestManager = new GuestManager(bookingManager);
        BillingManager billingManager = new BillingManager(bookingManager);

        try {
            SampleDataLoader.load(roomManager, guestManager, bookingManager);
        } catch (HotelException e) {
            // should never happen with our fixed sample data, but say so if it does
            System.out.println("Could not load sample data: " + e.getMessage());
        }

        // ----- UI layer -----
        ConsoleIO io = new ConsoleIO(forceNumbered);
        SharedForms forms = new SharedForms(io, roomManager, guestManager, bookingManager);

        AdminScreen adminScreen = new AdminScreen(io,
                new AdminRoomScreen(io, forms, roomManager),
                new AdminGuestScreen(io, forms, guestManager),
                new AdminBookingScreen(io, forms, bookingManager),
                new AdminInvoiceScreen(io, bookingManager, billingManager));
        GuestScreen guestScreen =
                new GuestScreen(io, forms, guestManager, bookingManager, billingManager);

        new MainScreen(io, adminScreen, guestScreen).show();
        System.exit(0); // also closes the JLine terminal cleanly
    }
}
