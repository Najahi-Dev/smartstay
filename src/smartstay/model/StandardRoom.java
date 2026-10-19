package smartstay.model;

import java.math.BigDecimal;

/**
 * Standard room - the basic package.
 * Package: LKR 6000 per night, breakfast not included,
 * LKR 1000 per extra guest per night, 10% tax, covers 2 guests.
 */
public class StandardRoom extends Room {

    // static final because every room of this type shares the same package values
    private static final BigDecimal DAILY_RATE = new BigDecimal("6000");
    private static final BigDecimal EXTRA_GUEST_CHARGE = new BigDecimal("1000");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final int CAPACITY = 2;

    public StandardRoom(String roomId, BedType bedType) {
        super(roomId, bedType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.STANDARD;
    }

    @Override
    public BigDecimal getDailyRate() {
        return DAILY_RATE;
    }

    @Override
    public boolean includesBreakfast() {
        return false;
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
