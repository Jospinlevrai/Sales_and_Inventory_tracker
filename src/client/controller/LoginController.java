package client.controller;

import client.AsyncTask;
import client.ClientApp;
import client.SessionContext;
import client.UiAlerts;
import dto.UserInfo;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import rmi.RemoteServices;
import service.UserService;

import java.util.Optional;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        hostField.setText(SessionContext.getHost());
        portField.setText(String.valueOf(SessionContext.getPort()));
    }

    @FXML
    private void handleLogin() {
        String host = hostField.getText().trim();
        int port = parsePort();
        if (port <= 0) {
            return;
        }
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (host.isEmpty()) {
            statusLabel.setText("Enter the server host.");
            return;
        }
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            statusLabel.setText("Enter your username and password.");
            return;
        }

        statusLabel.setText("");
        setBusy(true);

        AsyncTask.runVoid(
                () -> SessionContext.connectAndLogin(host, port, username, password),
                ClientApp::showMainShell,
                () -> setBusy(false)
        );
    }

    @FXML
    private void handleFirstAdminSetup() {
        String host = hostField.getText().trim();
        int port = parsePort();
        if (host.isEmpty() || port <= 0) {
            return;
        }

        Dialog<AdminSetupInput> dialog = new Dialog<>();
        dialog.setTitle("First-Time Administrator Setup");
        dialog.setHeaderText("Create the initial Administrator account (only available when no users exist).");

        ButtonType createType = new ButtonType("Create Admin", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField adminUser = new TextField();
        adminUser.setPromptText("e.g. admin");
        TextField adminFullName = new TextField();
        adminFullName.setPromptText("Full name");
        PasswordField adminPass = new PasswordField();
        adminPass.setPromptText("At least 8 characters");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(adminUser, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(adminFullName, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(adminPass, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == createType
                ? new AdminSetupInput(adminUser.getText(), adminFullName.getText(), adminPass.getText())
                : null);

        Optional<AdminSetupInput> result = dialog.showAndWait();
        result.ifPresent(input -> {
            setBusy(true);
            AsyncTask.run(() -> {
                RemoteServices services = SessionContext.connectUnauthenticated(host, port);
                UserService userService = services.service(UserService.class);
                return userService.registerFirstAdmin(input.username, input.fullName, input.password);
            }, (UserInfo created) -> {
                usernameField.setText(created.getUsername());
                passwordField.clear();
                UiAlerts.info("Administrator Created",
                        "Account '" + created.getUsername() + "' is ready. Sign in with your new password.");
            }, () -> setBusy(false));
        });
    }

    private int parsePort() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                statusLabel.setText("Port must be between 1 and 65535.");
                return -1;
            }
            return port;
        } catch (NumberFormatException e) {
            statusLabel.setText("Port must be a valid number.");
            return -1;
        }
    }

    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        loginButton.setText(busy ? "Signing in..." : "Sign In");
    }

    private static final class AdminSetupInput {
        final String username;
        final String fullName;
        final String password;

        AdminSetupInput(String username, String fullName, String password) {
            this.username = username;
            this.fullName = fullName;
            this.password = password;
        }
    }
}