package service;

import exception.DataAccessException;

import java.sql.SQLException;
import java.util.function.Supplier;

public class JdbcTransactionManager implements TransactionManager {
    private final Database database;

    public JdbcTransactionManager(Database database) {
        this.database = database;
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        if (database.inTransaction()) {
            return work.get();
        }
        try {
            database.begin();
            T result = work.get();
            database.commit();
            return result;
        } catch (SQLException e) {
            database.rollback();
            throw new DataAccessException("Could not complete the transaction: " + e.getMessage(), e);
        } catch (RuntimeException | Error e) {
            database.rollback();
            throw e;
        } finally {
            database.end();
        }
    }
}
