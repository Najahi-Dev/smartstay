package smartstay.model;

import java.math.BigDecimal;

/**
 * Suite - a larger room that fits three guests.
 * Package: LKR 14000 per night, breakfast included,
 * LKR 2000 per extra guest per night, 15% tax, covers 3 guests.
 */
public class SuiteRoom extends Room {

    // static final because every room of this type shares the same package values
    private static final BigDecimal DAILY_RATE = new BigDecimal("14000");
    private static final BigDecimal EXTRA_GUEST_CHARGE = new BigDecimal("2000");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private static final int CAPACITY = 3;

    public SuiteRoom(String roomId, BedType bedType) {
        super(roomId, bedType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.SUITE;
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
