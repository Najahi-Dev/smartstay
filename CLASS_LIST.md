# Class List for the UML Class Diagram

Visibility: `+` public, `-` private, `#` protected, `~` package-private.
*Italic* = abstract. `{static}` = static, `{final}` = constant / cannot change.

---

## Package `smartstay.model`

### «enumeration» RoomType
- Values: `STANDARD, DELUXE, SUITE, FAMILY_SUITE`
- `- label : String`
- `+ getLabel() : String`, `+ toString() : String`

### «enumeration» BedType
- Values: `SINGLE, DOUBLE, QUEEN, KING`
- `- label : String`
- `+ getLabel() : String`, `+ toString() : String`

### «enumeration» RoomStatus
- Values: `AVAILABLE, RESERVED, UNDER_MAINTENANCE`
- `- label : String`
- `+ getLabel() : String`, `+ toString() : String`

### «enumeration» BookingStatus
- Values: `CONFIRMED, CANCELLED, COMPLETED`
- `- label : String`
- `+ getLabel() : String`, `+ toString() : String`

### *Room* (abstract)
| Fields | Methods |
|--------|---------|
| `- roomId : String {final}` | `# Room(roomId : String, bedType : BedType)` |
| `- bedType : BedType` | *`+ getRoomType() : RoomType`* |
| `- status : RoomStatus` | *`+ getDailyRate() : BigDecimal`* |
| | *`+ includesBreakfast() : boolean`* |
| | *`+ getExtraGuestCharge() : BigDecimal`* |
| | *`+ getTaxRate() : BigDecimal`* |
| | *`+ getCapacity() : int`* |
| | `+ getRoomId() : String` |
| | `+ getBedType() : BedType` / `+ setBedType(BedType) : void` |
| | `+ getStatus() : RoomStatus` / `+ setStatus(RoomStatus) : void` |
| | `+ isAvailable() : boolean` |

### StandardRoom / DeluxeRoom / SuiteRoom / FamilySuiteRoom (each `extends Room`)
All four have the same members, only the values differ:
| Fields | Methods |
|--------|---------|
| `- DAILY_RATE : BigDecimal {static, final}` | `+ <ClassName>(roomId : String, bedType : BedType)` |
| `- EXTRA_GUEST_CHARGE : BigDecimal {static, final}` | `+ getRoomType() : RoomType` (overrides) |
| `- TAX_RATE : BigDecimal {static, final}` | `+ getDailyRate() : BigDecimal` (overrides) |
| `- CAPACITY : int {static, final}` | `+ includesBreakfast() : boolean` (overrides) |
| | `+ getExtraGuestCharge() : BigDecimal` (overrides) |
| | `+ getTaxRate() : BigDecimal` (overrides) |
| | `+ getCapacity() : int` (overrides) |

| Class | Rate | Breakfast | Extra/guest | Tax | Capacity |
|-------|------|-----------|-------------|-----|----------|
| StandardRoom | 6,000 | No | 1,000 | 0.10 | 2 |
| DeluxeRoom | 9,000 | Yes | 1,500 | 0.12 | 2 |
| SuiteRoom | 14,000 | Yes | 2,000 | 0.15 | 3 |
| FamilySuiteRoom | 18,000 | Yes | 2,500 | 0.15 | 4 |

### RoomFactory «Factory» (final)
- `- RoomFactory()` (private constructor)
- `+ createRoom(type : RoomType, roomId : String, bedType : BedType) : Room {static}`

### Guest
| Fields | Methods |
|--------|---------|
| `- nic : String {final}` | `+ Guest(nic, fullName, phone, email : String)` |
| `- fullName : String` | `+ getNic() : String` |
| `- phone : String` | `+ getFullName()` / `+ setFullName(String)` |
| `- email : String` | `+ getPhone()` / `+ setPhone(String)` |
| | `+ getEmail()` / `+ setEmail(String)` |

