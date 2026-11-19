package client.controller;

import client.AsyncTask;
import client.SessionContext;
import client.UiAlerts;
import dto.UserInfo;
import dto.UserRequest;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsersController {
    @FXML private TableView<UserInfo> usersTable;
    @FXML private TableColumn<UserInfo, Integer> colUserId;
    @FXML private TableColumn<UserInfo, String> colUsername;
    @FXML private TableColumn<UserInfo, String> colFullName;
    @FXML private TableColumn<UserInfo, String> colRole;
    @FXML private TableColumn<UserInfo, String> colEmail;
    @FXML private TableColumn<UserInfo, String> colPhone;
    @FXML private TableColumn<UserInfo, String> colStatus;

    private List<String> roleNames = new ArrayList<>();

    @FXML
    public void initialize() {
        colUserId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getUserId()).asObject());
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colFullName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoleName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPhoneNumber() != null ? c.getValue().getPhoneNumber() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Deactivated"));

        refreshUsers();
    }

    @FXML
    public void refreshUsers() {
        AsyncTask.run(() -> SessionContext.users().listRoleNames(), roles -> roleNames = roles);
        AsyncTask.run(
                () -> SessionContext.users().listUsers(SessionContext.getUserId()),
                list -> usersTable.getItems().setAll(list)
        );
    }

    @FXML
    private void handleCreateUser() {
        showUserDialog(null).ifPresent(req -> AsyncTask.run(
                () -> SessionContext.users().createUser(SessionContext.getUserId(), req),
                created -> {
                    UiAlerts.info("User Created", "Account '" + created.getUsername() + "' created.");
                    refreshUsers();
                }
        ));
    }

    @FXML
    private void handleEditUser() {
        UserInfo selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a user to edit.");
            return;
        }
        showUserDialog(selected).ifPresent(req -> AsyncTask.run(
                () -> SessionContext.users().updateUser(SessionContext.getUserId(), selected.getUserId(), req),
                updated -> refreshUsers()
        ));
    }

    @FXML
    private void handleResetPassword() {
        UserInfo selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a user first.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Set a new password for " + selected.getUsername());
        ButtonType saveType = new ButtonType("Reset Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));
        PasswordField passField = new PasswordField();
        passField.setPromptText("At least 8 characters");
        grid.add(new Label("New Password:"), 0, 0);
        grid.add(passField, 1, 0);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == saveType ? passField.getText() : null);
        dialog.showAndWait().ifPresent(newPassword -> AsyncTask.runVoid(
                () -> SessionContext.users().resetPassword(SessionContext.getUserId(), selected.getUserId(), newPassword),
                () -> UiAlerts.info("Password Reset", "Password updated for " + selected.getUsername() + "."),
                null
        ));
    }

    @FXML
    private void handleToggleActive() {
        UserInfo selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a user first.");
            return;
        }
        boolean target = !selected.isActive();
        AsyncTask.runVoid(
                () -> SessionContext.users().setUserActive(SessionContext.getUserId(), selected.getUserId(), target),
                this::refreshUsers,
                null
        );
    }

    private Optional<UserRequest> showUserDialog(UserInfo existing) {
        Dialog<UserRequest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Create User" : "Edit User");
        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField usernameField = new TextField(existing != null ? existing.getUsername() : "");
        usernameField.setDisable(existing != null);

        TextField fullNameField = new TextField(existing != null ? existing.getFullName() : "");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList(roleNames));
        if (existing != null) {
            roleBox.setValue(existing.getRoleName());
        } else if (!roleNames.isEmpty()) {
            roleBox.setValue(roleNames.get(0));
        }

        TextField emailField = new TextField(existing != null && existing.getEmail() != null ? existing.getEmail() : "");
        TextField phoneField = new TextField(existing != null && existing.getPhoneNumber() != null ? existing.getPhoneNumber() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("At least 8 characters");

        grid.add(new Label("Username:*"), 0, 0);  grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name:*"), 0, 1); grid.add(fullNameField, 1, 1);
        grid.add(new Label("Role:*"), 0, 2);      grid.add(roleBox, 1, 2);
        grid.add(new Label("Email:"), 0, 3);      grid.add(emailField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);      grid.add(phoneField, 1, 4);
        if (existing == null) {
            grid.add(new Label("Password:*"), 0, 5);
            grid.add(passwordField, 1, 5);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveType) {
                return null;
            }
            UserRequest req = new UserRequest();
            req.setUsername(usernameField.getText());
            req.setFullName(fullNameField.getText());
            req.setRoleName(roleBox.getValue());
            req.setEmail(emailField.getText());
            req.setPhoneNumber(phoneField.getText());
            if (existing == null) {
                req.setPassword(passwordField.getText());
            }
            return req;
        });

        return dialog.showAndWait();
    }
}