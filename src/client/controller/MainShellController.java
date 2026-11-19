package client.controller;

import client.AsyncTask;
import client.ClientApp;
import client.SessionContext;
import client.UiAlerts;
import dto.UserInfo;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import model.Role;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainShellController {
    @FXML private Label userBadgeLabel;
    @FXML private Button navDashboardBtn;
    @FXML private Button navPosBtn;
    @FXML private Button navInventoryBtn;
    @FXML private Button navPurchasesBtn;
    @FXML private Button navUsersBtn;
    @FXML private StackPane contentArea;

    private List<Button> allNavButtons;

    @FXML
    public void initialize() {
        allNavButtons = Arrays.asList(navDashboardBtn, navPosBtn, navInventoryBtn, navPurchasesBtn, navUsersBtn);
        UserInfo user = SessionContext.getUser();
        if (user == null) {
            ClientApp.showLogin();
            return;
        }

        userBadgeLabel.setText(user.getFullName() + " (" + user.getRoleName() + ")");

        String role = user.getRoleName();
        boolean isAdmin = Role.ADMIN.equals(role);
        boolean isManagerOrAdmin = isAdmin || Role.MANAGER.equals(role);

        setNavVisible(navDashboardBtn, isManagerOrAdmin);
        setNavVisible(navPosBtn, true);
        setNavVisible(navInventoryBtn, isManagerOrAdmin);
        setNavVisible(navPurchasesBtn, isManagerOrAdmin);
        setNavVisible(navUsersBtn, isAdmin);

        if (isManagerOrAdmin) {
            showDashboard();
        } else {
            showPos();
        }
    }

    @FXML
    public void showDashboard() {
        loadView("/client/fxml/DashboardView.fxml", navDashboardBtn);
    }

    @FXML
    public void showPos() {
        loadView("/client/fxml/PosView.fxml", navPosBtn);
    }

    @FXML
    public void showInventory() {
        loadView("/client/fxml/InventoryView.fxml", navInventoryBtn);
    }

    @FXML
    public void showPurchases() {
        loadView("/client/fxml/PurchaseView.fxml", navPurchasesBtn);
    }

    @FXML
    public void showUsers() {
        loadView("/client/fxml/UsersView.fxml", navUsersBtn);
    }

    @FXML
    private void handleChangePassword() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Update password for " + SessionContext.getUser().getUsername());

        ButtonType saveType = new ButtonType("Update Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        PasswordField currentPass = new PasswordField();
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("At least 8 characters");

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPass, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPass, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == saveType
                ? new String[] {currentPass.getText(), newPass.getText()}
                : null);

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(passwords -> AsyncTask.runVoid(
                () -> SessionContext.users().changeOwnPassword(SessionContext.getUserId(), passwords[0], passwords[1]),
                () -> UiAlerts.info("Password Changed", "Your password has been updated."),
                null
        ));
    }

    @FXML
    private void handleLogout() {
        SessionContext.logout();
        ClientApp.showLogin();
    }

    private void loadView(String fxmlPath, Button activeButton) {
        try {
            Parent view = ClientApp.loader(fxmlPath).load();
            contentArea.getChildren().setAll(view);
            highlightNav(activeButton);
        } catch (IOException e) {
            UiAlerts.error("Navigation Error", "Could not open screen: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private void setNavVisible(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }

    private void highlightNav(Button active) {
        for (Button btn : allNavButtons) {
            if (btn == active) {
                btn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 6; -fx-padding: 10 12;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-padding: 10 12;");
            }
        }
    }
}