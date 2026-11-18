package service;

import dto.UserInfo;

public interface AuthService {
    /** Checks the username and password. The returned role name tells the client which dashboard to open. */
    UserInfo login(String username, String password);
}
