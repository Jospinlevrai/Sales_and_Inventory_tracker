package service;

import model.AuditEntry;
import java.util.List;

public interface AuditRepository {
    AuditEntry save(AuditEntry entry);

    List<AuditEntry> findRecent(int limit);
}
