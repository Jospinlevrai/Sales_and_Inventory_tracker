package dto;

import model.User;

/** A user as the screens see it: never carries the password hash. */
import java.io.Serializable;

public class UserInfo  implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private final int userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final String phoneNumber;
    private final String roleName;
    private final boolean active;

    public UserInfo(int userId, String username, String fullName, String email, String phoneNumber,
                    String roleName, boolean active) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.roleName = roleName;
        this.active = active;
    }

    public static UserInfo from(User user, String roleName) {
        return new UserInfo(user.getUserId(), user.getUsername(), user.getFullName(), user.getEmail(),
                user.getPhoneNumber(), roleName, user.isActive());
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRoleName() { return roleName; }
    public boolean isActive() { return active; }
}
