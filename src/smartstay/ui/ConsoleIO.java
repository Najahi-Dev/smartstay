package smartstay.ui;

import smartstay.exception.GoBackException;
import smartstay.exception.HotelException;
import smartstay.exception.ValidationException;
import smartstay.util.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

/**
 * All keyboard input and short messages go through this class.
 *
 * It works in two modes:
 *   - arrow mode: JLine is available and the terminal supports arrow keys
 *   - numbered mode: plain System.in, menus ask for a number
 * Screens never need to know which mode is on; they just call readText(),
 * readDate(), confirm() and so on.
 *
 * Every read method keeps asking until the answer is valid, so bad input
 * shows a message instead of crashing. Typing # in a form jumps back to the
 * menu (see GoBackException).
 */
public class ConsoleIO {

    public static final String GO_BACK_KEY = "#";

    /** Lets a screen pass in a validator method, e.g. Validator::validateName. */
    @FunctionalInterface
    public interface TextCheck {
        String check(String raw) throws ValidationException;
    }

    /** One menu action that may fail with a hotel error. */
    @FunctionalInterface
    public interface ScreenAction {
        void run() throws HotelException;
    }

    private final ArrowKeyReader arrowReader; // null in numbered mode
    private final BufferedReader plainReader = new BufferedReader(new InputStreamReader(System.in));

    /**
     * @param forceNumbered true when the user started the program with --numbered
     */
    public ConsoleIO(boolean forceNumbered) {
        this.arrowReader = forceNumbered ? null : createArrowReader();
    }

    public boolean isArrowMode() {
        return arrowReader != null;
    }

    /** Used by the Menu class in arrow mode. */
    ArrowKeyReader getArrowReader() {
        return arrowReader;
    }

    // ---------------- reading input ----------------

    /**
     * Reads one raw line. If the input stream has ended (for example the user
     * pressed Ctrl+Z / Ctrl+D) there is nothing more we can ask, so the
     * program says goodbye and stops cleanly instead of looping forever.
     */
    public String readLine(String prompt) {
        String line;
        if (arrowReader != null) {
            System.out.flush();
            line = arrowReader.readLine(prompt);
        } else {
            System.out.print(prompt);
            System.out.flush();
            line = readPlainLine();
        }
        if (line == null) {
            System.out.println();
            System.out.println("Input closed. Goodbye!");
            System.exit(0);
        }
        return line.trim();
    }

    /** Reads text; typing # throws GoBackException to leave the form. */
    public String readText(String prompt) {
        String text = readLine(prompt);
        if (GO_BACK_KEY.equals(text)) {
            throw new GoBackException();
        }
        return text;
    }

    /** Asks again and again until the check passes, then returns the cleaned value. */
    public String readValid(String prompt, TextCheck check) {
        while (true) {
            try {
                return check.check(readText(prompt));
            } catch (ValidationException e) {
                printError(e.getMessage());
            }
        }
    }

    /**
     * Same as readValid, but pressing ENTER on an empty line keeps the
     * current value. Used by the Update screens.
     */
    public String readValidOrKeep(String prompt, String currentValue, TextCheck check) {
        while (true) {
            String text = readText(prompt + " [" + currentValue + "]: ");
            if (text.isEmpty()) {
                return currentValue;
            }
            try {
                return check.check(text);
            } catch (ValidationException e) {
                printError(e.getMessage());
            }
        }
    }

    public int readNumber(String prompt, int min, int max) {
        while (true) {
            try {
                return Validator.parseNumber(readText(prompt), min, max);
            } catch (ValidationException e) {
                printError(e.getMessage());
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            try {
                return Validator.parseDate(readText(prompt));
            } catch (ValidationException e) {
                printError(e.getMessage());
            }
        }
    }

    /** Password is hidden with * when JLine is available. */
    public String readPassword(String prompt) {
        if (arrowReader == null) {
            return readText(prompt);
        }
        String password = arrowReader.readHiddenLine(prompt);
        if (password == null) {
            return readText(prompt); // input ended: readText handles the exit message
        }
        return password.trim();
    }

    /** Y/N question. Anything other than y/yes/n/no asks again. */
    public boolean confirm(String question) {
        while (true) {
            String answer = readText(question + " (Y/N): ").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            printError("Please type Y for yes or N for no.");
        }
    }

    public void pause() {
        readLine("\nPress ENTER to continue...");
    }

    /**
     * Runs one menu action and deals with every expected problem in one place,
     * so each screen does not need its own try/catch blocks.
     */
    public void runAction(ScreenAction action) {
        try {
            action.run();
        } catch (HotelException e) {
            printError(e.getMessage());
        } catch (GoBackException e) {
            printInfo("Cancelled - nothing was changed.");
        } catch (RuntimeException e) {
            // last safety net: a bug in one action should not close the whole program
            printError("Something went wrong: " + e.getMessage());
        }
        pause();
    }

    // ---------------- printing messages ----------------

    public void printError(String message) {
        System.out.println("  [!] " + message);
    }

    public void printSuccess(String message) {
        System.out.println("  [OK] " + message);
    }

    public void printInfo(String message) {
        System.out.println("  " + message);
    }

    // ---------------- private helpers ----------------

    /**
     * NoClassDefFoundError (a LinkageError) happens if the JLine jar was not
     * put on the classpath. That is not a reason to stop the program, we just
     * use numbered menus.
     */
    private static ArrowKeyReader createArrowReader() {
        try {
            return ArrowKeyReader.tryCreate();
        } catch (LinkageError e) {
            return null;
        }
    }

    private String readPlainLine() {
        try {
            return plainReader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
