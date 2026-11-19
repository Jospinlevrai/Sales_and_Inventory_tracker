package service;

import exception.BusinessRuleException;
import model.AuditEntry;
import model.Role;
import model.User;

import java.time.LocalDateTime;
import java.util.List;

public class AuditServiceImpl implements AuditService {
    private final AuditRepository audits;
    private final UserRepository users;
    private final AccessControl access;

    public AuditServiceImpl(AuditRepository audits, UserRepository users, RoleRepository roles) {
        this.audits = audits;
        this.users = users;
        this.access = new AccessControl(users, roles);
    }

    @Override
    public List<AuditEntry> listRecent(int actingUserId, int limit) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        if (limit < 1 || limit > 500) {
            throw new BusinessRuleException("Choose between 1 and 500 audit rows");
        }
        return audits.findRecent(limit);
    }

    @Override
    public void record(int actingUserId, String action, String entityType, String entityId, String details) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER, Role.CASHIER);
        User user = users.findById(actingUserId).orElse(null);

        AuditEntry entry = new AuditEntry();
        entry.setUserId(actingUserId);
        entry.setUsername(user == null ? null : user.getUsername());
        entry.setAction(Text.required(action, 100, "Audit action"));
        entry.setEntityType(Text.optional(entityType, 100, "Entity type"));
        entry.setEntityId(Text.optional(entityId, 100, "Entity ID"));
        entry.setDetails(Text.optional(details, 2000, "Audit details"));
        entry.setCreatedAt(LocalDateTime.now());
        audits.save(entry);
    }
}
