package dto;

import java.io.Serializable;

public class LoginResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String token;
    private final UserInfo user;

    public LoginResult(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public UserInfo getUser() { return user; }
}
