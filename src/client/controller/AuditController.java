package client.controller;

import client.AsyncTask;
import client.SessionContext;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.AuditEntry;

import java.time.format.DateTimeFormatter;

public class AuditController {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private TableView<AuditEntry> auditTable;
    @FXML private TableColumn<AuditEntry, Long> colAuditId;
    @FXML private TableColumn<AuditEntry, String> colAuditTime;
    @FXML private TableColumn<AuditEntry, String> colAuditUser;
    @FXML private TableColumn<AuditEntry, String> colAuditAction;
    @FXML private TableColumn<AuditEntry, String> colAuditEntity;
    @FXML private TableColumn<AuditEntry, String> colAuditIdValue;
    @FXML private TableColumn<AuditEntry, String> colAuditDetails;

    @FXML
    public void initialize() {
        colAuditId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getAuditId()).asObject());
        colAuditTime.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() == null ? "" : c.getValue().getCreatedAt().format(FORMAT)));
        colAuditUser.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getUsername() == null ? "System" : c.getValue().getUsername()));
        colAuditAction.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAction()));
        colAuditEntity.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEntityType() == null ? "" : c.getValue().getEntityType()));
        colAuditIdValue.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEntityId() == null ? "" : c.getValue().getEntityId()));
        colAuditDetails.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDetails() == null ? "" : c.getValue().getDetails()));
        refresh();
    }

    @FXML
    public void refresh() {
        AsyncTask.run(
                () -> SessionContext.audits().listRecent(SessionContext.getUserId(), 200),
                entries -> auditTable.getItems().setAll(entries)
        );
    }
}
