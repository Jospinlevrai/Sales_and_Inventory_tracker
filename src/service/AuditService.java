package service;

import model.AuditEntry;
import java.util.List;

public interface AuditService {
    List<AuditEntry> listRecent(int actingUserId, int limit);

    void record(int actingUserId, String action, String entityType, String entityId, String details);
}
