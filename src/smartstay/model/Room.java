package smartstay.model;

import java.math.BigDecimal;

/**
 * Base class for every room in the hotel.
 *
 * It is abstract because a "plain room" does not exist - every room is one of
 * the four types. The things that are the same for all rooms (ID, bed type,
 * status) live here. The things that change by type (rate, breakfast, extra
 * guest charge, tax, capacity) are abstract methods, so each subclass gives
 * its own answer. The billing code just calls room.getDailyRate() and gets the
 * right value without checking the type itself (polymorphism).
 */
public abstract class Room {

    private final String roomId;          // never changes once created
    private BedType bedType;
    private RoomStatus status;

    protected Room(String roomId, BedType bedType) {
        this.roomId = roomId;
        this.bedType = bedType;
        this.status = RoomStatus.AVAILABLE; // new rooms start available
    }

    // ---- values each room type must define ----

    public abstract RoomType getRoomType();

    public abstract BigDecimal getDailyRate();

    public abstract boolean includesBreakfast();

    /** Charge per extra guest, per night. */
    public abstract BigDecimal getExtraGuestCharge();

    /** Tax as a fraction, for example 0.12 for 12%. */
    public abstract BigDecimal getTaxRate();

    /** Number of guests the room price already covers. */
    public abstract int getCapacity();

    // ---- shared getters and setters ----

    public String getRoomId() {
        return roomId;
    }

    public BedType getBedType() {
        return bedType;
    }

    public void setBedType(BedType bedType) {
        this.bedType = bedType;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }
}
