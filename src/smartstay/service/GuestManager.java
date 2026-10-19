package smartstay.service;

import smartstay.exception.BusinessRuleException;
import smartstay.exception.NotFoundException;
import smartstay.model.Guest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps all guests in a HashMap keyed by NIC/Passport, and holds the guest
 * rules (no duplicate NIC, no removing a guest with an active booking).
 * The values are already checked by the Validator in the UI, so this class
 * only deals with the business rules.
 */
public class GuestManager {

    private final Map<String, Guest> guests = new HashMap<>();
    private final BookingManager bookingManager;

    public GuestManager(BookingManager bookingManager) {
        this.bookingManager = bookingManager;
    }

    public Guest registerGuest(String nic, String fullName, String phone, String email)
            throws BusinessRuleException {
        if (isRegistered(nic)) {
            throw new BusinessRuleException(
                    "A guest with NIC/Passport " + nic + " is already registered.");
        }
        Guest guest = new Guest(nic, fullName, phone, email);
        guests.put(nic, guest);
        return guest;
    }

    public void updateGuest(String nic, String fullName, String phone, String email)
            throws NotFoundException {
        Guest guest = findGuest(nic);
        guest.setFullName(fullName);
        guest.setPhone(phone);
        guest.setEmail(email);
    }

    public void removeGuest(String nic)
            throws NotFoundException, BusinessRuleException {
        findGuest(nic); // throws if the guest does not exist
        if (bookingManager.hasActiveBookingForGuest(nic)) {
            throw new BusinessRuleException("Guest " + nic
                    + " has an active booking and cannot be removed.");
        }
        guests.remove(nic);
    }

    public boolean isRegistered(String nic) {
        return guests.containsKey(nic);
    }

    public Guest findGuest(String nic) throws NotFoundException {
        Guest guest = guests.get(nic);
        if (guest == null) {
            throw new NotFoundException("No guest found with NIC/Passport " + nic + ".");
        }
        return guest;
    }

    /** All guests in name order (ignoring upper/lower case). */
    public List<Guest> getAllGuests() {
        List<Guest> list = new ArrayList<>(guests.values());
        list.sort(Comparator.comparing(Guest::getFullName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }
}
