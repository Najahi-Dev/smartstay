package smartstay.ui;

import smartstay.exception.BusinessRuleException;
import smartstay.exception.GoBackException;
import smartstay.exception.HotelException;
import smartstay.exception.ValidationException;
import smartstay.model.BedType;
import smartstay.model.Booking;
import smartstay.model.Guest;
import smartstay.model.Room;
import smartstay.model.RoomType;
import smartstay.service.BookingManager;
import smartstay.service.GuestManager;
import smartstay.service.RoomManager;
import smartstay.util.MoneyFormatter;
import smartstay.util.Validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Forms that both the admin screens and the guest screens need:
 * register a guest, search rooms, make or change a booking, and pick a room/bed type.
 * Writing them once here avoids copying the same code into two screens.
 */
public class SharedForms {

    private final ConsoleIO io;
    private final RoomManager roomManager;
    private final GuestManager guestManager;
    private final BookingManager bookingManager;

    public SharedForms(ConsoleIO io, RoomManager roomManager,
                                GuestManager guestManager,
                                BookingManager bookingManager) {
        this.io = io;
        this.roomManager = roomManager;
        this.guestManager = guestManager;
        this.bookingManager = bookingManager;
    }

    // ---------------- guests ----------------

    /** Asks for all guest details, one at a time, and registers the guest. */
    public Guest registerGuestForm() throws BusinessRuleException {
        io.printInfo("Enter the guest details (type " + ConsoleIO.GO_BACK_KEY + " to go back).");
        String nic = io.readValid("  NIC/Passport : ", this::checkNewNic);
        String name = io.readValid("  Full name    : ", Validator::validateName);
        String phone = io.readValid("  Phone        : ", Validator::validatePhone);
        String email = io.readValid("  Email        : ", Validator::validateEmail);
        Guest guest = guestManager.registerGuest(nic, name, phone, email);
        io.printSuccess("Guest " + name + " registered with NIC/Passport " + nic + ".");
        return guest;
    }

    /** Asks for a NIC and returns the matching registered guest. */
    public Guest findGuestForm() throws HotelException {
        String nic = io.readValid("  Guest NIC/Passport: ", Validator::validateNic);
        return guestManager.findGuest(nic);
    }

    // ---------------- rooms ----------------

    /** Search available rooms by type and date, and print them. */
    public void searchRoomsForm() {
        RoomType type = chooseRoomType("Search rooms - choose a room type");
        io.printInfo("Enter the dates as yyyy-MM-dd (type " + ConsoleIO.GO_BACK_KEY + " to go back).");
        LocalDate checkIn = readCheckIn();
        LocalDate checkOut = readCheckOut(checkIn);
        List<Room> rooms = roomManager.findAvailableRooms(type, checkIn, checkOut);
        if (rooms.isEmpty()) {
            io.printError("No " + type + " rooms are available from " + checkIn + " to " + checkOut + ".");
            return;
        }
        io.printInfo("Available " + type + " rooms from " + checkIn + " to " + checkOut + ":");
        Display.printRooms(rooms);
    }

    public RoomType chooseRoomType(String title) {
        return chooseFromList(title, List.of(RoomType.values()));
    }

    public BedType chooseBedType(String title) {
        return chooseFromList(title, List.of(BedType.values()));
    }

    // ---------------- bookings ----------------

    /**
     * The booking steps:
     * room type -> dates -> pick one of the free rooms -> number of guests
     * -> summary -> Y/N -> booking is saved and the advance is charged.
     */
    public void bookingForm(Guest guest) throws HotelException {
        RoomType type = chooseRoomType("New booking for " + guest.getFullName()
                + " - choose a room type");
        io.printInfo("Enter the dates as yyyy-MM-dd (type " + ConsoleIO.GO_BACK_KEY + " to go back).");
        LocalDate checkIn = readCheckIn();
        LocalDate checkOut = readCheckOut(checkIn);

        List<Room> rooms = roomManager.findAvailableRooms(type, checkIn, checkOut);
        if (rooms.isEmpty()) {
            throw new BusinessRuleException("No " + type + " rooms are available from "
                    + checkIn + " to " + checkOut + ". Try other dates or another room type.");
        }
        Room room = chooseRoom(rooms);
        int numberOfGuests = readGuestCount(room);

        printBookingSummary(guest, room, numberOfGuests, checkIn, checkOut,
                "(paid now, refundable)");
        if (!io.confirm("  Confirm this booking and pay the advance?")) {
            io.printInfo("Booking not made.");
            return;
        }
        Booking booking =
                bookingManager.createBooking(guest, room, numberOfGuests, checkIn, checkOut);
        io.printSuccess("Booking " + booking.getBookingId() + " confirmed. Advance of "
                + MoneyFormatter.format(Booking.ADVANCE_AMOUNT) + " charged.");
        io.printInfo("The advance is refunded if you cancel more than 24 hours before check-in.");
    }

