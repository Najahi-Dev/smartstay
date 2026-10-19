package smartstay.exception;

/**
 * Thrown when a Room ID, NIC/Passport or Booking ID does not exist.
 */
public class NotFoundException extends HotelException {

    public NotFoundException(String message) {
        super(message);
    }
}
