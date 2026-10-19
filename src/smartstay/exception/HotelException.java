package smartstay.exception;

/**
 * Parent of all the "expected" problems in SmartStay (bad input, broken
 * business rule, record not found).
 *
 * It is a checked exception, so the compiler forces the UI to handle it.
 * Because all our exceptions share this parent, a screen can catch just this
 * one type and print getMessage() to the user.
 */
public class HotelException extends Exception {

    public HotelException(String message) {
        super(message);
    }
}
