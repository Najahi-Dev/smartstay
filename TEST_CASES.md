# Test Cases - SmartStay Hotel Booking System

**How to read the dates:** `T` = the date you run the test. `T+2` = two days later, typed as `yyyy-MM-dd`
(for example if T is 2026-10-19 then T+2 is 2026-10-21). Sample dates are worked out from the run date,
so the tests work on any day.

**Start state for every test (restart the program between groups if needed):** sample data loaded.
- Rooms R101-R103 Standard (**R103 Under Maintenance**), R201-R203 Deluxe, R301-R302 Suite, R401-R402 Family Suite.
- Guests: 200012345678 Nimal Perera, 987654321V Kamala Silva, N1234567 Sarah Johnson.
- **BK-0001**: Nimal, R201 Deluxe, 3 guests, T+10 to T+16 (6 nights). R201 is Reserved.
- **BK-0002**: Kamala, R301 Suite, 2 guests, T to T+3. R301 is Reserved.
- Admin login: `admin` / `admin123`.

---

## 1. Menus and general input

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-01 | Main menu | Valid menu choice (numbered mode) | `1` | Admin Login screen opens | | |
| TC-02 | Main menu | Letters instead of a number | `abc` | `'abc' is not a valid choice. Enter a number from 1 to 3.` Asks again, no crash | | |
| TC-03 | Main menu | Number out of range | `9` | Same message as TC-02, asks again | | |
| TC-04 | Main menu | Empty input | ENTER only | `'' is not a valid choice...`, asks again | | |
| TC-05 | Arrow menus | Arrow keys in Windows Terminal / PowerShell | DOWN, DOWN, UP, ENTER | Highlight moves down twice, up once; option 2 (Guest) opens. Hint line shown under the options | | |
| TC-06 | Fallback | Run inside the IDE Run console (or with `--numbered`) | start program | `Numbered menus are on...` shown; menus show numbers and `Hint: type a number and press ENTER.` | | |
| TC-07 | Back in form | Leave a form part way | Admin > Room Mgmt > Add Room, type `#` | `Cancelled - nothing was changed.`, back to Room Management menu | | |
| TC-08 | Back option | Every menu has Back | Open each menu | Last option is Back (Exit on the main menu) | | |
| TC-09 | Robustness | End of input | Press Ctrl+Z then ENTER (Windows) / Ctrl+D (Mac/Linux) at a prompt | `Input closed. Goodbye!`, program ends without an error trace | | |

## 2. Admin login

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-10 | Admin login | Valid login | `admin` / `admin123` | `[OK] Welcome, admin.` Admin Menu shown | | |
| TC-11 | Admin login | Wrong password once | `admin` / `abc` | `Wrong username or password. 2 attempt(s) left.` | | |
| TC-12 | Admin login | Three wrong tries | 3 x wrong password | `Too many failed attempts. Returning to the main menu.` | | |

## 3. Guest registration and login

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-13 | Register | Valid details | NIC `200199912345`, name `Ruwan Fernando`, phone `0779998887`, email `ruwan@example.com` | `[OK] Guest Ruwan Fernando registered...`, Guest Menu opens | | |
| TC-14 | Register | Duplicate NIC | NIC `987654321V` | `A guest with NIC/Passport 987654321V is already registered.` Asks for NIC again | | |
| TC-15 | Register | Duplicate NIC, different case | NIC `987654321v` | Same as TC-14 (NIC stored in upper case) | | |
| TC-16 | Register | NIC with space/symbol | `12 34`, `1234-567` | `NIC/Passport can only contain letters and digits (no spaces or symbols).` | | |
| TC-17 | Register | NIC too short | `AB12` | `NIC/Passport must be 6 to 12 characters long.` | | |
| TC-18 | Register | Empty NIC | ENTER only | `NIC/Passport cannot be empty.` | | |
| TC-19 | Register | Name with digit | `Nimal2` | `Name can only contain letters and spaces.` | | |
| TC-20 | Register | Phone 9 digits | `077123456` | `Phone number must be 10 digits and start with 0, for example 0771234567.` | | |
| TC-21 | Register | Phone not starting with 0 | `7712345678` | Same as TC-20 | | |
| TC-22 | Register | Bad email | `bad@mail` | `Email must look like name@example.com.` | | |
| TC-23 | Guest login | Valid NIC (lower case) | `987654321v` | `[OK] Welcome, Kamala Silva.` | | |
| TC-24 | Guest login | Unknown NIC | `X9999999` | `No guest found with NIC/Passport X9999999.` | | |