    /**
     * The change steps: same as a new booking (room type -> dates -> room
     * -> guests -> summary -> Y/N), but the booking keeps its ID and advance.
     * The 24 hour rule is checked by the caller before this form opens, and
     * again by BookingManager when the change is saved.
     */
    public void changeBookingForm(Booking booking) throws HotelException {
        io.printInfo("Current booking: " + Display.describeBooking(booking));
        io.printInfo("Enter the new details (type " + ConsoleIO.GO_BACK_KEY + " to go back).");
        RoomType type = chooseRoomType("Change " + booking.getBookingId()
                + " - choose a room type");
        io.printInfo("Enter the new dates as yyyy-MM-dd.");
        LocalDate checkIn = readCheckIn();
        LocalDate checkOut = readCheckOut(checkIn);

        List<Room> rooms = roomManager.findRoomsForChange(type, checkIn, checkOut, booking);
        if (rooms.isEmpty()) {
            throw new BusinessRuleException("No " + type + " rooms are available from "
                    + checkIn + " to " + checkOut + ". Try other dates or another room type.");
        }
        Room room = chooseRoom(rooms);
        int numberOfGuests = readGuestCount(room);

        printBookingSummary(booking.getGuest(), room, numberOfGuests, checkIn, checkOut,
                "(already paid, stays on this booking)");
        if (!io.confirm("  Save these changes to " + booking.getBookingId() + "?")) {
            io.printInfo("Booking not changed.");
            return;
        }
        bookingManager.modifyBooking(booking.getBookingId(), room, numberOfGuests,
                checkIn, checkOut);
        io.printSuccess("Booking " + booking.getBookingId() + " changed: "
                + Display.describeBooking(booking));
    }

    // ---------------- private helpers ----------------

    /** Check-in date, re-asked until it is at least 2 days from today. */
    private LocalDate readCheckIn() {
        while (true) {
            LocalDate checkIn = io.readDate("  Check-in date  : ");
            try {
                Validator.checkCheckInDate(checkIn, LocalDate.now());
                return checkIn;
            } catch (ValidationException e) {
                io.printError(e.getMessage());
            }
        }
    }

    /** Check-out date, re-asked until it is after check-in. */
    private LocalDate readCheckOut(LocalDate checkIn) {
        while (true) {
            LocalDate checkOut = io.readDate("  Check-out date : ");
            if (checkOut.isAfter(checkIn)) {
                return checkOut;
            }
            io.printError("Check-out must be after check-in (" + checkIn + ").");
        }
    }

    private Room chooseRoom(List<Room> rooms) {
        List<String> options = new ArrayList<>();
        for (Room room : rooms) {
            options.add(Display.describeRoom(room));
        }
        options.add("Back");
        Menu menu = new Menu(io, "Choose a room", options);
        int choice = menu.show();
        if (menu.isLast(choice)) {
            throw new GoBackException();
        }
        return rooms.get(choice);
    }

    private int readGuestCount(Room room) {
        int maxGuests = room.getCapacity() + BookingManager.MAX_EXTRA_GUESTS;
        io.printInfo("Room " + room.getRoomId() + " covers " + room.getCapacity()
                + " guests. Each extra guest costs "
                + MoneyFormatter.format(room.getExtraGuestCharge()) + " per night.");
        return io.readNumber("  Number of guests (1-" + maxGuests + "): ", 1, maxGuests);
    }

    private void printBookingSummary(Guest guest, Room room, int numberOfGuests,
                                     LocalDate checkIn, LocalDate checkOut, String advanceNote) {
        System.out.println();
        Display.printBox("Booking summary");
        io.printInfo("Guest     : " + guest.getFullName() + " (" + guest.getNic() + ")");
        io.printInfo("Room      : " + Display.describeRoom(room));
        io.printInfo("Dates     : " + checkIn + " to " + checkOut);
        io.printInfo("Guests    : " + numberOfGuests);
        io.printInfo("Advance   : " + MoneyFormatter.format(Booking.ADVANCE_AMOUNT)
                + " " + advanceNote);
    }

    /** Validates a NIC and also makes sure it is not taken, so the user can re-type at once. */
    private String checkNewNic(String raw) throws ValidationException {
        String nic = Validator.validateNic(raw);
        if (guestManager.isRegistered(nic)) {
            throw new ValidationException(
                    "A guest with NIC/Passport " + nic + " is already registered.");
        }
        return nic;
    }

    /**
     * Shows any list of enum values as a menu with Back at the end.
     * Generic <T> so the same method works for RoomType, BedType and RoomStatus.
     */
    public <T> T chooseFromList(String title, List<T> values) {
        List<String> options = new ArrayList<>();
        for (T value : values) {
            options.add(value.toString());
        }
        options.add("Back");
        Menu menu = new Menu(io, title, options);
        int choice = menu.show();
        if (menu.isLast(choice)) {
            throw new GoBackException();
        }
        return values.get(choice);
    }
}
