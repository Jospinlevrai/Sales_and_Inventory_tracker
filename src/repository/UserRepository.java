package service;

import model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(int userId);

    /** Exact match: usernames are stored in lower case. */
    Optional<User> findByUsername(String username);

    List<User> findAll();

    long count();

    /** Inserts the user and fills in userId. */
    User save(User user);

    /** Updates the role, full name, email and phone number. */
    void update(User user);

    void updatePasswordHash(int userId, String passwordHash);

    void setActive(int userId, boolean active);

    /** True when another user (not excludeUserId; pass 0 for none) has this email, ignoring case. */
    boolean emailExists(String email, int excludeUserId);

    int countActiveWithRole(int roleId);
}
