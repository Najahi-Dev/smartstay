package smartstay.ui;

import smartstay.model.Booking;
import smartstay.model.Guest;
import smartstay.model.Invoice;
import smartstay.model.Room;
import smartstay.util.MoneyFormatter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Prints boxes, tables and invoices. No business logic here - it only shows
 * data it is given.
 *
 * Tables use printf with fixed column widths: %-10s means "left aligned,
 * 10 characters wide" and %12s means "right aligned, 12 wide" (used for
 * money so the decimal points line up). Long text is cut with fit() so a
 * long name cannot push the columns out of line.
 * Colour codes are added only AFTER the text is padded, otherwise printf
 * would count the invisible codes and the borders would not line up.
 */
public final class Display {

    private static final int BOX_WIDTH = 64;
    private static final int INVOICE_WIDTH = 62;

    // ----- table layouts: the separator and the format must use the same widths -----
    private static final int[] ROOM_COLS = {6, 12, 6, 12, 9, 3, 17};
    private static final String ROOM_ROW = "| %-6s | %-12s | %-6s | %12s | %-9s | %3s | %s |%n";

    private static final int[] GUEST_COLS = {12, 24, 10, 28};
    private static final String GUEST_ROW = "| %-12s | %-24s | %-10s | %-28s |%n";

    private static final int[] BOOKING_COLS = {7, 20, 5, 12, 10, 10, 6, 6, 9};
    private static final String BOOKING_ROW =
            "| %-7s | %-20s | %-5s | %-12s | %-10s | %-10s | %6s | %6s | %s |%n";

    private static final int[] INVOICE_COLS = {8, 10, 7, 20, 5, 16};
    private static final String INVOICE_ROW = "| %-8s | %-10s | %-7s | %-20s | %-5s | %16s |%n";

    private Display() {
        // only static methods
    }

    /** Draws a title inside a + - | box, title in cyan. */
    public static void printBox(String title) {
        printBox(title, false);
    }

    /** important = true shows the title in bold yellow, e.g. the booking summary. */
    public static void printBox(String title, boolean important) {
        String border = "+" + "-".repeat(BOX_WIDTH - 2) + "+";
        String padded = pad(title, BOX_WIDTH - 4);
        System.out.println(border);
        System.out.println("| " + (important ? Colors.highlight(padded) : Colors.cyan(padded)) + " |");
        System.out.println(border);
    }

    // ---------------- rooms ----------------

