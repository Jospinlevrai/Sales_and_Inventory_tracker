package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ClientApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Inventory & POS Tracker");
        primaryStage.setOnCloseRequest(event -> SessionContext.logout());
        showLogin();
        primaryStage.show();
    }

    public static void showLogin() {
        setRoot("/client/fxml/LoginView.fxml", 480, 560);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
    }

    public static void showMainShell() {
        setRoot("/client/fxml/MainShell.fxml", 1220, 760);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }
    public static FXMLLoader loader(String fxmlPath) {
        URL resource = ClientApp.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalStateException("Cannot find FXML resource: " + fxmlPath);
        }
        return new FXMLLoader(resource);
    }

    private static void setRoot(String fxmlPath, double width, double height) {
        try {
            Parent root = loader(fxmlPath).load();
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                primaryStage.setScene(new Scene(root, width, height));
            } else {
                scene.setRoot(root);
                primaryStage.setWidth(width);
                primaryStage.setHeight(height);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlPath, e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}