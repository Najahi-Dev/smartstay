package smartstay.util;

import smartstay.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * All input checks in one place.
 *
 * Each method takes the raw text the user typed and either returns a cleaned
 * value (trimmed, upper case, etc.) or throws a ValidationException with a
 * message that says exactly what is wrong. Returning the cleaned value means
 * callers cannot forget to tidy the input after checking it.
 */
public final class Validator {

    /** Check-in must be at least this many days after today. */
    public static final int MIN_DAYS_IN_ADVANCE = 2;

    // Patterns are compiled once and reused (compiling a regex is slow-ish).
    private static final Pattern NIC_PATTERN = Pattern.compile("[A-Z0-9]{6,12}");
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z]+( [A-Za-z]+)*");
    private static final Pattern PHONE_PATTERN = Pattern.compile("0\\d{9}");
    // simple check: something@something.something, no spaces
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");
    private static final Pattern ROOM_ID_PATTERN = Pattern.compile("[A-Z0-9]{2,6}");

    // "uuuu" + STRICT makes Java reject dates that do not exist, like 2026-02-30.
    // With the default (SMART) style Java would quietly change it to 2026-02-28.
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private Validator() {
        // only static methods
    }

    /**
     * NIC or passport: letters and digits only, 6 to 12 characters.
     * Old Sri Lankan NIC = 10 (e.g. 987654321V), new NIC = 12 digits,
     * passports are usually 8 or 9, so 6 to 12 covers all of them.
     * Stored in upper case so "987654321v" and "987654321V" count as the same.
     */
    public static String validateNic(String raw) throws ValidationException {
        String nic = requireNotEmpty(raw, "NIC/Passport").toUpperCase();
        if (!nic.chars().allMatch(Character::isLetterOrDigit)) {
            throw new ValidationException(
                    "NIC/Passport can only contain letters and digits (no spaces or symbols).");
        }
        if (!NIC_PATTERN.matcher(nic).matches()) {
            throw new ValidationException("NIC/Passport must be 6 to 12 characters long.");
        }
        return nic;
    }

    /** Full name: letters and single spaces, 2 to 50 characters. */
    public static String validateName(String raw) throws ValidationException {
        // collapse "Nimal    Perera" into "Nimal Perera" before checking
        String name = requireNotEmpty(raw, "Name").replaceAll("\\s+", " ");
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new ValidationException("Name can only contain letters and spaces.");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new ValidationException("Name must be 2 to 50 characters long.");
        }
        return name;
    }

    /** Sri Lankan style phone number: exactly 10 digits starting with 0. */
    public static String validatePhone(String raw) throws ValidationException {
        String phone = requireNotEmpty(raw, "Phone number");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(
                    "Phone number must be 10 digits and start with 0, for example 0771234567.");
        }
        return phone;
    }

    public static String validateEmail(String raw) throws ValidationException {
        String email = requireNotEmpty(raw, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(
                    "Email must look like name@example.com.");
        }
        return email.toLowerCase();
    }

    /** Room ID: 2 to 6 letters or digits, stored in upper case, e.g. R101. */
    public static String validateRoomId(String raw) throws ValidationException {
        String roomId = requireNotEmpty(raw, "Room ID").toUpperCase();
        if (!ROOM_ID_PATTERN.matcher(roomId).matches()) {
            throw new ValidationException(
                    "Room ID must be 2 to 6 letters or digits, for example R101.");
        }
        return roomId;
    }

    /** Turns "2026-12-25" into a LocalDate, or explains the expected format. */
    public static LocalDate parseDate(String raw) throws ValidationException {
        String text = requireNotEmpty(raw, "Date");
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "'" + text + "' is not a valid date. Use yyyy-MM-dd, for example 2026-12-25.");
        }
    }

    /** Reads a whole number and makes sure it is inside [min, max]. */
    public static int parseNumber(String raw, int min, int max) throws ValidationException {
        String text = requireNotEmpty(raw, "Number");
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' is not a whole number.");
        }
        if (value < min || value > max) {
            throw new ValidationException(
                    "Please enter a number from " + min + " to " + max + ".");
        }
        return value;
    }

    /**
     * Booking date rules:
     *  1. check-in must be on or after today + 2 days
     *  2. check-out must be after check-in (so at least 1 night)
     * "today" is passed in instead of calling LocalDate.now() here, so the
     * rule is easy to test with any date.
     */
    public static void checkStayDates(LocalDate checkIn, LocalDate checkOut, LocalDate today)
            throws ValidationException {
        checkCheckInDate(checkIn, today);
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("Check-out must be after check-in.");
        }
    }

    /** Rule 1 on its own, so the UI can check it as soon as check-in is typed. */
    public static void checkCheckInDate(LocalDate checkIn, LocalDate today)
            throws ValidationException {
        LocalDate earliest = today.plusDays(MIN_DAYS_IN_ADVANCE);
        if (checkIn.isBefore(earliest)) {
            throw new ValidationException("Check-in must be at least "
                    + MIN_DAYS_IN_ADVANCE + " days from today (earliest: " + earliest + ").");
        }
    }

    private static String requireNotEmpty(String raw, String fieldName)
            throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        return raw.trim();
    }
}
