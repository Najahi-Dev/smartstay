# Assumptions

## A. Rules taken directly from the brief

| # | Rule | Brief wording |
|---|------|---------------|
| A1 | The extra guest charge applies only to guests above the room capacity. Extra guests = number of guests minus capacity (never below 0). | Bill step 2: "Add extra guest charges if the number of guests exceeds the room capacity." |
| A2 | Only **Available** rooms can be booked, so **Reserved** and **Under Maintenance** rooms can never be booked. | "Reservations cannot be made if the room's availability status is not Available." |
| A3 | The LKR 10,000 advance is refunded when an eligible reservation is cancelled. If the stay goes ahead, it is deducted from the final bill. | "A refundable booking advance of LKR 10,000..." and bill step 5: "Deduct the refundable booking advance." |

## B. Assumptions made while building (please check)

| # | Assumption | Why |
|---|------------|-----|
| B1 | **Room capacity** (guests included in the daily rate): Standard 2, Deluxe 2, Suite 3, Family Suite 4. | Bill step 2 depends on "room capacity" but the brief gives no numbers. These follow the size of each room type. |
| B2 | **Extra guest charge is per night:** extra guests x extra guest charge x nights. | Table 1 says "per guest" but not per night or per stay. The room rate is per night and an extra guest stays every night, so it is charged the same way. |
| B3 | **Nights = check-out date minus check-in date.** | The brief uses "Number of Nights" without defining it. This is how hotels count nights. |
| B4 | **Booking IDs are made automatically:** BK-0001, BK-0002, ... | The brief needs a booking ID (search by booking ID) but does not say how it is made. Automatic IDs are always unique. |
| B5 | **Making a booking sets the room to Reserved.** Cancelling it (or creating its invoice, see B14) sets the room back to **Available** if no other active booking holds the room. | The brief lists Reserved as a status but does not say when it is set. |
| B6 | **A date overlap check is always run as well**, as a safety check. Two stays overlap if `newCheckIn < oldCheckOut` and `newCheckOut > oldCheckIn`. | The brief does not say how dates are compared. This stops a double booking even if a room's status were wrong. |
| B7 | **"2 days in advance" means check-in date >= today + 2 days.** | The brief does not say whether today counts. |
| B8 | **Dates are typed as `yyyy-MM-dd`**, e.g. 2026-12-25. | The brief gives no date format. This is the international standard (ISO 8601) and Java's default. |
| B9 | **Admin login is a hardcoded username and password** (`admin` / `admin123`). | The brief has administrator functions but no admin accounts. A login stops guests from using the admin menu, and there is no file or database storage for accounts. |
| B10 | **Guest login is the NIC/Passport number only** (no password). | Registration in the brief does not include a password, and the NIC/Passport is unique to each guest. |
| B11 | **Check-in time is 14:00.** The 24 hour rule counts back from 14:00 on the check-in date. Cancelling is blocked if less than 24 hours are left, or if check-in has already passed. | The brief says "24 hours" but dates have no time, so a time was needed. 14:00 is a common hotel check-in time. |
| B12 | **Up to 2 extra guests** per booking (max guests = capacity + 2). | Without a limit someone could book 50 people into a Standard room. |
| B13 | **Same-day turnover is allowed.** A stay ending 2026-12-10 does not overlap one starting 2026-12-10. | Follows from the strict `<` / `>` overlap check in B6. |
| B14 | **Creating an invoice closes the booking.** The booking becomes *Completed* and the room goes back to *Available* (if no other active booking). The admin can create it at any time for a *Confirmed* booking, and only once per booking. | The brief has no check-in/check-out feature, so the invoice step stands for check-out. No date limit, so it can be demonstrated. |
| B15 | **Guest "Print Invoice"** shows the real invoice if the admin has made one. Otherwise it shows an **estimate** with the same calculation, which is not saved and does not change the booking. | Lets the guest see the cost before check-out without closing the booking. |
| B16 | **Final payable can be negative** (e.g. 1 night in a Standard room costs less than the 10,000 advance). The invoice then says the balance is refunded to the guest. | The advance is fixed, but some stays cost less than that. |
| B17 | **Money rounding:** each amount (base, extra, discount, tax) is rounded to 2 decimals, half up, as soon as it is worked out. `BigDecimal` is used, not `double`. | Avoids floating point errors like 6782.3999999. |
| B18 | **Discount** is exactly 12% of the base charge when nights >= 5. It does not apply to the extra guest charge. | As stated in the brief ("base room charge only"). |
| B19 | **Tax is not charged on the advance.** The advance is subtracted after tax. | Step 5 of the bill order in the brief. |
| B20 | **Update Room changes the bed type only.** The room type is the subclass and sets the whole price package, so to change the type you remove the room and add it again. | Keeps the polymorphism design clean. A Room object cannot change its own class. |
| B21 | **Change Room Status:** the admin can only choose Available or Under Maintenance. *Reserved* is set only by bookings. The status cannot be changed while the room has an active booking. | Stops a booked room from being marked Available (double booking) or Under Maintenance. |
| B22 | **Removing a guest** is allowed if they have no *active* (Confirmed) booking. Their cancelled or completed bookings and invoices stay in the history. | The brief only blocks guests with an active booking. |
| B23 | **"Search Reservation" in the guest menu** searches all bookings (by guest name or booking ID), as the brief lists it. | The brief says "by guest name". Searching only your own bookings by your own name would be pointless. |
| B24 | **"View by Check-in Date" in the guest menu** lists the logged-in guest's own bookings, sorted by check-in date (earliest first). The admin "View Bookings" shows all bookings sorted the same way. | My reading of the menu label. |
| B25 | **Modify reservation:** the guest menu has a "Modify Reservation" option. A guest can change the room type, room, dates and number of guests of an active booking. The booking keeps its ID and the advance already paid. The same rules as a new booking apply (2 days ahead, Available rooms only, guest limit, overlap check), and changes are blocked within 24 hours of check-in. If the room changes, the old room goes back to Available. | The brief says reservations cannot be modified within 24 hours of check-in, so modifying is allowed before that. No menu in the brief lists it, so it was added to the guest menu next to Cancel. |
| B26 | **Validation limits:** NIC/Passport 6-12 letters/digits (old NIC 10, new NIC 12, passports 8-9). Name 2-50 letters/spaces. Phone `0` + 9 digits. Room ID 2-6 letters/digits (e.g. R101). | "Sensible length" was not defined. |
| B27 | NIC/Passport and Room ID are stored in **upper case**, email in **lower case**, so `987654321v` and `987654321V` are the same guest. | Avoids near-duplicate records. |
| B28 | **Sample booking BK-0002 starts today.** It is added directly by the sample data loader (an "old" booking) so it skips the 2-day rule. All sample dates are counted from the day the program runs. | Needed to demonstrate the 24 hour cancel rule on any day. |
| B29 | Bed type does not change the price. | Not in the price table. |
| B30 | The admin gets 3 login attempts, then the program returns to the main menu. | A sensible limit. |
| B31 | Typing `#` in any form goes back to the menu without saving. | The brief asks for a Back option everywhere, and forms are not menus. |


claude --resume 695fd1a6-eae5-428a-a623-bf25c1d74291