### Booking
| Fields | Methods |
|--------|---------|
| `+ ADVANCE_AMOUNT : BigDecimal {static, final} = 10000.00` | `+ Booking(bookingId : String, guest : Guest, room : Room, numberOfGuests : int, checkIn : LocalDate, checkOut : LocalDate)` |
| `- bookingId : String {final}` | `+ getNights() : long` |
| `- guest : Guest {final}` | `+ overlaps(newCheckIn : LocalDate, newCheckOut : LocalDate) : boolean` |
| `- room : Room {final}` | `+ isActive() : boolean` |
| `- numberOfGuests : int {final}` | `+ cancel() : void` |
| `- checkIn : LocalDate {final}` | `+ complete() : void` |
| `- checkOut : LocalDate {final}` | getters for every field |
| `- status : BookingStatus` | |

### Invoice
| Fields | Methods |
|--------|---------|
| `- invoiceId : String {final}` | `+ Invoice(invoiceId, booking, issuedOn, extraGuests, baseCharge, extraGuestCharge, discount, tax, totalAmount, advanceDeducted, finalPayable)` |
| `- booking : Booking {final}` | getters for every field (no setters, read-only) |
| `- issuedOn : LocalDate {final}` | |
| `- extraGuests : int {final}` | |
| `- baseCharge : BigDecimal {final}` | |
| `- extraGuestCharge : BigDecimal {final}` | |
| `- discount : BigDecimal {final}` | |
| `- tax : BigDecimal {final}` | |
| `- totalAmount : BigDecimal {final}` | |
| `- advanceDeducted : BigDecimal {final}` | |
| `- finalPayable : BigDecimal {final}` | |

---

## Package `smartstay.service`

### BookingManager
| Fields | Methods |
|--------|---------|
| `+ MAX_EXTRA_GUESTS : int {static, final} = 2` | `+ createBooking(guest : Guest, room : Room, numberOfGuests : int, checkIn : LocalDate, checkOut : LocalDate) : Booking` |
| `+ CHECK_IN_TIME : LocalTime {static, final} = 14:00` | `~ addSampleBooking(guest, room, numberOfGuests, checkIn, checkOut) : Booking` |
| `- NO_CHANGE_HOURS : long {static, final} = 24` | `+ cancelBooking(bookingId : String) : Booking` |
| `- BY_CHECK_IN : Comparator<Booking> {static, final}` | `+ checkCanCancel(booking : Booking) : void` |
| `- bookings : Map<String, Booking>` (HashMap) | `+ completeBooking(booking : Booking) : void` |
| `- lastBookingNumber : int` | `+ findBooking(bookingId : String) : Booking` |
| | `+ searchByGuestName(text : String) : List<Booking>` |
| | `+ getAllBookingsSorted() : List<Booking>` |
| | `+ getBookingsForGuest(nic : String) : List<Booking>` |
| | `+ isRoomFreeForDates(room : Room, checkIn : LocalDate, checkOut : LocalDate) : boolean` |
| | `+ hasActiveBookingForRoom(roomId : String) : boolean` |
| | `+ hasActiveBookingForGuest(nic : String) : boolean` |
| | `- checkOutsideNoChangeWindow(booking : Booking, now : LocalDateTime) : void` |
| | `- storeBooking(...) : Booking`, `- nextBookingId() : String` |
| | `- checkRoomCanBeBooked(room : Room) : void`, `- checkGuestCount(room : Room, numberOfGuests : int) : void` |
| | `- releaseRoomIfFree(room : Room) : void` |

### RoomManager
| Fields | Methods |
|--------|---------|
| `- rooms : Map<String, Room>` (HashMap) | `+ RoomManager(bookingManager : BookingManager)` |
| `- bookingManager : BookingManager {final}` | `+ addRoom(roomId : String, type : RoomType, bedType : BedType) : Room` |
| | `+ removeRoom(roomId : String) : void` |
| | `+ updateBedType(roomId : String, bedType : BedType) : void` |
| | `+ changeStatus(roomId : String, newStatus : RoomStatus) : void` |
| | `+ roomExists(roomId : String) : boolean` |
| | `+ findRoom(roomId : String) : Room` |
| | `+ getAllRooms() : List<Room>` |
| | `+ findAvailableRooms(type : RoomType, checkIn : LocalDate, checkOut : LocalDate) : List<Room>` |

