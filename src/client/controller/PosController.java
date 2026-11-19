package client.controller;

import client.AsyncTask;
import client.SessionContext;
import client.UiAlerts;
import dto.CashierSummary;
import dto.SaleRequest;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Category;
import model.Product;
import model.Sale;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class PosController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colSku;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, BigDecimal> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Void> colAddAction;

    @FXML private TableView<CartRow> cartTable;
    @FXML private TableColumn<CartRow, String> colCartProduct;
    @FXML private TableColumn<CartRow, BigDecimal> colCartPrice;
    @FXML private TableColumn<CartRow, Integer> colCartQty;
    @FXML private TableColumn<CartRow, BigDecimal> colCartTotal;
    @FXML private TableColumn<CartRow, Void> colCartRemove;

    @FXML private ComboBox<Sale.PaymentMethod> paymentMethodBox;
    @FXML private TextField discountField;
    @FXML private TextField taxRateField;
    @FXML private TextField notesField;
    @FXML private Label subtotalLabel;
    @FXML private Label grandTotalLabel;
    @FXML private Button checkoutButton;

    @FXML private Label shiftCountLabel;
    @FXML private Label shiftTotalLabel;
    @FXML private Label shiftCashLabel;
    @FXML private Label shiftMpesaLabel;
    @FXML private Label shiftCardLabel;

    private final ObservableList<Product> catalogItems = FXCollections.observableArrayList();
    private final ObservableList<CartRow> cartRows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupCatalogTable();
        setupCartTable();

        paymentMethodBox.setItems(FXCollections.observableArrayList(Sale.PaymentMethod.values()));
        paymentMethodBox.setValue(Sale.PaymentMethod.CASH);

        categoryFilter.setCellFactory(list -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Categories" : item.getCategoryName());
            }
        });
        categoryFilter.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Categories" : item.getCategoryName());
            }
        });

        discountField.textProperty().addListener((obs, o, n) -> recalculateTotals());
        taxRateField.textProperty().addListener((obs, o, n) -> recalculateTotals());

        refreshAll();
    }

    private void setupCatalogTable() {
        productTable.setItems(catalogItems);
        colSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSellingPrice()));
        colStock.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantityInStock()).asObject());

        colAddAction.setCellFactory(col -> new TableCell<Product, Void>() {
            private final Button addBtn = new Button("+ Add");

            {
                addBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 11px;");
                addBtn.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    addToCart(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    addBtn.setDisable(product.getQuantityInStock() <= 0);
                    setGraphic(addBtn);
                }
            }
        });
    }

    private void setupCartTable() {
        cartTable.setItems(cartRows);
        colCartProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().product.getProductName()));
        colCartPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().product.getSellingPrice()));
        colCartQty.setCellValueFactory(c -> c.getValue().quantity.asObject());
        colCartTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().lineTotal()));

        colCartRemove.setCellFactory(col -> new TableCell<CartRow, Void>() {
            private final Button minusBtn = new Button("-");

            {
                minusBtn.setOnAction(e -> {
                    CartRow row = getTableView().getItems().get(getIndex());
                    if (row.quantity.get() > 1) {
                        row.quantity.set(row.quantity.get() - 1);
                        cartTable.refresh();
                    } else {
                        cartRows.remove(row);
                    }
                    recalculateTotals();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : minusBtn);
            }
        });
    }

    @FXML
    public void refreshAll() {
        AsyncTask.run(() -> {
            List<Category> cats = new ArrayList<>();
            cats.add(null);
            cats.addAll(SessionContext.categories().listCategories());
            return cats;
        }, cats -> {
            Category selected = categoryFilter.getValue();
            categoryFilter.setItems(FXCollections.observableArrayList(cats));
            categoryFilter.setValue(selected);
        });

        handleSearch();
        refreshShiftSummary();
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText();
        Category selected = categoryFilter.getValue();
        Integer categoryId = selected == null ? null : selected.getCategoryId();

        AsyncTask.run(
                () -> SessionContext.products().searchProducts(query, categoryId),
                catalogItems::setAll
        );
    }

    private void addToCart(Product product) {
        for (CartRow row : cartRows) {
            if (row.product.getProductId() == product.getProductId()) {
                if (row.quantity.get() + 1 > product.getQuantityInStock()) {
                    UiAlerts.warn("Stock Limit", "Only " + product.getQuantityInStock() + " available in stock.");
                    return;
                }
                row.quantity.set(row.quantity.get() + 1);
                cartTable.refresh();
                recalculateTotals();
                return;
            }
        }
        cartRows.add(new CartRow(product, 1));
        recalculateTotals();
    }

    @FXML
    private void handleClearCart() {
        cartRows.clear();
        discountField.setText("0.00");
        notesField.clear();
        recalculateTotals();
    }

    @FXML
    private void handleCheckout() {
        if (cartRows.isEmpty()) {
            UiAlerts.warn("Empty Cart", "Add at least one product before completing a sale.");
            return;
        }

        BigDecimal discount = parseMoney(discountField.getText(), "Discount");
        BigDecimal taxRate = parseMoney(taxRateField.getText(), "Tax rate");
        if (discount == null || taxRate == null) {
            return;
        }

        SaleRequest request = new SaleRequest(SessionContext.getUserId(), paymentMethodBox.getValue());
        request.setDiscount(discount);
        request.setTaxRate(taxRate);
        request.setNotes(notesField.getText());
        for (CartRow row : cartRows) {
            request.addLine(row.product.getProductId(), row.quantity.get());
        }

        checkoutButton.setDisable(true);
        AsyncTask.run(
                () -> SessionContext.sales().completeSale(request),
                (Sale completed) -> {
                    UiAlerts.info("Sale Completed",
                            "Receipt: " + completed.getReceiptNo()
                                    + "\nTotal Paid: " + completed.getTotalAmount()
                                    + " (" + completed.getPaymentMethod() + ")");
                    handleClearCart();
                    handleSearch();
                    refreshShiftSummary();
                },
                () -> checkoutButton.setDisable(false)
        );
    }

    private void refreshShiftSummary() {
        AsyncTask.run(
                () -> SessionContext.reports().getMyShiftToday(SessionContext.getUserId()),
                (CashierSummary summary) -> {
                    shiftCountLabel.setText("Sales: " + summary.getSalesCount());
                    shiftTotalLabel.setText("Total: " + summary.getTakings());
                    shiftCashLabel.setText("Cash: " + summary.getCash());
                    shiftMpesaLabel.setText("M-Pesa: " + summary.getMpesa());
                    shiftCardLabel.setText("Card: " + summary.getCard());
                }
        );
    }

    private void recalculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartRow row : cartRows) {
            subtotal = subtotal.add(row.lineTotal());
        }
        BigDecimal discount = safeDecimal(discountField.getText());
        BigDecimal taxRate = safeDecimal(taxRateField.getText());
        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal total = taxable.multiply(BigDecimal.ONE.add(taxRate.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);

        subtotalLabel.setText(subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString());
        grandTotalLabel.setText(total.toPlainString());
    }

    private static BigDecimal parseMoney(String text, String label) {
        try {
            return new BigDecimal(text == null || text.trim().isEmpty() ? "0" : text.trim());
        } catch (NumberFormatException e) {
            UiAlerts.warn("Invalid Number", label + " must be a valid number.");
            return null;
        }
    }

    private static BigDecimal safeDecimal(String text) {
        try {
            return new BigDecimal(text == null || text.trim().isEmpty() ? "0" : text.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static final class CartRow {
        final Product product;
        final SimpleIntegerProperty quantity;

        CartRow(Product product, int qty) {
            this.product = product;
            this.quantity = new SimpleIntegerProperty(qty);
        }

        BigDecimal lineTotal() {
            return product.getSellingPrice().multiply(BigDecimal.valueOf(quantity.get()));
        }
    }
}