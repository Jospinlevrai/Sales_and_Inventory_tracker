package service;

import exception.BusinessRuleException;
import exception.DataAccessException;
import model.Role;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** PostgreSQL backup and CSV export implementation. */
public class BackupServiceImpl implements BackupService {
    private static final Map<String, String> TABLES = new LinkedHashMap<>();

    static {
        TABLES.put("products", "Products");
        TABLES.put("categories", "Categories");
        TABLES.put("suppliers", "Suppliers");
        TABLES.put("users", "Users");
        TABLES.put("sales", "Sales");
        TABLES.put("sale_items", "Sale_Items");
        TABLES.put("purchases", "Purchases");
        TABLES.put("purchase_items", "Purchase_Items");
        TABLES.put("stock_movements", "Stock_Movements");
    }

    private final DBConnection database;
    private final AccessControl access;

    public BackupServiceImpl(DBConnection database, UserRepository users, RoleRepository roles) {
        this.database = database;
        this.access = new AccessControl(users, roles);
    }

    @Override
    public byte[] createDatabaseBackup(int actingUserId) {
        access.require(actingUserId, Role.ADMIN);

        URI uri;
        try {
            uri = URI.create(database.getUrl().replace("jdbc:", ""));
        } catch (IllegalArgumentException e) {
            throw new DataAccessException("The database URL is not valid for backup", e);
        }

        String host = uri.getHost();
        String port = uri.getPort() > 0 ? String.valueOf(uri.getPort()) : "5432";
        String databaseName = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();

        ProcessBuilder builder = new ProcessBuilder(
                "pg_dump", "--host=" + host, "--port=" + port,
                "--username=" + database.getUser(), "--format=custom", "--no-password", databaseName);
        builder.environment().put("PGPASSWORD", database.getPassword());

        try {
            Process process = builder.start();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            process.getInputStream().transferTo(output);

            String errors;
            try (java.io.ByteArrayOutputStream errorOutput = new java.io.ByteArrayOutputStream()) {
                process.getErrorStream().transferTo(errorOutput);
                errors = errorOutput.toString(StandardCharsets.UTF_8);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new DataAccessException("Database backup failed: " + errors);
            }
            return output.toByteArray();
        } catch (java.io.IOException e) {
            throw new DataAccessException("Cannot start pg_dump. Install PostgreSQL client tools and try again", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DataAccessException("Database backup was interrupted", e);
        }
    }

    @Override
    public byte[] exportTableCsv(int actingUserId, String tableName) {
        access.require(actingUserId, Role.ADMIN);
        if (tableName == null) {
            throw new BusinessRuleException("Choose a table to export");
        }

        String physicalTable = TABLES.get(tableName.toLowerCase(Locale.ROOT));
        if (physicalTable == null) {
            throw new BusinessRuleException("Table " + tableName + " is not available for export");
        }

        return database.withConnection(connection -> {
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT * FROM " + physicalTable)) {
                StringBuilder csv = new StringBuilder();
                ResultSetMetaData metadata = rows.getMetaData();
                int columns = metadata.getColumnCount();

                for (int i = 1; i <= columns; i++) {
                    if (i > 1) csv.append(',');
                    csv.append(csv(metadata.getColumnLabel(i)));
                }
                csv.append('\n');

                while (rows.next()) {
                    for (int i = 1; i <= columns; i++) {
                        if (i > 1) csv.append(',');
                        csv.append(csv(value(rows, i)));
                    }
                    csv.append('\n');
                }
                return csv.toString().getBytes(StandardCharsets.UTF_8);
            } catch (SQLException e) {
                throw new DataAccessException("Could not export table " + physicalTable + ": " + e.getMessage(), e);
            }
        });
    }

    @Override
    public List<String> exportableTables(int actingUserId) {
        access.require(actingUserId, Role.ADMIN);
        return new ArrayList<>(TABLES.keySet());
    }

    private static String value(ResultSet rows, int column) throws SQLException {
        Object value = rows.getObject(column);
        return value == null ? "" : value.toString();
    }

    private static String csv(String value) {
        String text = value == null ? "" : value;
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
