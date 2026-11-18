package service;

import dto.UserInfo;
import exception.BusinessRuleException;
import model.Role;
import model.User;

import java.util.Locale;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {
    private static final String WRONG_CREDENTIALS = "Wrong username or password";
    /** Checked when the username does not exist, so an unknown name takes as long as a wrong password. */
    private static final String DUMMY_HASH = PasswordHasher.hash("not-a-real-password");

    private final UserRepository users;
    private final RoleRepository roles;

    public AuthServiceImpl(UserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    @Override
    public UserInfo login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessRuleException("Enter your username and password");
        }
        Optional<User> found = users.findByUsername(username.trim().toLowerCase(Locale.ROOT));
        boolean passwordMatches = PasswordHasher.verify(password, found.map(User::getPasswordHash).orElse(DUMMY_HASH));
        if (!found.isPresent() || !passwordMatches) {
            throw new BusinessRuleException(WRONG_CREDENTIALS);
        }
        User user = found.get();
        if (!user.isActive()) {
            throw new BusinessRuleException("This account is deactivated. Ask an administrator.");
        }
        String roleName = roles.findById(user.getRoleId()).map(Role::getRoleName)
                .orElseThrow(() -> new BusinessRuleException("This account has no valid role. Ask an administrator."));
        return UserInfo.from(user, roleName);
    }
}