### GuestManager
| Fields | Methods |
|--------|---------|
| `- guests : Map<String, Guest>` (HashMap) | `+ GuestManager(bookingManager : BookingManager)` |
| `- bookingManager : BookingManager {final}` | `+ registerGuest(nic, fullName, phone, email : String) : Guest` |
| | `+ updateGuest(nic, fullName, phone, email : String) : void` |
| | `+ removeGuest(nic : String) : void` |
| | `+ isRegistered(nic : String) : boolean` |
| | `+ findGuest(nic : String) : Guest` |
| | `+ getAllGuests() : List<Guest>` |

### BillingManager
| Fields | Methods |
|--------|---------|
| `+ DISCOUNT_MIN_NIGHTS : int {static, final} = 5` | `+ BillingManager(bookingManager : BookingManager)` |
| `+ DISCOUNT_RATE : BigDecimal {static, final} = 0.12` | `+ createInvoice(bookingId : String) : Invoice` |
| `+ ESTIMATE_ID : String {static, final}` | `+ estimateInvoice(booking : Booking) : Invoice` |
| `- invoices : List<Invoice>` (ArrayList) | `+ findInvoiceForBooking(bookingId : String) : Optional<Invoice>` |
| `- bookingManager : BookingManager {final}` | `+ getInvoiceHistory() : List<Invoice>` |
| `- lastInvoiceNumber : int` | `- calculateInvoice(booking : Booking, invoiceId : String) : Invoice` |
| | `- money(value : BigDecimal) : BigDecimal`, `- nextInvoiceId() : String` |

### SampleDataLoader (final, static only)
- `+ load(roomManager, guestManager, bookingManager) : void {static}`
- `- loadRooms(...)`, `- loadGuests(...)`, `- loadBookings(...)` `{static}`

---

## Package `smartstay.util`

### Validator (final, static only)
| Fields | Methods (all `{static}`) |
|--------|---------|
| `+ MIN_DAYS_IN_ADVANCE : int = 2` | `+ validateNic(raw : String) : String` |
| `- NIC_PATTERN, NAME_PATTERN, PHONE_PATTERN, EMAIL_PATTERN, ROOM_ID_PATTERN : Pattern` | `+ validateName(raw : String) : String` |
| `- DATE_FORMAT : DateTimeFormatter` | `+ validatePhone(raw : String) : String` |
| | `+ validateEmail(raw : String) : String` |
| | `+ validateRoomId(raw : String) : String` |
| | `+ parseDate(raw : String) : LocalDate` |
| | `+ parseNumber(raw : String, min : int, max : int) : int` |
| | `+ checkStayDates(checkIn, checkOut, today : LocalDate) : void` |
| | `+ checkCheckInDate(checkIn, today : LocalDate) : void` |
| | `- requireNotEmpty(raw : String, fieldName : String) : String` |

### MoneyFormatter (final, static only)
- `- FORMAT : DecimalFormat {static, final}`
- `+ format(amount : BigDecimal) : String` -> "LKR 54,000.00"
- `+ formatNumber(amount : BigDecimal) : String` -> "54,000.00"

---

## Package `smartstay.exception`

| Class | Extends | Notes |
|-------|---------|-------|
| HotelException | `Exception` | checked, parent of the three below |
| ValidationException | HotelException | bad format / out of range input |
| BusinessRuleException | HotelException | a hotel rule was broken |
| NotFoundException | HotelException | unknown Room ID / NIC / Booking ID |
| GoBackException | `RuntimeException` | user typed `#` in a form |

Each has one constructor `+ <Name>(message : String)` (GoBackException has a no-argument constructor).

---

## Package `smartstay.ui`

