package smartstay.ui;

import smartstay.exception.HotelException;
import smartstay.exception.ValidationException;
import smartstay.model.BedType;
import smartstay.model.Room;
import smartstay.model.RoomStatus;
import smartstay.model.RoomType;
import smartstay.service.RoomManager;
import smartstay.util.Validator;

import java.util.List;

/**
 * Admin > Room Management: add, remove, update, view and change status.
 * This class only asks questions and prints answers; the rules
 * (duplicate ID, room with an active booking, ...) are in RoomManager.
 */
public class AdminRoomScreen {

    private final ConsoleIO io;
    private final SharedForms forms;
    private final RoomManager roomManager;

    public AdminRoomScreen(ConsoleIO io, SharedForms forms,
                                    RoomManager roomManager) {
        this.io = io;
        this.forms = forms;
        this.roomManager = roomManager;
    }

    public void show() {
        Menu menu = new Menu(io, "Admin > Room Management", List.of(
                "Add Room", "Remove Room", "Update Room", "View Rooms", "Change Room Status", "Back"));
        while (true) {
            int choice = menu.show();
            if (menu.isLast(choice)) {
                return;
            }
            switch (choice) {
                case 0 -> io.runAction(this::addRoom);
                case 1 -> io.runAction(this::removeRoom);
                case 2 -> io.runAction(this::updateRoom);
                case 3 -> io.runAction(this::viewRooms);
                case 4 -> io.runAction(this::changeStatus);
                default -> io.printError("Unknown option.");
            }
        }
    }

    private void addRoom() throws HotelException {
        io.printInfo("Type " + ConsoleIO.GO_BACK_KEY + " at any question to go back.");
        String roomId = io.readValid("  New Room ID (e.g. R105): ", this::checkNewRoomId);
        RoomType type = forms.chooseRoomType("Room type for " + roomId);
        BedType bedType = forms.chooseBedType("Bed type for " + roomId);
        Room room = roomManager.addRoom(roomId, type, bedType);
        io.printSuccess("Room added: " + Display.describeRoom(room));
    }

    private void removeRoom() throws HotelException {
        Display.printRooms(roomManager.getAllRooms());
        Room room = askForRoom();
        if (io.confirm("  Remove room " + room.getRoomId() + " (" + room.getRoomType() + ")?")) {
            roomManager.removeRoom(room.getRoomId());
            io.printSuccess("Room " + room.getRoomId() + " removed.");
        } else {
            io.printInfo("Room was not removed.");
        }
    }

    /**
     * Only the bed type can be updated. The room type decides the whole
     * price package (it is the subclass), so a different type means a
     * different room: remove it and add it again.
     */
    private void updateRoom() throws HotelException {
        Display.printRooms(roomManager.getAllRooms());
        Room room = askForRoom();
        io.printInfo("Current bed type: " + room.getBedType()
                + ". (To change the room type, remove the room and add it again.)");
        BedType bedType = forms.chooseBedType("New bed type for " + room.getRoomId());
        roomManager.updateBedType(room.getRoomId(), bedType);
        io.printSuccess("Room " + room.getRoomId() + " now has a " + bedType + " bed.");
    }

    private void viewRooms() {
        Display.printRooms(roomManager.getAllRooms());
    }

    private void changeStatus() throws HotelException {
        Display.printRooms(roomManager.getAllRooms());
        Room room = askForRoom();
        io.printInfo("Current status: " + room.getStatus()
                + ". (Reserved is set automatically by bookings.)");
        RoomStatus newStatus = forms.chooseFromList("New status for " + room.getRoomId(),
                List.of(RoomStatus.AVAILABLE, RoomStatus.UNDER_MAINTENANCE));
        roomManager.changeStatus(room.getRoomId(), newStatus);
        io.printSuccess("Room " + room.getRoomId() + " is now " + newStatus + ".");
    }

    private Room askForRoom() throws HotelException {
        String roomId = io.readValid("  Room ID (" + ConsoleIO.GO_BACK_KEY + " to go back): ",
                Validator::validateRoomId);
        return roomManager.findRoom(roomId);
    }

    /** Checks the format AND that the ID is free, so the admin can re-type straight away. */
    private String checkNewRoomId(String raw) throws ValidationException {
        String roomId = Validator.validateRoomId(raw);
        if (roomManager.roomExists(roomId)) {
            throw new ValidationException("Room ID " + roomId + " already exists.");
        }
        return roomId;
    }
}
