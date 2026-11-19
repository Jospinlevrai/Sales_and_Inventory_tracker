package client;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class AsyncTask {
    private AsyncTask() {
    }

    public static <T> void run(Callable<T> work, Consumer<T> onSuccess) {
        run(work, onSuccess, null);
    }

    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Runnable onAlways) {
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };

        task.setOnSucceeded(event -> {
            if (onAlways != null) {
                onAlways.run();
            }
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });

        task.setOnFailed(event -> {
            if (onAlways != null) {
                onAlways.run();
            }
            UiAlerts.handleException(task.getException());
        });

        Thread thread = new Thread(task, "rmi-client-worker");
        thread.setDaemon(true);
        thread.start();
    }

    public static void runVoid(ThrowingRunnable work, Runnable onSuccess, Runnable onAlways) {
        run(() -> {
            work.run();
            return null;
        }, ignored -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        }, onAlways);
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}