package smartstay.service;

import smartstay.exception.BusinessRuleException;
import smartstay.exception.NotFoundException;
import smartstay.model.BedType;
import smartstay.model.Booking;
import smartstay.model.Room;
import smartstay.model.RoomFactory;
import smartstay.model.RoomStatus;
import smartstay.model.RoomType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps all rooms in a HashMap keyed by Room ID, and holds the room rules
 * (no duplicate IDs, no removing a booked room, etc.).
 *
 * It needs the BookingManager to answer "does this room have an active
 * booking?", so it is given one in the constructor (dependency).
 */
public class RoomManager {

    private final Map<String, Room> rooms = new HashMap<>();
    private final BookingManager bookingManager;

    public RoomManager(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    /** Creates the right subclass through the factory and stores it. */
    public Room addRoom(String roomId, RoomType type, BedType bedType)
            throws BusinessRuleException {
        if (roomExists(roomId)) {
            throw new BusinessRuleException("Room ID " + roomId + " already exists.");
        }
        Room room = RoomFactory.createRoom(type, roomId, bedType);
        rooms.put(roomId, room);
        return room;
    }

    public void removeRoom(String roomId)
            throws NotFoundException, BusinessRuleException {
        findRoom(roomId); // throws if the room does not exist
        if (bookingManager.hasActiveBookingForRoom(roomId)) {
            throw new BusinessRuleException("Room " + roomId
                    + " has an active booking and cannot be removed.");
        }
        rooms.remove(roomId);
    }

    public void updateBedType(String roomId, BedType bedType)
            throws NotFoundException {
        findRoom(roomId).setBedType(bedType);
    }

    /**
     * Admin can switch a room between Available and Under Maintenance.
     * Reserved is only set by the booking system, and a room that has an
     * active booking cannot be changed, otherwise a guest's booking would
     * point to a room that is "Available" or "Under Maintenance".
     */
    public void changeStatus(String roomId, RoomStatus newStatus)
            throws NotFoundException, BusinessRuleException {
        Room room = findRoom(roomId);
        if (newStatus == RoomStatus.RESERVED) {
            throw new BusinessRuleException(
                    "Rooms become Reserved only by making a booking.");
        }
        if (bookingManager.hasActiveBookingForRoom(roomId)) {
            throw new BusinessRuleException("Room " + roomId
                    + " has an active booking. Cancel or complete the booking first.");
        }
        room.setStatus(newStatus);
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    public Room findRoom(String roomId) throws NotFoundException {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new NotFoundException("No room found with ID " + roomId + ".");
        }
        return room;
    }

    /** All rooms in Room ID order, so the table is easy to read. */
    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>(rooms.values());
        list.sort(Comparator.comparing(Room::getRoomId));
        return list;
    }

    /**
     * Search available rooms by type and date.
     * A room is returned only if:
     *   1. it is the requested type,
     *   2. its status is Available (so not Reserved or Under Maintenance), and
     *   3. no active booking overlaps the dates (safety check).
     */
    public List<Room> findAvailableRooms(RoomType type,
                                                  LocalDate checkIn, LocalDate checkOut) {
        List<Room> result = new ArrayList<>();
        for (Room room : getAllRooms()) {
            if (room.getRoomType() == type
                    && room.isAvailable()
                    && bookingManager.isRoomFreeForDates(room, checkIn, checkOut)) {
                result.add(room);
            }
        }
        return result;
    }

    /**
     * Rooms a booking can move to when it is changed. Same as
     * findAvailableRooms, except the booking's own room is also offered
     * (it shows as Reserved, but only this booking holds it), and the
     * booking's own old dates are ignored in the overlap check.
     */
    public List<Room> findRoomsForChange(RoomType type, LocalDate checkIn,
                                         LocalDate checkOut, Booking booking) {
        List<Room> result = new ArrayList<>();
        for (Room room : getAllRooms()) {
            boolean ownRoom = room.getRoomId().equals(booking.getRoom().getRoomId());
            if (room.getRoomType() == type
                    && (room.isAvailable() || ownRoom)
                    && bookingManager.isRoomFreeForDates(room, checkIn, checkOut, booking)) {
                result.add(room);
            }
        }
        return result;
    }
}
