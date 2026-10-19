package smartstay.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats money as "LKR 54,000.00" everywhere in the program, so all screens
 * look the same. Locale.US is fixed on purpose: on a computer set to another
 * region the separators could become "54.000,00", which would be confusing.
 */
public final class MoneyFormatter {

    private static final DecimalFormat FORMAT =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    private MoneyFormatter() {
        // only static methods
    }

    /** "LKR 54,000.00" */
    public static String format(BigDecimal amount) {
        return "LKR " + FORMAT.format(amount);
    }

    /** "54,000.00" - used in tables where the column heading already says LKR. */
    public static String formatNumber(BigDecimal amount) {
        return FORMAT.format(amount);
    }
}
