package smartstay.model;

/**
 * Factory pattern.
 *
 * The rest of the program asks the factory for a room of a given type and
 * gets back the correct subclass. Only this class knows which subclass
 * matches which RoomType, so if a new room type is added later we change
 * one place instead of every screen that creates rooms.
 */
public final class RoomFactory {

    private RoomFactory() {
        // only static methods, so no objects of this class are needed
    }

    public static Room createRoom(RoomType type, String roomId,
                                           BedType bedType) {
        // switch expression: the compiler complains if a RoomType is not handled
        return switch (type) {
            case STANDARD -> new StandardRoom(roomId, bedType);
            case DELUXE -> new DeluxeRoom(roomId, bedType);
            case SUITE -> new SuiteRoom(roomId, bedType);
            case FAMILY_SUITE -> new FamilySuiteRoom(roomId, bedType);
        };
    }
}
