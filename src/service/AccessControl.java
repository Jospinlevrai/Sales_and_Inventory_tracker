package service;

import exception.BusinessRuleException;
import model.Role;
import model.User;

/** Checks who is allowed to do what, using the user id sent with the request. */
final class AccessControl {
    private final UserRepository users;
    private final RoleRepository roles;

    AccessControl(UserRepository users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }

    /** Throws unless the user exists, is active and has one of the allowed roles. */
    User require(int userId, String... allowedRoles) {
        User user = requireActive(userId);
        String role = roles.findById(user.getRoleId()).map(Role::getRoleName).orElse("");
        for (String allowed : allowedRoles) {
            if (allowed.equals(role)) {
                return user;
            }
        }
        throw new BusinessRuleException("You are not allowed to do this (it needs " + String.join(" or ", allowedRoles) + ")");
    }

    /** Throws unless the user exists and is active, whatever the role. */
    User requireActive(int userId) {
        User user = users.findById(userId).orElseThrow(() -> new BusinessRuleException("Unknown user"));
        if (!user.isActive()) {
            throw new BusinessRuleException("This account is deactivated");
        }
        return user;
    }
}