### Menu
| Fields | Methods |
|--------|---------|
| `- io : ConsoleIO {final}` | `+ Menu(io : ConsoleIO, title : String, options : List<String>)` |
| `- title : String {final}` | `+ show() : int` |
| `- options : List<String> {final}` | `+ isLast(choice : int) : boolean` |
| | `- showNumbered() : int` |

### ConsoleIO
| Fields | Methods |
|--------|---------|
| `+ GO_BACK_KEY : String {static, final} = "#"` | `+ ConsoleIO(forceNumbered : boolean)` |
| `- arrowReader : ArrowKeyReader {final}` (null in numbered mode) | `+ isArrowMode() : boolean`, `~ getArrowReader() : ArrowKeyReader` |
| `- plainReader : BufferedReader {final}` | `+ readLine(prompt) : String`, `+ readText(prompt) : String` |
| | `+ readValid(prompt, check : TextCheck) : String` |
| | `+ readValidOrKeep(prompt, currentValue, check : TextCheck) : String` |
| | `+ readNumber(prompt, min, max) : int`, `+ readDate(prompt) : LocalDate` |
| | `+ readPassword(prompt) : String`, `+ confirm(question) : boolean`, `+ pause() : void` |
| | `+ runAction(action : ScreenAction) : void` |
| | `+ printError(msg)`, `+ printSuccess(msg)`, `+ printInfo(msg)` |
| | `- createArrowReader() : ArrowKeyReader {static}`, `- readPlainLine() : String` |

Nested «interface»s inside ConsoleIO (both `@FunctionalInterface`):
- `TextCheck`: `+ check(raw : String) : String`
- `ScreenAction`: `+ run() : void`

### ArrowKeyReader (the only class that uses JLine)
| Fields | Methods |
|--------|---------|
| `- KEY_UP, KEY_DOWN, KEY_ENTER, ESC, HIGHLIGHT_ON, HIGHLIGHT_OFF, CLEAR_LINE : String {static, final}` | `- ArrowKeyReader(terminal : Terminal)` |
| `- terminal : Terminal` | `+ tryCreate() : ArrowKeyReader {static}` |
| `- lineReader : LineReader` | `+ readLine(prompt) : String`, `+ readHiddenLine(prompt) : String` |
| `- keyReader : BindingReader` | `+ select(options : List<String>, hint : String) : int` |
| `- menuKeys : KeyMap<String>` | `- drawOptions(...)`, `- buildMenuKeys(...) {static}`, `- bindIfKnown(...) {static}` |

### Display (final, static only)
- `+ printBox(title)`, `+ printRooms(List<Room>)`, `+ describeRoom(Room) : String`
- `+ printGuests(List<Guest>)`, `+ printBookings(List<Booking>)`, `+ describeBooking(Booking) : String`
- `+ printInvoiceHistory(List<Invoice>)`, `+ printInvoice(Invoice)`
- private helpers: `printInvoiceGuest`, `printInvoiceStay`, `printInvoiceCharges`, `invoiceLine`, `invoiceField`, `invoiceAmount`, `separator`, `fit`

### SharedForms
- Fields: `- io : ConsoleIO`, `- roomManager : RoomManager`, `- guestManager : GuestManager`, `- bookingManager : BookingManager`
- `+ registerGuestForm() : Guest`, `+ findGuestForm() : Guest`, `+ searchRoomsForm() : void`
- `+ chooseRoomType(title) : RoomType`, `+ chooseBedType(title) : BedType`, `+ <T> chooseFromList(title, values : List<T>) : T`
- `+ bookingForm(guest : Guest) : void`
- private: `readCheckIn`, `readCheckOut`, `chooseRoom`, `readGuestCount`, `printBookingSummary`, `checkNewNic`

