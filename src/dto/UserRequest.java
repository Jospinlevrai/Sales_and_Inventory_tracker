package dto;

/** What the administrator types when creating or editing a user. The password is only used when creating. */
public class UserRequest {
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String roleName;
    private String password;

    public UserRequest() {
    }

    public UserRequest(String username, String fullName, String roleName, String password) {
        this.username = username;
        this.fullName = fullName;
        this.roleName = roleName;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
