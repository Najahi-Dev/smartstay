package smartstay.model;

/**
 * Availability status of a room.
 * Only AVAILABLE rooms can be booked. RESERVED is set by the system when a
 * booking is made, and UNDER_MAINTENANCE is set by the admin.
 */
public enum RoomStatus {
    AVAILABLE("Available"),
    RESERVED("Reserved"),
    UNDER_MAINTENANCE("Under Maintenance");

    private final String label;

    RoomStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
