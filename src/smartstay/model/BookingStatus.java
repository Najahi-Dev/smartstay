package smartstay.model;

/**
 * Life cycle of a booking.
 * CONFIRMED = active booking (advance paid, stay not finished yet).
 * CANCELLED = guest cancelled in time, advance refunded.
 * COMPLETED = invoice created, stay is closed.
 */
public enum BookingStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String label;

    BookingStatus(String label) {
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
