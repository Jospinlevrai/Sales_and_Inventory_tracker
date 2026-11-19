package client;

import exception.BusinessRuleException;
import exception.InsufficientStockException;
import exception.ServerUnavailableException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class UiAlerts {
    private UiAlerts() {
    }

    public static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void warn(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    public static void error(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void handleException(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && !(cause instanceof BusinessRuleException)
                && !(cause instanceof ServerUnavailableException)) {
            cause = cause.getCause();
        }

        if (cause instanceof InsufficientStockException) {
            InsufficientStockException ise = (InsufficientStockException) cause;
            warn("Insufficient Stock",
                    ise.getProductName() + " only has " + ise.getAvailable()
                            + " in stock (requested " + ise.getRequested() + ").");
        } else if (cause instanceof BusinessRuleException) {
            warn("Action Not Allowed", cause.getMessage());
        } else if (cause instanceof ServerUnavailableException) {
            error("Server Connection Lost", cause.getMessage());
        } else {
            error("Unexpected Error", cause.getMessage() != null ? cause.getMessage() : cause.toString());
        }
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}