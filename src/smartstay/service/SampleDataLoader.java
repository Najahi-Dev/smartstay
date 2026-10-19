package smartstay.service;

import smartstay.exception.HotelException;
import smartstay.model.BedType;
import smartstay.model.Guest;
import smartstay.model.Room;
import smartstay.model.RoomStatus;
import smartstay.model.RoomType;

import java.time.LocalDate;

/**
 * Fills the system with demo data at start up, because nothing is saved
 * between runs (in-memory storage only).
 *
 * Booking dates are worked out from today, so the demo works on any day:
 *  - BK-0001 starts in 10 days -> can be cancelled (more than 24 hours away)
 *    and is the worked example: Deluxe, 6 nights, 3 guests.
 *  - BK-0002 starts today      -> cannot be cancelled (inside 24 hours).
 */
public final class SampleDataLoader {

    private SampleDataLoader() {
        // only static methods
    }

    public static void load(RoomManager roomManager, GuestManager guestManager,
                            BookingManager bookingManager) throws HotelException {
        loadRooms(roomManager);
        loadGuests(guestManager);
        loadBookings(roomManager, guestManager, bookingManager);
    }

    private static void loadRooms(RoomManager rooms) throws HotelException {
        rooms.addRoom("R101", RoomType.STANDARD, BedType.SINGLE);
        rooms.addRoom("R102", RoomType.STANDARD, BedType.DOUBLE);
        rooms.addRoom("R103", RoomType.STANDARD, BedType.DOUBLE);
        rooms.addRoom("R201", RoomType.DELUXE, BedType.QUEEN);
        rooms.addRoom("R202", RoomType.DELUXE, BedType.KING);
        rooms.addRoom("R203", RoomType.DELUXE, BedType.DOUBLE);
        rooms.addRoom("R301", RoomType.SUITE, BedType.KING);
        rooms.addRoom("R302", RoomType.SUITE, BedType.QUEEN);
        rooms.addRoom("R401", RoomType.FAMILY_SUITE, BedType.KING);
        rooms.addRoom("R402", RoomType.FAMILY_SUITE, BedType.QUEEN);
        rooms.changeStatus("R103", RoomStatus.UNDER_MAINTENANCE);
    }

    private static void loadGuests(GuestManager guests) throws HotelException {
        guests.registerGuest("200012345678", "Nimal Perera", "0771234567", "nimal.perera@example.com");
        guests.registerGuest("987654321V", "Kamala Silva", "0712345678", "kamala.silva@example.com");
        guests.registerGuest("N1234567", "Sarah Johnson", "0759876543", "sarah.johnson@example.com");
    }

    private static void loadBookings(RoomManager rooms, GuestManager guests,
                                     BookingManager bookings) throws HotelException {
        LocalDate today = LocalDate.now();

        Guest nimal = guests.findGuest("200012345678");
        Room deluxe = rooms.findRoom("R201");
        bookings.addSampleBooking(nimal, deluxe, 3, today.plusDays(10), today.plusDays(16));

        Guest kamala = guests.findGuest("987654321V");
        Room suite = rooms.findRoom("R301");
        bookings.addSampleBooking(kamala, suite, 2, today, today.plusDays(3));
    }
}