### Screens
| Class | Fields | Public methods |
|-------|--------|----------------|
| MainScreen | io, adminScreen, guestScreen | `+ show()` |
| AdminScreen | ADMIN_USERNAME, ADMIN_PASSWORD, MAX_LOGIN_ATTEMPTS {static}, io, roomScreen, guestScreen, bookingScreen, invoiceScreen | `+ show()`, `- login() : boolean`, `- tryLogin() : boolean` |
| AdminRoomScreen | io, forms, roomManager | `+ show()` (+ private addRoom, removeRoom, updateRoom, viewRooms, changeStatus, askForRoom, checkNewRoomId) |
| AdminGuestScreen | io, forms, guestManager | `+ show()` (+ private removeGuest, updateGuest, viewGuests) |
| AdminBookingScreen | io, forms, bookingManager | `+ show()` (+ private createBooking, viewBookings) |
| AdminInvoiceScreen | io, bookingManager, billingManager | `+ show()` (+ private createInvoice, viewHistory) |
| GuestScreen | io, forms, guestManager, bookingManager, billingManager, `- currentGuest : Guest` | `+ show()` (+ private login, showGuestMenu, searchReservation, viewByCheckInDate, cancelReservation, printInvoice, chooseOwnBooking, checkSearchText) |

---

## Package `smartstay`

### SmartStayApp
- `+ main(args : String[]) : void {static}`: creates the managers, loads sample data, creates the screens, starts MainScreen.

---

# Relationships

### Inheritance (generalisation, hollow triangle arrow)
- StandardRoom, DeluxeRoom, SuiteRoom, FamilySuiteRoom ──▷ *Room*
- ValidationException, BusinessRuleException, NotFoundException ──▷ HotelException ──▷ `java.lang.Exception`
- GoBackException ──▷ `java.lang.RuntimeException`

### Association (solid line, with multiplicity)
| From | To | Multiplicity | Meaning |
|------|----|--------------|---------|
| Booking | Guest | `0..* -> 1` | a booking is for exactly one guest; a guest can have many bookings |
| Booking | Room | `0..* -> 1` | a booking is for exactly one room; a room can have many bookings (over time) |
| Invoice | Booking | `0..1 -> 1` | an invoice is for one booking; a booking has at most one invoice |
| Room | RoomType / BedType / RoomStatus | `* -> 1` | each room has one of each |
| Booking | BookingStatus | `* -> 1` | |

### Aggregation (hollow diamond: the manager holds the objects in a collection)
| Whole | Part | Multiplicity | Collection |
|-------|------|--------------|------------|
| RoomManager | Room | `1 ◇── 0..*` | HashMap by Room ID |
| GuestManager | Guest | `1 ◇── 0..*` | HashMap by NIC |
| BookingManager | Booking | `1 ◇── 0..*` | HashMap by Booking ID |
| BillingManager | Invoice | `1 ◇── 0..*` | ArrayList (issue order) |

(Aggregation, not composition: a removed guest's old bookings and invoices still point to the Guest object.)

### Association between managers (field, solid arrow)
- RoomManager ──> BookingManager `1` (asks "does this room have an active booking?")
- GuestManager ──> BookingManager `1`
- BillingManager ──> BookingManager `1` (finds bookings, marks them completed)

### Dependency (dashed arrow, "uses")
- RoomManager ┄> RoomFactory (`addRoom` calls `createRoom`)
- RoomFactory ┄> StandardRoom, DeluxeRoom, SuiteRoom, FamilySuiteRoom «create»
- BookingManager ┄> Validator (date rules)
- All screens ┄> Menu, Display; SharedForms / screens ┄> Validator, MoneyFormatter
- ConsoleIO ┄> Validator
- Managers ┄> exception classes «throws»
- SmartStayApp ┄> every manager and screen «create», SampleDataLoader

### UI composition (solid arrow, field)
- MainScreen ──> AdminScreen `1`, GuestScreen `1`
- AdminScreen ──> AdminRoomScreen, AdminGuestScreen, AdminBookingScreen, AdminInvoiceScreen (`1` each)
- every screen ──> ConsoleIO `1`; most screens ──> SharedForms `1`
- ConsoleIO ──> ArrowKeyReader `0..1` (none in numbered mode)
- Menu ──> ConsoleIO `1`
