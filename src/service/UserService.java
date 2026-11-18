package service;

import dto.UserInfo;
import dto.UserRequest;

import java.util.List;

public interface UserService {
    /** Creates the three roles if missing and the first administrator. Only works while there are no users. */
    UserInfo registerFirstAdmin(String username, String fullName, String password);

    /** Administrators only. */
    UserInfo createUser(int actingUserId, UserRequest request);

    /** Administrators only. Changes name, email, phone and role (not the username or password). */
    UserInfo updateUser(int actingUserId, int userId, UserRequest request);

    /** Administrators only. Accounts are deactivated, never deleted. */
    void setUserActive(int actingUserId, int userId, boolean active);

    /** Administrators only. */
    void resetPassword(int actingUserId, int userId, String newPassword);

    void changeOwnPassword(int userId, String oldPassword, String newPassword);

    /** Administrators only. */
    List<UserInfo> listUsers(int actingUserId);

    List<String> listRoleNames();
}