    public static void printRooms(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("  No rooms to show.");
            return;
        }
        String separator = separator(ROOM_COLS);
        System.out.println(separator);
        System.out.printf(ROOM_ROW, "Room", "Type", "Bed", "Rate (LKR)", "Breakfast", "Cap",
                pad("Status", ROOM_COLS[6]));
        System.out.println(separator);
        for (Room room : rooms) {
            System.out.printf(ROOM_ROW, room.getRoomId(), room.getRoomType(), room.getBedType(),
                    MoneyFormatter.formatNumber(room.getDailyRate()),
                    room.includesBreakfast() ? "Yes" : "No",
                    room.getCapacity(),
                    Colors.forRoomStatus(room.getStatus(), pad(room.getStatus(), ROOM_COLS[6])));
        }
        System.out.println(separator);
        System.out.println("  Cap = guests included in the rate. Extra guests are charged per night.");
    }

    /** One line used inside menus when the user picks a room. */
    public static String describeRoom(Room room) {
        return String.format("%-6s %-12s %-6s %s/night, extra guest %s, tax %s%%",
                room.getRoomId(), room.getRoomType(), room.getBedType(),
                MoneyFormatter.format(room.getDailyRate()),
                MoneyFormatter.formatNumber(room.getExtraGuestCharge()),
                room.getTaxRate().movePointRight(2).stripTrailingZeros().toPlainString());
    }

    // ---------------- guests ----------------

    public static void printGuests(List<Guest> guests) {
        if (guests.isEmpty()) {
            System.out.println("  No guests to show.");
            return;
        }
        String separator = separator(GUEST_COLS);
        System.out.println(separator);
        System.out.printf(GUEST_ROW, "NIC/Passport", "Full name", "Phone", "Email");
        System.out.println(separator);
        for (Guest guest : guests) {
            System.out.printf(GUEST_ROW, guest.getNic(), fit(guest.getFullName(), 24),
                    guest.getPhone(), fit(guest.getEmail(), 28));
        }
        System.out.println(separator);
    }

    // ---------------- bookings ----------------

    public static void printBookings(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("  No bookings found.");
            return;
        }
        String separator = separator(BOOKING_COLS);
        System.out.println(separator);
        System.out.printf(BOOKING_ROW, "Booking", "Guest", "Room", "Type",
                "Check-in", "Check-out", "Nights", "Guests", pad("Status", BOOKING_COLS[8]));
        System.out.println(separator);
        for (Booking booking : bookings) {
            System.out.printf(BOOKING_ROW, booking.getBookingId(),
                    fit(booking.getGuest().getFullName(), 20),
                    booking.getRoom().getRoomId(), booking.getRoom().getRoomType(),
                    booking.getCheckIn(), booking.getCheckOut(), booking.getNights(),
                    booking.getNumberOfGuests(),
                    Colors.forBookingStatus(booking.getStatus(),
                            pad(booking.getStatus(), BOOKING_COLS[8])));
        }
        System.out.println(separator);
    }

    /** One line used inside menus when the user picks a booking. */
    public static String describeBooking(Booking booking) {
        return String.format("%s  %s %s  %s to %s  (%s)", booking.getBookingId(),
                booking.getRoom().getRoomId(), booking.getRoom().getRoomType(),
                booking.getCheckIn(), booking.getCheckOut(), booking.getStatus());
    }

    // ---------------- invoices ----------------

    public static void printInvoiceHistory(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            System.out.println("  No invoices have been created yet.");
            return;
        }
        String separator = separator(INVOICE_COLS);
        System.out.println(separator);
        System.out.printf(INVOICE_ROW, "Invoice", "Date", "Booking", "Guest", "Room", "Payable (LKR)");
        System.out.println(separator);
        for (Invoice invoice : invoices) {
            Booking booking = invoice.getBooking();
            System.out.printf(INVOICE_ROW, invoice.getInvoiceId(), invoice.getIssuedOn(),
                    booking.getBookingId(), fit(booking.getGuest().getFullName(), 20),
                    booking.getRoom().getRoomId(),
                    MoneyFormatter.formatNumber(invoice.getFinalPayable()));
        }
        System.out.println(separator);
    }

    /** Prints the full invoice with all the details the brief asks for. */
    public static void printInvoice(Invoice invoice) {
        Booking booking = invoice.getBooking();
        String border = "+" + "-".repeat(INVOICE_WIDTH - 2) + "+";

        System.out.println();
        System.out.println(border);
        invoiceLine("SMARTSTAY HOTEL - INVOICE");
        System.out.println(border);
        invoiceField("Invoice No", invoice.getInvoiceId());
        invoiceField("Date", invoice.getIssuedOn().toString());
        invoiceField("Booking ID", booking.getBookingId());
        System.out.println(border);
        printInvoiceGuest(booking.getGuest());
        System.out.println(border);
        printInvoiceStay(booking, invoice.getExtraGuests());
        System.out.println(border);
        printInvoiceCharges(invoice);
        System.out.println(border);
    }

    private static void printInvoiceGuest(Guest guest) {
        invoiceField("Guest", guest.getFullName());
        invoiceField("NIC/Passport", guest.getNic());
        invoiceField("Phone", guest.getPhone());
        invoiceField("Email", guest.getEmail());
    }

    private static void printInvoiceStay(Booking booking, int extraGuests) {
        Room room = booking.getRoom();
        invoiceField("Room ID", room.getRoomId());
        invoiceField("Room type", room.getRoomType() + (room.includesBreakfast() ? " (breakfast incl.)" : ""));
        invoiceField("Bed type", room.getBedType().toString());
        invoiceField("Daily rate", MoneyFormatter.format(room.getDailyRate()));
        invoiceField("Check-in", booking.getCheckIn().toString());
        invoiceField("Check-out", booking.getCheckOut().toString());
        invoiceField("Nights", String.valueOf(booking.getNights()));
        invoiceField("Guests", booking.getNumberOfGuests() + " (room covers "
                + room.getCapacity() + ", extra " + extraGuests + ")");
    }

    /** The five bill steps, printed in the same order they are calculated. */
    private static void printInvoiceCharges(Invoice invoice) {
        invoiceAmount("1. Base charge", invoice.getBaseCharge());
        invoiceAmount("2. Extra guest charge", invoice.getExtraGuestCharge());
        invoiceAmount("3. Discount (12%, 5+ nights)", invoice.getDiscount().negate());
        invoiceAmount("4. Tax", invoice.getTax());
        invoiceAmount("   Total", invoice.getTotalAmount());
        invoiceAmount("5. Advance deducted", invoice.getAdvanceDeducted().negate());
        System.out.println("|" + "=".repeat(INVOICE_WIDTH - 2) + "|");
        // final amount in bold yellow, or green when money goes back to the guest
        boolean refund = invoice.getFinalPayable().signum() < 0;
        String finalLine = amountText("FINAL PAYABLE", invoice.getFinalPayable());
        System.out.println("| " + (refund ? Colors.green(finalLine) : Colors.highlight(finalLine)) + " |");
        if (refund) {
            invoiceLine("(negative = balance refunded to the guest)");
        }
    }

    private static void invoiceLine(String text) {
        System.out.printf("| %-" + (INVOICE_WIDTH - 4) + "s |%n", text);
    }

    private static void invoiceField(String label, String value) {
        invoiceLine(String.format("%-13s: %s", label, value));
    }

    /** Label on the left, money on the right, so all amounts line up. */
    private static void invoiceAmount(String label, BigDecimal amount) {
        invoiceLine(amountText(label, amount));
    }

    /** The amount line, padded to the invoice width (no colour yet). */
    private static String amountText(String label, BigDecimal amount) {
        String text = String.format("%-36s %20s", label, MoneyFormatter.format(amount));
        return pad(text, INVOICE_WIDTH - 4);
    }

    // ---------------- small helpers ----------------

    /** Builds "+--------+------+" from column widths (+2 for the space each side). */
    private static String separator(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append("-".repeat(width + 2)).append("+");
        }
        return line.toString();
    }

    /** Pads text with spaces on the right to the given width. */
    private static String pad(Object text, int width) {
        return String.format("%-" + width + "s", text);
    }

    /** Cuts text that is longer than the column and adds "..". */
    private static String fit(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, width - 2) + "..";
    }
}
