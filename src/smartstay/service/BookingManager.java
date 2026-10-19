package smartstay.service;

import smartstay.exception.BusinessRuleException;
import smartstay.exception.NotFoundException;
import smartstay.exception.ValidationException;
import smartstay.model.Booking;
import smartstay.model.Guest;
import smartstay.model.Room;
import smartstay.model.RoomStatus;
import smartstay.util.Validator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all bookings and applies the booking rules:
 * creating, cancelling, searching, sorting and the date overlap check.
 *
 * Bookings are kept in a HashMap by Booking ID, so finding one by ID is fast
 * (no need to loop through every booking).
 */
public class BookingManager {

    /** A booking can have this many guests above the room capacity (each one is charged). */
    public static final int MAX_EXTRA_GUESTS = 2;

    /** Hotel check-in time. The 24 hour rule is counted back from this time. */
    public static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);

    private static final long NO_CHANGE_HOURS = 24;

    /**
     * Sorts bookings by check-in date. If two bookings start on the same day,
     * the booking ID decides, so the order is always the same.
     */
    private static final Comparator<Booking> BY_CHECK_IN =
            Comparator.comparing(Booking::getCheckIn)
                    .thenComparing(Booking::getBookingId);

    private final Map<String, Booking> bookings = new HashMap<>();
    private int lastBookingNumber = 0;

    /**
     * Makes a new booking after checking every rule.
     * The order of checks goes from cheap/simple to the overlap loop.
     */
    public Booking createBooking(Guest guest, Room room,
                                          int numberOfGuests, LocalDate checkIn, LocalDate checkOut)
            throws ValidationException, BusinessRuleException {
        Validator.checkStayDates(checkIn, checkOut, LocalDate.now());
        checkRoomCanBeBooked(room);
        checkGuestCount(room, numberOfGuests);
        // Safety check: the status should already stop double booking, but we
        // still look at the dates in case the status was wrong for any reason.
        if (!isRoomFreeForDates(room, checkIn, checkOut)) {
            throw new BusinessRuleException("Room " + room.getRoomId()
                    + " already has a booking that overlaps these dates.");
        }
        return storeBooking(guest, room, numberOfGuests, checkIn, checkOut);
    }

    /**
     * Used only by the sample data loader to add bookings that were "made
     * earlier". It skips the 2-day rule because one sample booking starts
     * today, so the 24 hour cancel rule can be shown in the demo.
     */
    Booking addSampleBooking(Guest guest, Room room,
                                      int numberOfGuests, LocalDate checkIn, LocalDate checkOut) {
        return storeBooking(guest, room, numberOfGuests, checkIn, checkOut);
    }

    /**
     * Cancels a booking. The guest gets the LKR 10,000 advance back.
     * After cancelling, the room becomes Available again unless another
     * active booking still holds it.
     */
    public Booking cancelBooking(String bookingId)
            throws NotFoundException, BusinessRuleException {
        Booking booking = findBooking(bookingId);
        checkCanCancel(booking);
        booking.cancel();
        releaseRoomIfFree(booking.getRoom());
        return booking;
    }

    /**
     * Throws if the booking cannot be cancelled. Public so the UI can check
     * BEFORE asking "Are you sure?", instead of after.
     */
    public void checkCanCancel(Booking booking) throws BusinessRuleException {
        if (!booking.isActive()) {
            throw new BusinessRuleException("Booking " + booking.getBookingId() + " is "
                    + booking.getStatus() + " and cannot be cancelled.");
        }
        checkOutsideNoChangeWindow(booking, LocalDateTime.now());
    }

    /** Called by billing when the invoice is made: the stay is finished. */
    public void completeBooking(Booking booking) {
        booking.complete();
        releaseRoomIfFree(booking.getRoom());
    }

    /**
     * 24 hour rule.
     * Duration.between(now, checkInMoment) gives the time left until check-in.
     * If it is less than 24 hours (or check-in has already passed, which gives
     * a negative duration) the booking can no longer be cancelled.
     */
    private void checkOutsideNoChangeWindow(Booking booking, LocalDateTime now)
            throws BusinessRuleException {
        LocalDateTime checkInMoment = booking.getCheckIn().atTime(CHECK_IN_TIME);
        long hoursLeft = Duration.between(now, checkInMoment).toHours();
        if (hoursLeft < NO_CHANGE_HOURS) {
            throw new BusinessRuleException(
                    "Reservations cannot be cancelled within 24 hours of check-in (check-in is "
                            + booking.getCheckIn() + " at " + CHECK_IN_TIME + ").");
        }
    }

    public Booking findBooking(String bookingId) throws NotFoundException {
        // IDs are stored as "BK-0001"; accept "bk-0001" too
        Booking booking = bookings.get(bookingId.trim().toUpperCase());
        if (booking == null) {
            throw new NotFoundException("No booking found with ID " + bookingId + ".");
        }
        return booking;
    }

    /**
     * Search by guest name: case insensitive, partial match.
     * Both the name and the search text are turned to lower case, then
     * contains() finds the text anywhere in the name ("per" matches "Nimal Perera").
     */
    public List<Booking> searchByGuestName(String text) {
        String searchText = text.trim().toLowerCase();
        List<Booking> matches = new ArrayList<>();
        for (Booking booking : bookings.values()) {
            if (booking.getGuest().getFullName().toLowerCase().contains(searchText)) {
                matches.add(booking);
            }
        }
        matches.sort(BY_CHECK_IN);
        return matches;
    }

    /** Every booking, earliest check-in first. */
    public List<Booking> getAllBookingsSorted() {
        List<Booking> list = new ArrayList<>(bookings.values());
        list.sort(BY_CHECK_IN);
        return list;
    }

    /** One guest's bookings, earliest check-in first. */
    public List<Booking> getBookingsForGuest(String nic) {
        List<Booking> list = new ArrayList<>();
        for (Booking booking : bookings.values()) {
            if (booking.getGuest().getNic().equals(nic)) {
                list.add(booking);
            }
        }
        list.sort(BY_CHECK_IN);
        return list;
    }

    /**
     * Overlap check against every active booking of this room.
     * Cancelled and completed bookings are ignored because they no longer
     * hold the room.
     */
    public boolean isRoomFreeForDates(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Booking booking : bookings.values()) {
            boolean sameRoom = booking.getRoom().getRoomId().equals(room.getRoomId());
            if (sameRoom && booking.isActive() && booking.overlaps(checkIn, checkOut)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasActiveBookingForRoom(String roomId) {
        for (Booking booking : bookings.values()) {
            if (booking.isActive() && booking.getRoom().getRoomId().equals(roomId)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveBookingForGuest(String nic) {
        for (Booking booking : bookings.values()) {
            if (booking.isActive() && booking.getGuest().getNic().equals(nic)) {
                return true;
            }
        }
        return false;
    }

    // ---------------- private helpers ----------------

    private Booking storeBooking(Guest guest, Room room,
                                          int numberOfGuests, LocalDate checkIn, LocalDate checkOut) {
        String bookingId = nextBookingId();
        Booking booking =
                new Booking(bookingId, guest, room, numberOfGuests, checkIn, checkOut);
        bookings.put(bookingId, booking);
        room.setStatus(RoomStatus.RESERVED);
        return booking;
    }

    /** BK-0001, BK-0002 ... %04d pads the number with zeros to 4 digits. */
    private String nextBookingId() {
        lastBookingNumber++;
        return String.format("BK-%04d", lastBookingNumber);
    }

    private void checkRoomCanBeBooked(Room room) throws BusinessRuleException {
        if (!room.isAvailable()) {
            throw new BusinessRuleException("Room " + room.getRoomId() + " is "
                    + room.getStatus() + " and cannot be booked. Only Available rooms can be booked.");
        }
    }

    private void checkGuestCount(Room room, int numberOfGuests)
            throws BusinessRuleException {
        int maxGuests = room.getCapacity() + MAX_EXTRA_GUESTS;
        if (numberOfGuests < 1 || numberOfGuests > maxGuests) {
            throw new BusinessRuleException("A " + room.getRoomType()
                    + " room allows 1 to " + maxGuests + " guests.");
        }
    }

    /** Puts a Reserved room back to Available when no active booking is left on it. */
    private void releaseRoomIfFree(Room room) {
        boolean reserved = room.getStatus() == RoomStatus.RESERVED;
        if (reserved && !hasActiveBookingForRoom(room.getRoomId())) {
            room.setStatus(RoomStatus.AVAILABLE);
        }
    }
}
