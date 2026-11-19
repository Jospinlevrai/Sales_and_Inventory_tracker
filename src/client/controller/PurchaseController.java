package client.controller;

import client.AsyncTask;
import client.SessionContext;
import client.UiAlerts;
import dto.PurchaseRequest;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Product;
import model.Purchase;
import model.Supplier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PurchaseController {
    @FXML private ComboBox<Supplier> supplierBox;
    @FXML private TextField invoiceField;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField notesField;

    @FXML private ComboBox<Product> productBox;
    @FXML private TextField qtyField;
    @FXML private TextField unitCostField;

    @FXML private TableView<DeliveryRow> linesTable;
    @FXML private TableColumn<DeliveryRow, String> colLineSku;
    @FXML private TableColumn<DeliveryRow, String> colLineProduct;
    @FXML private TableColumn<DeliveryRow, Integer> colLineQty;
    @FXML private TableColumn<DeliveryRow, BigDecimal> colLineCost;
    @FXML private TableColumn<DeliveryRow, BigDecimal> colLineTotal;
    @FXML private TableColumn<DeliveryRow, Void> colLineRemove;

    @FXML private Label totalCostLabel;
    @FXML private Button submitButton;

    private final ObservableList<DeliveryRow> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        deliveryDatePicker.setValue(LocalDate.now());
        setupCombos();
        setupTable();
        refreshLookups();
    }

    private void setupCombos() {
        supplierBox.setCellFactory(l -> new ListCell<Supplier>() {
            @Override protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getSupplierName());
            }
        });
        supplierBox.setButtonCell(new ListCell<Supplier>() {
            @Override protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getSupplierName());
            }
        });

        productBox.setCellFactory(l -> new ListCell<Product>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getSku() + " - " + item.getProductName());
            }
        });
        productBox.setButtonCell(new ListCell<Product>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getSku() + " - " + item.getProductName());
            }
        });
    }

    private void setupTable() {
        linesTable.setItems(rows);
        colLineSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().product.getSku()));
        colLineProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().product.getProductName()));
        colLineQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().quantity).asObject());
        colLineCost.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().unitCost));
        colLineTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().lineTotal()));

        colLineRemove.setCellFactory(col -> new TableCell<DeliveryRow, Void>() {
            private final Button removeBtn = new Button("Remove");

            {
                removeBtn.setOnAction(e -> {
                    rows.remove(getIndex());
                    updateTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
    }

    @FXML
    public void refreshLookups() {
        AsyncTask.run(
                () -> SessionContext.suppliers().listSuppliers(true),
                list -> supplierBox.setItems(FXCollections.observableArrayList(list))
        );
        AsyncTask.run(
                () -> SessionContext.products().listProducts(true),
                list -> productBox.setItems(FXCollections.observableArrayList(list))
        );
    }

    @FXML
    private void handleProductPicked() {
        Product selected = productBox.getValue();
        if (selected != null && selected.getCostPrice() != null) {
            unitCostField.setText(selected.getCostPrice().toPlainString());
        }
    }

    @FXML
    private void handleAddLine() {
        Product product = productBox.getValue();
        if (product == null) {
            UiAlerts.warn("No Product", "Select a product to add.");
            return;
        }
        int qty;
        BigDecimal unitCost;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            unitCost = new BigDecimal(unitCostField.getText().trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            UiAlerts.warn("Invalid Input", "Enter a valid quantity and unit cost.");
            return;
        }
        if (qty <= 0 || unitCost.signum() < 0) {
            UiAlerts.warn("Invalid Input", "Quantity must be at least 1 and unit cost cannot be negative.");
            return;
        }

        for (DeliveryRow existing : rows) {
            if (existing.product.getProductId() == product.getProductId()) {
                existing.quantity += qty;
                existing.unitCost = unitCost;
                linesTable.refresh();
                updateTotal();
                qtyField.clear();
                return;
            }
        }

        rows.add(new DeliveryRow(product, qty, unitCost));
        updateTotal();
        qtyField.clear();
    }

    @FXML
    private void handleClearForm() {
        invoiceField.clear();
        notesField.clear();
        deliveryDatePicker.setValue(LocalDate.now());
        rows.clear();
        updateTotal();
    }

    @FXML
    private void handleRecordDelivery() {
        Supplier supplier = supplierBox.getValue();
        if (supplier == null) {
            UiAlerts.warn("Missing Supplier", "Select a supplier first.");
            return;
        }
        if (rows.isEmpty()) {
            UiAlerts.warn("No Items", "Add at least one product to the delivery.");
            return;
        }

        PurchaseRequest request = new PurchaseRequest(supplier.getSupplierId(), SessionContext.getUserId(), invoiceField.getText());
        request.setPurchaseDate(deliveryDatePicker.getValue());
        request.setNotes(notesField.getText());
        for (DeliveryRow row : rows) {
            request.addLine(row.product.getProductId(), row.quantity, row.unitCost);
        }

        submitButton.setDisable(true);
        AsyncTask.run(
                () -> SessionContext.purchases().recordDelivery(request),
                (Purchase saved) -> {
                    UiAlerts.info("Delivery Recorded",
                            "Purchase #" + saved.getPurchaseId() + " saved (Total cost: " + saved.getTotalCost() + "). Stock and cost prices updated.");
                    handleClearForm();
                    refreshLookups();
                },
                () -> submitButton.setDisable(false)
        );
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DeliveryRow row : rows) {
            total = total.add(row.lineTotal());
        }
        totalCostLabel.setText(total.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    public static final class DeliveryRow {
        final Product product;
        int quantity;
        BigDecimal unitCost;

        DeliveryRow(Product product, int quantity, BigDecimal unitCost) {
            this.product = product;
            this.quantity = quantity;
            this.unitCost = unitCost;
        }

        BigDecimal lineTotal() {
            return unitCost.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        }
    }
}