package smartstay.model;

/**
 * The four room types the hotel sells.
 * Each constant keeps a readable label so menus and tables look nice
 * instead of showing values like FAMILY_SUITE.
 */
public enum RoomType {
    STANDARD("Standard"),
    DELUXE("Deluxe"),
    SUITE("Suite"),
    FAMILY_SUITE("Family Suite");

    private final String label;

    RoomType(String label) {
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
