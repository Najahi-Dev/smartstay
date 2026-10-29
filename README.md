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
|   |-- ui/          Menu, ConsoleIO, ArrowKeyReader (JLine), Display, all screens
|   |-- util/        Validator, MoneyFormatter
|   `-- exception/   HotelException + ValidationException, BusinessRuleException,
|                    NotFoundException, GoBackException
|-- run.bat / run.sh               compile + run in one step
|-- ASSUMPTIONS.md  CLASS_LIST.md  TEST_CASES.md
```

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
    This is the worked invoice example, and it can be cancelled.
  - **BK-0002**: Kamala, R301 Suite, 2 guests, today to today+3. It cannot be cancelled
    because check-in is within 24 hours.
