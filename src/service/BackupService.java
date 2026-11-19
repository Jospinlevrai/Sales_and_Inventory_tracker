package service;

/** Administrators can back up or export application data. */
public interface BackupService {
    byte[] createDatabaseBackup(int actingUserId);

    byte[] exportTableCsv(int actingUserId, String tableName);

    java.util.List<String> exportableTables(int actingUserId);
}
