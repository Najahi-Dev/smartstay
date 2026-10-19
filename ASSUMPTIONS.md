# Assumptions

## A. Assumptions given in the brief

| # | Assumption |
|---|------------|
| A1 | Room capacity (guests included in the daily rate): Standard 2, Deluxe 2, Suite 3, Family Suite 4. |
| A2 | Extra guest charge = extra guests x extra guest charge x nights. Extra guests = guests above capacity. |
| A3 | Nights = check-out date minus check-in date. |
| A4 | Booking IDs are made automatically: BK-0001, BK-0002, ... |
| A5 | Making a booking sets the room to **Reserved**. Cancelling it sets the room back to **Available** if no other active booking holds the room. **Under Maintenance** rooms can never be booked. |
| A6 | A date overlap check is always run as well, as a safety check. Two stays overlap if `newCheckIn < oldCheckOut` and `newCheckOut > oldCheckIn`. |
| A7 | A valid cancellation refunds the LKR 10,000 advance. If the stay goes ahead, the advance is deducted from the final bill. |
| A8 | "2 days in advance" means check-in date >= today + 2 days. |
| A9 | Dates are typed as `yyyy-MM-dd`. |
| A10 | Admin login is a hardcoded username and password (`admin` / `admin123`). |
| A11 | Guest login is the NIC/Passport number only (no password). |

## B. Extra assumptions made while building (please check)

| # | Assumption | Why |
|---|------------|-----|
| B1 | **Check-in time is 14:00.** The 24 hour rule counts back from 14:00 on the check-in date. Cancelling is blocked if less than 24 hours are left, or if check-in has already passed. | The brief says "24 hours" but dates have no time, so a time was needed. 14:00 is a common hotel check-in time. |
| B2 | **Up to 2 extra guests** per booking (max guests = capacity + 2). | Without a limit someone could book 50 people into a Standard room. |
| B3 | **Same-day turnover is allowed.** A stay ending 2026-12-10 does not overlap one starting 2026-12-10. | Matches the strict `<` / `>` overlap formula in the brief. |
| B4 | **Creating an invoice closes the booking.** The booking becomes *Completed* and the room goes back to *Available* (if no other active booking). The admin can create it at any time for a *Confirmed* booking, and only once per booking. | The brief has no check-in/check-out feature, so the invoice step stands for check-out. No date limit, so it can be demonstrated. |
| B5 | **Guest "Print Invoice"** shows the real invoice if the admin has made one. Otherwise it shows an **estimate** with the same calculation, which is not saved and does not change the booking. | Lets the guest see the cost before check-out without closing the booking. |
| B6 | **Final payable can be negative** (e.g. 1 night in a Standard room costs less than the 10,000 advance). The invoice then says the balance is refunded to the guest. | The advance is fixed, but some stays cost less than that. |
| B7 | **Money rounding:** each amount (base, extra, discount, tax) is rounded to 2 decimals, half up, as soon as it is worked out. `BigDecimal` is used, not `double`. | Avoids floating point errors like 6782.3999999. |
| B8 | **Discount** is exactly 12% of the base charge when nights >= 5. It does not apply to the extra guest charge. | As stated in the brief ("base room charge only"). |
| B9 | **Tax is not charged on the advance.** The advance is subtracted after tax. | Step 5 of the bill order in the brief. |
| B10 | **Update Room changes the bed type only.** The room type is the subclass and sets the whole price package, so to change the type you remove the room and add it again. | Keeps the polymorphism design clean. A Room object cannot change its own class. |
| B11 | **Change Room Status:** the admin can only choose Available or Under Maintenance. *Reserved* is set only by bookings. The status cannot be changed while the room has an active booking. | Stops a booked room from being marked Available (double booking) or Under Maintenance. |
| B12 | **Removing a guest** is allowed if they have no *active* (Confirmed) booking. Their cancelled or completed bookings and invoices stay in the history. | The brief only blocks guests with an active booking. |
| B13 | **"Search Reservation" in the guest menu** searches all bookings (by guest name or booking ID), as the brief lists it. | The brief says "by guest name". Searching only your own bookings by your own name would be pointless. |
| B14 | **"View by Check-in Date" in the guest menu** lists the logged-in guest's own bookings, sorted by check-in date (earliest first). The admin "View Bookings" shows all bookings sorted the same way. | My reading of the menu label. |
| B15 | **Modify reservation:** the brief mentions the 24 hour rule for modifying, but no menu has a "Modify" option. So only Cancel is built, and it uses the 24 hour rule. To change a booking, cancel it (if allowed) and book again. | Kept to the menus listed in the brief. |
| B16 | **Validation limits:** NIC/Passport 6-12 letters/digits (old NIC 10, new NIC 12, passports 8-9). Name 2-50 letters/spaces. Phone `0` + 9 digits. Room ID 2-6 letters/digits (e.g. R101). | "Sensible length" was not defined. |
| B17 | NIC/Passport and Room ID are stored in **upper case**, email in **lower case**, so `987654321v` and `987654321V` are the same guest. | Avoids near-duplicate records. |
| B18 | **Sample booking BK-0002 starts today.** It is added directly by the sample data loader (an "old" booking) so it skips the 2-day rule. All sample dates are counted from the day the program runs. | Needed to demonstrate the 24 hour cancel rule on any day. |
| B19 | Bed type does not change the price. | Not in the price table. |
| B20 | The admin gets 3 login attempts, then the program returns to the main menu. | A sensible limit. |
| B21 | Typing `#` in any form goes back to the menu without saving. | The brief asks for a Back option everywhere, and forms are not menus. |
