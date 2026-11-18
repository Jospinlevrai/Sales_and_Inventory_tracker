package service;

import model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findById(int roleId);

    Optional<Role> findByName(String roleName);

    List<Role> findAll();

    /** Inserts the role and fills in roleId. */
    Role save(Role role);
}
