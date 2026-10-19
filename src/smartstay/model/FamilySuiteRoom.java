package smartstay.model;

import java.math.BigDecimal;

/**
 * Family Suite - the biggest room, made for families of four.
 * Package: LKR 18000 per night, breakfast included,
 * LKR 2500 per extra guest per night, 15% tax, covers 4 guests.
 */
public class FamilySuiteRoom extends Room {

    // static final because every room of this type shares the same package values
    private static final BigDecimal DAILY_RATE = new BigDecimal("18000");
    private static final BigDecimal EXTRA_GUEST_CHARGE = new BigDecimal("2500");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private static final int CAPACITY = 4;

    public FamilySuiteRoom(String roomId, BedType bedType) {
        super(roomId, bedType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.FAMILY_SUITE;
    }

    @Override
    public BigDecimal getDailyRate() {
        return DAILY_RATE;
    }

    @Override
    public boolean includesBreakfast() {
        return true;
    }

    @Override
    public BigDecimal getExtraGuestCharge() {
        return EXTRA_GUEST_CHARGE;
    }

    @Override
    public BigDecimal getTaxRate() {
        return TAX_RATE;
    }

    @Override
    public int getCapacity() {
        return CAPACITY;
    }
}
