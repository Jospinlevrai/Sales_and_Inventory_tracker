package service;

import java.util.function.Supplier;

public interface TransactionManager {
    /**
     * Runs the work in one database transaction: commit when it returns, roll back when it throws.
     * A call made while a transaction is already running joins that transaction.
     */
    <T> T execute(Supplier<T> work);

    default void run(Runnable work) {
        execute(() -> {
            work.run();
            return null;
        });
    }
}
