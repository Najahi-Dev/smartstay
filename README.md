# SmartStay Hotel Booking System

CI6115 Programming III coursework: a console (CLI) hotel booking system in plain Java.
All data is kept in memory (HashMap / ArrayList). Nothing is saved after the program closes.
Sample data is loaded each time it starts.

---

## Requirements

| What | Version |
|------|---------|
| Java JDK | 17 or newer (tested on JDK 27) |
| JLine | 3.26.3, already in `lib/jline-3.26.3.jar` |

JLine is the only extra library. It is used **only** for arrow-key menus.
If the jar is missing, or the terminal cannot handle arrow keys, the program switches
to numbered menus by itself.

## Project structure

```
smartstay/
|-- lib/jline-3.26.3.jar           JLine (arrow key menus)
|-- src/smartstay/
|   |-- SmartStayApp.java  main() - creates and connects all objects
|   |-- model/       Room (abstract) + 4 subclasses, RoomFactory, Guest, Booking, Invoice, enums
|   |-- service/     RoomManager, GuestManager, BookingManager, BillingManager, SampleDataLoader
|   |-- ui/          Menu, ConsoleIO, ArrowKeyReader (JLine), Colors, Display,
|   |                SharedForms, Admin*Screen, GuestScreen, MainScreen
|   |-- util/        Validator, MoneyFormatter
|   `-- exception/   HotelException + ValidationException, BusinessRuleException,
|                    NotFoundException, GoBackException
|-- uml/                           PlantUML class diagrams (domain + UI)
|-- run.bat / run.sh               compile + run in one step
|-- ASSUMPTIONS.md  CLASS_LIST.md  TEST_CASES.md
```

The `out/` folder (compiled classes) and the `Report_*.docx` files are in `.gitignore`,
so they are not uploaded to GitHub.

## Features

**Admin** (log in with username and password)

| Menu | Options |
|------|---------|
| Room Management | Add Room, Remove Room, Update Room (bed type), View Rooms, Change Room Status |
| Guest Management | Add Guest, Remove Guest, Update Guest, View Guests |
| Booking Management | Create Booking, View Bookings (sorted by check-in date) |
| Invoice Management | Create Invoice (closes the booking), View Invoice History |

**Guest** (register, or log in with NIC/Passport). The guest menu is grouped by category:

| Menu | Options |
|------|---------|
| Rooms | Search Room (by type and dates) |
| Reservations | Make Reservation, Search Reservation, View by Check-in Date, Modify Reservation, Cancel Reservation |
| Invoices | View Printed Invoice (only invoices the admin has already made) |

Main rules:
- Check-in must be at least 2 days from today.
- Every booking pays an advance of LKR 10,000, which is taken off the final bill.
- Reservations cannot be cancelled or changed within 24 hours of check-in (check-in time is 14:00).
- Bill order: base charge, extra guest charge, 12% discount on the base for 5+ nights,
  tax, then minus the advance.

See `ASSUMPTIONS.md` for every rule and choice that the brief did not spell out.

## Quick start

**Windows (Command Prompt):** `run.bat`
**Windows (PowerShell):** `.\run.bat`
**macOS / Linux:** `chmod +x run.sh` once, then `./run.sh`

Add `--numbered` to force numbered menus, e.g. `run.bat --numbered`.

## Compile and run by hand

Run these from the `smartstay` folder.

**Windows (PowerShell):**
```powershell
javac -cp lib\jline-3.26.3.jar -d out (Get-ChildItem -Recurse src\*.java).FullName
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jline-3.26.3.jar" smartstay.SmartStayApp
```

**Windows (Command Prompt):**
```bat
(for /r src %f in (*.java) do @echo "%f") > sources.txt
powershell -Command "(Get-Content sources.txt) -replace '\\','/' | Set-Content sources.txt"
javac -cp lib\jline-3.26.3.jar -d out @sources.txt
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jline-3.26.3.jar" smartstay.SmartStayApp
```

**macOS / Linux:**
```sh
javac -cp lib/jline-3.26.3.jar -d out $(find src -name "*.java")
java --enable-native-access=ALL-UNNAMED -cp "out:lib/jline-3.26.3.jar" smartstay.SmartStayApp
```

Notes:
- The classpath separator is `;` on Windows and `:` on macOS/Linux.
- `--enable-native-access=ALL-UNNAMED` only hides a warning that Java 22+ prints when JLine
  talks to the Windows console. If your Java does not accept it, just leave it out.
- If the JLine jar is not on the classpath, the program still runs with numbered menus.

## Which terminal to use

| Terminal | Menus |
|----------|-------|
| Windows Terminal, PowerShell window, Command Prompt | Arrow keys |
| macOS Terminal, iTerm2, Linux terminals | Arrow keys |
| IntelliJ / Eclipse / VS Code "Run" console | Numbered (automatic fallback) |
| Input piped from a file | Numbered (automatic fallback) |

For the arrow-key demo, run it from a real terminal window, not the IDE's Run console.
IntelliJ's built-in *Terminal* tab (not the Run tab) usually supports arrow keys.

### Colours

In arrow mode the program uses colour:

| What | Colour |
|------|--------|
| `[!]` error messages | Red |
| `[OK]` success messages | Green |
| Menu titles / selected menu option | Cyan |
| Booking summary title, Y/N questions, FINAL PAYABLE | Yellow |
| Hints and "Press ENTER to continue..." | Grey |
| Status in tables | Available / Confirmed = green, Reserved = yellow, Under Maintenance / Cancelled = red, Completed = grey |

Numbered mode (IDE console, piped input) stays plain text, so no odd codes like `[31m`
appear. To turn colour off in a normal terminal, set the `NO_COLOR` environment variable
(PowerShell: `$env:NO_COLOR = "1"`, macOS/Linux: `export NO_COLOR=1`).
The `[!]` and `[OK]` markers are always shown, with or without colour.

### Running from IntelliJ IDEA
1. Open the `smartstay` folder as a project. Mark `src` as *Sources Root*.
2. *File > Project Structure > Libraries > + > Java* and pick `lib/jline-3.26.3.jar`.
3. Run `smartstay.SmartStayApp`. The Run console has no arrow keys,
   so you will get numbered menus. That is expected.

## How to use

| Login | Details |
|-------|---------|
| Admin | username `admin`, password `admin123` |
| Guest | NIC/Passport of a registered guest, e.g. `200012345678`, `987654321V`, `N1234567` |

- **Arrow mode:** UP/DOWN to move, ENTER to select.
- **Numbered mode:** type the option number and press ENTER.
- In any form, type `#` and press ENTER to go back without saving.
- Dates are typed as `yyyy-MM-dd`, e.g. `2026-12-25`.
- Every menu ends with a Back option (Exit on the main menu).

### Sample data (loaded at start)
- 10 rooms: R101-R103 Standard (R103 Under Maintenance), R201-R203 Deluxe,
  R301-R302 Suite, R401-R402 Family Suite.
- 3 guests: Nimal Perera (200012345678), Kamala Silva (987654321V), Sarah Johnson (N1234567).
- 2 bookings, with dates counted from the day you run the program:
  - **BK-0001**: Nimal, R201 Deluxe, 3 guests, today+10 to today+16 (6 nights).
    This is the worked invoice example (final payable LKR 53,302.40), and it can be
    changed or cancelled.
  - **BK-0002**: Kamala, R301 Suite, 2 guests, today to today+3. It cannot be changed
    or cancelled because check-in is within 24 hours.

Anything you add (rooms, guests, bookings, invoices) is lost when the program closes,
and the sample data above is loaded again on the next start.

### Quick demo path
1. **Guest > Login** `987654321V` > **Reservations > Cancel Reservation** > BK-0002
   (shows the 24 hour rule message).
2. **Admin** > **Invoice Management > Create Invoice** > `BK-0001` (prints the invoice,
   final payable LKR 53,302.40).
3. **Guest > Login** `200012345678` > **Invoices > View Printed Invoice** (shows INV-0001).
4. **Guest > Login** `N1234567` > **Reservations > Make Reservation**, then
   **Modify Reservation** on the new booking to change its dates or room.
