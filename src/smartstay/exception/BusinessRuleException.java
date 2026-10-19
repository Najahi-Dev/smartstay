package smartstay.exception;

/**
 * Thrown when the input looks fine but a hotel rule says no, for example
 * booking a room that is Under Maintenance, cancelling within 24 hours of
 * check-in, or adding a Room ID that already exists.
 */
public class BusinessRuleException extends HotelException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
