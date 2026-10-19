package smartstay.ui;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * The ONLY class that uses the JLine library.
 *
 * Keeping JLine in one class means the rest of the program does not care
 * whether arrow keys work. If JLine is missing, or the terminal cannot handle
 * arrow keys (for example the console inside an IDE), tryCreate() returns
 * null and the program uses numbered menus instead.
 */
public class ArrowKeyReader {

    // What a key press means for the menu. Strings keep it simple (no extra enum).
    private static final String KEY_UP = "up";
    private static final String KEY_DOWN = "down";
    private static final String KEY_ENTER = "enter";

    // ANSI escape codes (understood by Windows Terminal, PowerShell, macOS and Linux terminals)
    private static final String ESC = "\u001B[";
    private static final String HIGHLIGHT_ON = ESC + "7m";   // reverse colours
    private static final String HIGHLIGHT_OFF = ESC + "0m";
    private static final String CLEAR_LINE = "\r" + ESC + "2K";

    private final Terminal terminal;
    private final LineReader lineReader;
    private final BindingReader keyReader;
    private final KeyMap<String> menuKeys;

    private ArrowKeyReader(Terminal terminal) {
        this.terminal = terminal;
        this.lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        this.keyReader = new BindingReader(terminal.reader());
        this.menuKeys = buildMenuKeys(terminal);
    }

    /**
     * Returns a reader if this terminal supports arrow keys, otherwise null.
     * A "dumb" terminal is what JLine gives when it cannot control the
     * screen (IDE console, input piped from a file), so we treat it as "no".
     */
    public static ArrowKeyReader tryCreate() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).dumb(true).build();
            if (terminal.getType().startsWith(Terminal.TYPE_DUMB)) {
                terminal.close();
                return null;
            }
            return new ArrowKeyReader(terminal);
        } catch (IOException e) {
            return null;
        }
    }

    /** Reads one line of text. Returns null if input has ended (Ctrl+D / Ctrl+Z). */
    public String readLine(String prompt) {
        return readLine(prompt, null);
    }

    /** Reads a line and shows * for each character (used for the admin password). */
    public String readHiddenLine(String prompt) {
        return readLine(prompt, '*');
    }

    /**
     * Shows the options and lets the user move with UP/DOWN and pick with ENTER.
     * The hint line is printed under the options.
     * Returns the index of the chosen option.
     */
    public int select(List<String> options, String hint) {
        int selected = 0;
        PrintWriter out = terminal.writer();
        // Raw mode sends every key press straight to us, without waiting for ENTER.
        Attributes savedSettings = terminal.enterRawMode();
        try {
            drawOptions(out, options, selected, hint);
            while (true) {
                String key = keyReader.readBinding(menuKeys);
                if (key == null) {
                    // input has ended, so pick the last option (always Back or Exit)
                    return options.size() - 1;
                }
                if (KEY_ENTER.equals(key)) {
                    return selected;
                }
                // wrap around: UP on the first option jumps to the last one
                if (KEY_UP.equals(key)) {
                    selected = (selected - 1 + options.size()) % options.size();
                } else if (KEY_DOWN.equals(key)) {
                    selected = (selected + 1) % options.size();
                }
                // move the cursor back up (options + hint line) and draw again in place
                out.print(ESC + (options.size() + 1) + "A");
                drawOptions(out, options, selected, hint);
            }
        } finally {
            terminal.setAttributes(savedSettings); // always give the normal terminal back
        }
    }

    private String readLine(String prompt, Character mask) {
        try {
            return lineReader.readLine(prompt, mask);
        } catch (UserInterruptException e) {
            return "";   // Ctrl+C: treat as an empty answer instead of crashing
        } catch (EndOfFileException e) {
            return null;
        }
    }

    private void drawOptions(PrintWriter out, List<String> options, int selected, String hint) {
        for (int i = 0; i < options.size(); i++) {
            String line = String.format("  %d. %s", i + 1, options.get(i));
            if (i == selected) {
                out.print(CLEAR_LINE + HIGHLIGHT_ON + " >" + line + " " + HIGHLIGHT_OFF + "\n");
            } else {
                out.print(CLEAR_LINE + "  " + line + "\n");
            }
        }
        out.print(CLEAR_LINE + "  " + hint + "\n");
        out.flush();
    }

    /**
     * Terminals send different codes for the arrow keys, so we bind the code
     * this terminal says it uses, plus the two common ones (ESC[A and ESC OA).
     */
    private static KeyMap<String> buildMenuKeys(Terminal terminal) {
        KeyMap<String> keys = new KeyMap<>();
        bindIfKnown(keys, KEY_UP, KeyMap.key(terminal, InfoCmp.Capability.key_up));
        bindIfKnown(keys, KEY_DOWN, KeyMap.key(terminal, InfoCmp.Capability.key_down));
        keys.bind(KEY_UP, "\u001B[A", "\u001BOA");
        keys.bind(KEY_DOWN, "\u001B[B", "\u001BOB");
        keys.bind(KEY_ENTER, "\r", "\n");
        return keys;
    }

    private static void bindIfKnown(KeyMap<String> keys, String action, String keyCode) {
        if (keyCode != null) {
            keys.bind(action, keyCode);
        }
    }
}
