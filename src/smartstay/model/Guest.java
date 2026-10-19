package smartstay.model;

/**
 * A hotel guest. The NIC/Passport number is the unique key, so it is final
 * and has no setter. Name, phone and email can be updated by the admin.
 * Checking that the values are valid is done by the Validator before a
 * Guest is created, so this class only stores the data.
 */
public class Guest {

    private final String nic;
    private String fullName;
    private String phone;
    private String email;

    public Guest(String nic, String fullName, String phone, String email) {
        this.nic = nic;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    public String getNic() {
        return nic;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
