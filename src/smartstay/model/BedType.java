package smartstay.model;

/**
 * Bed types a room can have. Bed type does not change the price,
 * it is just information for the guest.
 */
public enum BedType {
    SINGLE("Single"),
    DOUBLE("Double"),
    QUEEN("Queen"),
    KING("King");

    private final String label;

    BedType(String label) {
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
