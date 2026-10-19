package smartstay.model;

import java.math.BigDecimal;

/**
 * Deluxe room - a better room with breakfast.
 * Package: LKR 9000 per night, breakfast included,
 * LKR 1500 per extra guest per night, 12% tax, covers 2 guests.
 */
public class DeluxeRoom extends Room {

    // static final because every room of this type shares the same package values
    private static final BigDecimal DAILY_RATE = new BigDecimal("9000");
    private static final BigDecimal EXTRA_GUEST_CHARGE = new BigDecimal("1500");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.12");
    private static final int CAPACITY = 2;

    public DeluxeRoom(String roomId, BedType bedType) {
        super(roomId, bedType);
    }

    @Override
    public RoomType getRoomType() {
        return RoomType.DELUXE;
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
