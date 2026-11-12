package smartstay.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One reservation of one room by one guest for a date range.
 *
 * A booking links a Guest and a Room (association). The status is not
 * changed with a public setter - only through cancel() and complete() -
 * so a booking cannot jump into a strange state by mistake.
 * The room, dates and guest count can only change together, through
 * change(), after the BookingManager has checked the new details.
 */
public class Booking {

    /** Every booking takes the same refundable advance. */
    public static final BigDecimal ADVANCE_AMOUNT = new BigDecimal("10000.00");

    private final String bookingId;
    private final Guest guest;
    private Room room;
    private int numberOfGuests;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BookingStatus status;

    public Booking(String bookingId, Guest guest, Room room,
                            int numberOfGuests, LocalDate checkIn, LocalDate checkOut) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.numberOfGuests = numberOfGuests;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = BookingStatus.CONFIRMED;
    }

    /**
     * Nights = check-out minus check-in.
     * ChronoUnit.DAYS.between counts whole days, so 2026-01-10 to 2026-01-12
     * gives 2 nights.
     */
    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /**
     * Date overlap check.
     * Two stays clash if the new one starts before the old one ends AND the
     * new one ends after the old one starts. Using "before" and "after"
     * (not "or equal") means a guest can check in on the same day another
     * guest checks out, which is how hotels normally work.
     */
    public boolean overlaps(LocalDate newCheckIn, LocalDate newCheckOut) {
        return newCheckIn.isBefore(checkOut) && newCheckOut.isAfter(checkIn);
    }

    /** Only confirmed bookings still hold the room. */
    public boolean isActive() {
        return status == BookingStatus.CONFIRMED;
    }

    public void cancel() {
        status = BookingStatus.CANCELLED;
    }

    public void complete() {
        status = BookingStatus.COMPLETED;
    }

    /**
     * Changes the room, guest count and dates in one step. The booking ID,
     * guest, status and advance stay the same. Only BookingManager.modifyBooking
     * calls this, after every booking rule has been checked.
     */
    public void change(Room newRoom, int newNumberOfGuests,
                       LocalDate newCheckIn, LocalDate newCheckOut) {
        this.room = newRoom;
        this.numberOfGuests = newNumberOfGuests;
        this.checkIn = newCheckIn;
        this.checkOut = newCheckOut;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public BookingStatus getStatus() {
        return status;
    }
}