## 4. Room management (Admin)

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-25 | View rooms | List all rooms | View Rooms | Table of 10 rooms in ID order; R103 Under Maintenance, R201 and R301 Reserved; rates as `6,000.00` etc. | | |
| TC-26 | Add room | Valid new room | ID `R105`, Deluxe, King | `[OK] Room added: R105 Deluxe King LKR 9,000.00/night...` and status Available | | |
| TC-27 | Add room | Duplicate Room ID | `R101` | `Room ID R101 already exists.` Asks again | | |
| TC-28 | Add room | Bad Room ID | `R-1` | `Room ID must be 2 to 6 letters or digits, for example R101.` | | |
| TC-29 | Update room | Change bed type | `R102`, choose King | `[OK] Room R102 now has a King bed.` View Rooms shows King | | |
| TC-30 | Remove room | Room with no booking | `R102`, `Y` | `[OK] Room R102 removed.` Not in View Rooms | | |
| TC-31 | Remove room | Room with an active booking | `R201`, `Y` | `Room R201 has an active booking and cannot be removed.` | | |
| TC-32 | Remove room | Say No at confirm | `R101`, `N` | `Room was not removed.` | | |
| TC-33 | Remove room | Unknown room | `R999` | `No room found with ID R999.` | | |
| TC-34 | Room status | Maintenance -> Available | `R103`, Available | `[OK] Room R103 is now Available.` | | |
| TC-35 | Room status | Room with active booking | `R301`, Under Maintenance | `Room R301 has an active booking. Cancel or complete the booking first.` | | |

## 5. Guest management (Admin)

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-36 | View guests | List guests | View Guests | 3 guests sorted by name | | |
| TC-37 | Add guest | Same form as register | valid details | Guest added | | |
| TC-38 | Update guest | Change phone only | NIC `N1234567`, ENTER, `0701234567`, ENTER | `[OK] Guest N1234567 updated.` Only phone changed | | |
| TC-39 | Update guest | Invalid value | phone `123` | Phone error message, asks again | | |
| TC-40 | Remove guest | Guest with active booking | `200012345678`, `Y` | `Guest 200012345678 has an active booking and cannot be removed.` | | |
| TC-41 | Remove guest | Guest without bookings | `N1234567`, `Y` | `[OK] Guest Sarah Johnson removed.` | | |

## 6. Room search

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-42 | Search room | Rooms available | Standard, T+2 to T+5 | R101 and R102 listed. **R103 not listed** (Under Maintenance) | | |
| TC-43 | Search room | Overlapping dates | Deluxe, T+12 to T+14 (inside BK-0001) | R202 and R203 listed, **R201 not listed** | | |
| TC-44 | Search room | None available | First book R302 (Suite) for any dates, then search Suite, T+2 to T+4 | `No Suite rooms are available from ... to ....` | | |
| TC-45 | Search room | Bad date format | `2026/12/01` | `'2026/12/01' is not a valid date. Use yyyy-MM-dd, for example 2026-12-25.` Asks again | | |
| TC-46 | Search room | Date that does not exist | `2027-02-30` | Same "not a valid date" message | | |

## 7. Booking

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-47 | Booking | Exactly 2 days ahead | Guest Kamala > Make Reservation: Standard, T+2 to T+4, R101, 2 guests, `Y` | `Booking BK-0003 confirmed. Advance of LKR 10,000.00 charged.` R101 now Reserved | | |
| TC-48 | Booking | 1 day ahead | check-in T+1 | `Check-in must be at least 2 days from today (earliest: T+2).` Asks again | | |
| TC-49 | Booking | Check-in today / in the past | check-in T or T-5 | Same message as TC-48 | | |
| TC-50 | Booking | Check-out before check-in | in T+5, out T+3 | `Check-out must be after check-in (T+5).` Asks again | | |
| TC-51 | Booking | Check-out same day as check-in | in T+5, out T+5 | Same as TC-50 | | |
| TC-52 | Booking | Reserved room | Suite, T+20 to T+22 | R301 (Reserved) is **not offered**, only R302 | | |
| TC-53 | Booking | Under Maintenance room | Standard, T+2 to T+4 | R103 is **not offered** | | |
| TC-54 | Booking | Too many guests | Deluxe room, guests `5` | `Please enter a number from 1 to 4.` Asks again | | |
| TC-55 | Booking | Guests = 0 / letters | `0`, `two` | Range message / `'two' is not a whole number.` | | |
| TC-56 | Booking | Say No at confirm | ... `N` | `Booking not made.` No new booking in View Bookings | | |
| TC-57 | Booking | Admin booking for unknown guest | Admin > Create Booking, NIC `X9999999` | `No guest found with NIC/Passport X9999999.` | | |
| TC-58 | Booking | Admin booking for a guest | Admin > Create Booking, NIC `N1234567`, Family Suite, T+3 to T+5, R401, 4 guests, `Y` | Booking confirmed for Sarah Johnson | | |

