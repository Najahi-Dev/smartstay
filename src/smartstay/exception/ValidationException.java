package smartstay.exception;

/**
 * Thrown when a value typed by the user has the wrong format or is out of
 * range, for example a phone number with 9 digits or a date like 2026-02-30.
 */
public class ValidationException extends HotelException {

    public ValidationException(String message) {
        super(message);
    }
}
