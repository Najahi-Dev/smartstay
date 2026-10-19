package smartstay.ui;

import java.util.List;

/**
 * The one reusable menu used by every screen.
 *
 * Give it a title and a list of options; show() returns the index (0 based)
 * of the option the user picked. By convention the last option is always
 * "Back" (or "Exit"), so callers can check for it with isLast().
 *
 * In arrow mode the user moves with UP/DOWN and presses ENTER.
 * In numbered mode the user types the number and presses ENTER.
 */
public class Menu {

    private final ConsoleIO io;
    private final String title;
    private final List<String> options;

    public Menu(ConsoleIO io, String title, List<String> options) {
        this.io = io;
        this.title = title;
        this.options = List.copyOf(options); // copy so the caller cannot change it later
    }

    public int show() {
        System.out.println();
        Display.printBox(title);
        if (io.isArrowMode()) {
            int choice = io.getArrowReader().select(options,
                    "Hint: UP/DOWN arrows to move, ENTER to select.");
            System.out.println("  You chose: " + options.get(choice));
            return choice;
        }
        return showNumbered();
    }

    /** True when the chosen index is the last option (Back / Exit). */
    public boolean isLast(int choice) {
        return choice == options.size() - 1;
    }

    private int showNumbered() {
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("   %d. %s%n", i + 1, options.get(i));
        }
        System.out.println("  Hint: type a number and press ENTER.");
        // Numbers on screen start at 1, but list indexes start at 0, hence the -1.
        // Not using io.readNumber here so that # is just "invalid", not "go back".
        while (true) {
            String text = io.readLine("  Choose (1-" + options.size() + "): ");
            try {
                int number = Integer.parseInt(text);
                if (number >= 1 && number <= options.size()) {
                    return number - 1;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message below
            }
            io.printError("'" + text + "' is not a valid choice. Enter a number from 1 to "
                    + options.size() + ".");
        }
    }
}
