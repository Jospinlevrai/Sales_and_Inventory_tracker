package service;

import dto.UserInfo;
import dto.UserRequest;
import exception.BusinessRuleException;
import model.Role;
import model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {
    private static final Pattern USERNAME = Pattern.compile("[a-z0-9._-]{3,50}");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository users;
    private final RoleRepository roles;
    private final TransactionManager transactions;
    private final AccessControl access;

    public UserServiceImpl(UserRepository users, RoleRepository roles, TransactionManager transactions) {
        this.users = users;
        this.roles = roles;
        this.transactions = transactions;
        this.access = new AccessControl(users, roles);
    }

    @Override
    public UserInfo registerFirstAdmin(String username, String fullName, String password) {
        return transactions.execute(() -> {
            if (users.count() > 0) {
                throw new BusinessRuleException("The first administrator already exists");
            }
            for (String name : new String[] {Role.ADMIN, Role.MANAGER, Role.CASHIER}) {
                if (!roles.findByName(name).isPresent()) {
                    Role role = new Role();
                    role.setRoleName(name);
                    roles.save(role);
                }
            }
            return create(new UserRequest(username, fullName, Role.ADMIN, password));
        });
    }

    @Override
    public UserInfo createUser(int actingUserId, UserRequest request) {
        access.require(actingUserId, Role.ADMIN);
        return transactions.execute(() -> create(request));
    }

    @Override
    public UserInfo updateUser(int actingUserId, int userId, UserRequest request) {
        access.require(actingUserId, Role.ADMIN);
        return transactions.execute(() -> {
            if (request == null) {
                throw new BusinessRuleException("The user is missing");
            }
            User user = findUser(userId);
            String fullName = Text.required(request.getFullName(), 100, "Full name");
            String email = Text.optionalEmail(request.getEmail());
            String phone = Text.optionalPhone(request.getPhoneNumber());
            Role role = findRole(request.getRoleName());
            if (email != null && users.emailExists(email, userId)) {
                throw new BusinessRuleException("The email " + email + " is already used by another user");
            }
            if (user.getRoleId() != role.getRoleId()) {
                protectLastAdmin(user);
            }
            user.setRoleId(role.getRoleId());
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhoneNumber(phone);
            users.update(user);
            return UserInfo.from(user, role.getRoleName());
        });
    }

    @Override
    public void setUserActive(int actingUserId, int userId, boolean active) {
        access.require(actingUserId, Role.ADMIN);
        if (!active && actingUserId == userId) {
            throw new BusinessRuleException("You cannot deactivate your own account");
        }
        transactions.run(() -> {
            User user = findUser(userId);
            if (!active) {
                protectLastAdmin(user);
            }
            users.setActive(userId, active);
        });
    }

    @Override
    public void resetPassword(int actingUserId, int userId, String newPassword) {
        access.require(actingUserId, Role.ADMIN);
        transactions.run(() -> {
            User user = findUser(userId);
            checkPassword(newPassword, user.getUsername());
            users.updatePasswordHash(userId, PasswordHasher.hash(newPassword));
        });
    }

    @Override
    public void changeOwnPassword(int userId, String oldPassword, String newPassword) {
        transactions.run(() -> {
            User user = access.requireActive(userId);
            if (!PasswordHasher.verify(oldPassword, user.getPasswordHash())) {
                throw new BusinessRuleException("The current password is wrong");
            }
            checkPassword(newPassword, user.getUsername());
            if (newPassword.equals(oldPassword)) {
                throw new BusinessRuleException("The new password must be different from the current one");
            }
            users.updatePasswordHash(userId, PasswordHasher.hash(newPassword));
        });
    }

    @Override
    public List<UserInfo> listUsers(int actingUserId) {
        access.require(actingUserId, Role.ADMIN);
        Map<Integer, String> roleNames = new HashMap<>();
        for (Role role : roles.findAll()) {
            roleNames.put(role.getRoleId(), role.getRoleName());
        }
        List<UserInfo> infos = new ArrayList<>();
        for (User user : users.findAll()) {
            infos.add(UserInfo.from(user, roleNames.getOrDefault(user.getRoleId(), "")));
        }
        return infos;
    }

    @Override
    public List<String> listRoleNames() {
        List<String> names = new ArrayList<>();
        for (Role role : roles.findAll()) {
            names.add(role.getRoleName());
        }
        return names;
    }

    private UserInfo create(UserRequest request) {
        if (request == null) {
            throw new BusinessRuleException("The user is missing");
        }
        String username = Text.required(request.getUsername(), 50, "Username").toLowerCase(Locale.ROOT);
        if (!USERNAME.matcher(username).matches()) {
            throw new BusinessRuleException("The username must be 3 to 50 letters, digits, dots, dashes or underscores");
        }
        String fullName = Text.required(request.getFullName(), 100, "Full name");
        String email = Text.optionalEmail(request.getEmail());
        String phone = Text.optionalPhone(request.getPhoneNumber());
        checkPassword(request.getPassword(), username);
        Role role = findRole(request.getRoleName());
        if (users.findByUsername(username).isPresent()) {
            throw new BusinessRuleException("The username " + username + " is already taken");
        }
        if (email != null && users.emailExists(email, 0)) {
            throw new BusinessRuleException("The email " + email + " is already used by another user");
        }
        User user = new User();
        user.setRoleId(role.getRoleId());
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(request.getPassword()));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setActive(true);
        user = users.save(user);
        return UserInfo.from(user, role.getRoleName());
    }

    private User findUser(int userId) {
        return users.findById(userId).orElseThrow(() -> new BusinessRuleException("User " + userId + " does not exist"));
    }

    private Role findRole(String roleName) {
        String name = roleName == null ? "" : roleName.trim().toUpperCase(Locale.ROOT);
        return roles.findByName(name).orElseThrow(() -> new BusinessRuleException("The role " + roleName + " does not exist"));
    }

    private void checkPassword(String password, String username) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessRuleException("The password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (password.equalsIgnoreCase(username)) {
            throw new BusinessRuleException("The password must not be the same as the username");
        }
    }

    /** The last active administrator can be neither deactivated nor given another role. */
    private void protectLastAdmin(User user) {
        Role admin = roles.findByName(Role.ADMIN).orElse(null);
        if (admin != null && user.isActive() && user.getRoleId() == admin.getRoleId()
                && users.countActiveWithRole(admin.getRoleId()) <= 1) {
            throw new BusinessRuleException("There must be at least one active administrator");
        }
    }
}