## 8. Cancellation

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-59 | Cancel | More than 24 h before check-in | Login `200012345678` > Cancel > BK-0001 > `Y` | `Reservation BK-0001 cancelled. Your advance of LKR 10,000.00 will be refunded.` R201 back to Available | | |
| TC-60 | Cancel | Within 24 h of check-in | Login `987654321V` > Cancel > BK-0002 | `Reservations cannot be cancelled within 24 hours of check-in (check-in is T at 14:00).` Booking stays Confirmed | | |
| TC-61 | Cancel | Say No at confirm | BK-0001, `N` | `Reservation kept.` | | |
| TC-62 | Cancel | Guest has no active booking | Login `N1234567` > Cancel | `You have no reservations for this option.` | | |

## 9. Invoices

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-63 | Invoice | **Worked example**: Deluxe, 6 nights, 3 guests | Admin > Invoice > Create, `BK-0001`, `Y` | Base **54,000.00**, Extra guest **9,000.00**, Discount **-6,480.00**, Tax **6,782.40**, Total **63,302.40**, Advance **-10,000.00**, Final payable **LKR 53,302.40**. BK-0001 becomes Completed, R201 Available | | |
| TC-64 | Invoice | 4 nights, no discount | Book Deluxe T+3 to T+7, 2 guests, then create invoice | Base 36,000.00, Extra 0.00, Discount 0.00, Tax 4,320.00, Total 40,320.00, Final 30,320.00 | | |
| TC-65 | Invoice | Exactly 5 nights, discount | Book Deluxe T+3 to T+8, 2 guests, then create invoice | Base 45,000.00, Extra 0.00, Discount -5,400.00, Tax 4,752.00, Total 44,352.00, Final 34,352.00 | | |
| TC-66 | Invoice | With extra guest | Book Standard T+2 to T+4, **3** guests, then create invoice | Base 12,000.00, Extra 2,000.00 (1 x 1,000 x 2), Discount 0.00, Tax 1,400.00, Total 15,400.00, Final 5,400.00 | | |
| TC-67 | Invoice | Without extra guest | Book Standard T+2 to T+4, **2** guests, then create invoice | Base 12,000.00, Extra 0.00, Tax 1,200.00, Total 13,200.00, Final 3,200.00 | | |
| TC-68 | Invoice | Total below the advance | Book Standard T+2 to T+3, 1 guest, then create invoice | Total 6,600.00, Final **-3,400.00** and `(negative = balance refunded to the guest)` | | |
| TC-69 | Invoice | Same booking twice | Create invoice for `BK-0001` again | `Invoice INV-0001 already exists for booking BK-0001.` | | |
| TC-70 | Invoice | Cancelled booking | Cancel a booking, then create its invoice | `Booking BK-000X is Cancelled, so no invoice can be created.` | | |
| TC-71 | Invoice | Unknown booking | `BK-9999` | `No booking found with ID BK-9999.` | | |
| TC-72 | Invoice | Invoice history | View Invoice History | Table with invoice no., date, booking, guest, room, payable, in issue order | | |
| TC-73 | Invoice | Guest prints before final invoice | Login `987654321V` > Print Invoice > BK-0002 | `SMARTSTAY HOTEL - ESTIMATED BILL`: Base 42,000.00, Tax 6,300.00, Total 48,300.00, Final 38,300.00 | | |
| TC-74 | Invoice | Guest prints after final invoice | After TC-63, login `200012345678` > Print Invoice > BK-0001 | Shows `SMARTSTAY HOTEL - INVOICE` with `INV-0001` | | |
| TC-75 | Invoice | All required fields shown | any invoice | Booking ID, guest name/NIC/phone/email, room ID/type/bed/daily rate, check-in, check-out, nights, guests, base, extra, discount, tax, advance, final payable | | |

## 10. Searching and sorting bookings

| Test ID | Feature | Description | Input | Expected result | Actual result | Pass/Fail |
|---|---|---|---|---|---|---|
| TC-76 | Search reservation | By partial name | Search > By guest name > `per` | BK-0001 (Nimal **Per**era) | | |
| TC-77 | Search reservation | Name in different case | `KAMALA` | BK-0002 (case insensitive) | | |
| TC-78 | Search reservation | No match | `zzz` | `No bookings found.` | | |
| TC-79 | Search reservation | By booking ID | `BK-0002` | BK-0002 shown | | |
| TC-80 | Search reservation | By booking ID, lower case | `bk-0002` | BK-0002 shown | | |
| TC-81 | Search reservation | Unknown booking ID | `BK-9999` | `No booking found with ID BK-9999.` | | |
| TC-82 | Sort | Admin View Bookings sorted by check-in | After TC-47 (BK-0003 at T+2) | Order: BK-0002 (T), BK-0003 (T+2), BK-0001 (T+10) | | |
| TC-83 | Sort | Guest View by Check-in Date | Login `987654321V` after TC-47 | BK-0002 then BK-0003, only Kamala's bookings | | |
