package smartstay.ui;

import smartstay.model.BookingStatus;
import smartstay.model.RoomStatus;

/**
 * ANSI colours for the console.
 *
 * Colour is only switched on in arrow mode, because JLine has already
 * checked that the terminal understands control codes. In numbered mode
 * (IDE console, input from a file) every method just returns the text
 * unchanged, so no junk like "[31m" appears on screen.
 * The NO_COLOR environment variable (a common standard) also turns it off.
 *
 * The [!] and [OK] markers are kept even with colour, so the messages still
 * make sense for colour blind users and in plain text output.
 *
 * Colour codes are invisible but printf still counts them as characters,
 * so callers must pad text to the column width first and colour it after.
 */
public final class Colors {

    private static final String ESC = "\u001B[";
    private static final String RESET = ESC + "0m";

    private static boolean enabled = false;

    private Colors() {
        // only static methods
    }

    /** Turns colour on only if asked for and NO_COLOR is not set. */
    public static void enable(boolean on) {
        String noColor = System.getenv("NO_COLOR");
        enabled = on && (noColor == null || noColor.isEmpty());
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String red(String text) {
        return wrap("31", text);
    }

    public static String green(String text) {
        return wrap("32", text);
    }

    public static String yellow(String text) {
        return wrap("33", text);
    }

    /** Bold cyan, used for titles. */
    public static String cyan(String text) {
        return wrap("1;36", text);
    }

    /** Dim grey, used for hints that are less important. */
    public static String grey(String text) {
        return wrap("90", text);
    }

    /** Bold yellow, used for the final amount on the invoice. */
    public static String highlight(String text) {
        return wrap("1;33", text);
    }

    /** Green = free / fine, yellow = taken, red = blocked, grey = finished. */
    public static String forRoomStatus(RoomStatus status, String text) {
        return switch (status) {
            case AVAILABLE -> green(text);
            case RESERVED -> yellow(text);
            case UNDER_MAINTENANCE -> red(text);
        };
    }

    public static String forBookingStatus(BookingStatus status, String text) {
        return switch (status) {
            case CONFIRMED -> green(text);
            case CANCELLED -> red(text);
            case COMPLETED -> grey(text);
        };
    }

    private static String wrap(String code, String text) {
        return enabled ? ESC + code + "m" + text + RESET : text;
    }
}
