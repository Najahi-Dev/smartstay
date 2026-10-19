package smartstay.exception;

/**
 * Thrown by the input reader when the user types # in the middle of a form.
 *
 * Forms ask several questions in a row. Without this, a user who changes
 * their mind would have to finish every question first. Throwing an
 * exception jumps straight out of the form back to the menu.
 * It is unchecked (RuntimeException) because it is about navigation, not an
 * error, and it would be noisy to declare it on every form method.
 */
public class GoBackException extends RuntimeException {

    public GoBackException() {
        super("User chose to go back");
    }
}
