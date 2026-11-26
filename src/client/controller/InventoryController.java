package client.controller;

import client.AsyncTask;
import client.SessionContext;
import client.UiAlerts;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import model.Category;
import model.Product;
import model.Supplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryController {
    @FXML private CheckBox showInactiveProductsCheck;
    @FXML private CheckBox lowStockOnlyCheck;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colProdSku;
    @FXML private TableColumn<Product, String> colProdBarcode;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, String> colProdCategory;
    @FXML private TableColumn<Product, BigDecimal> colProdCost;
    @FXML private TableColumn<Product, BigDecimal> colProdPrice;
    @FXML private TableColumn<Product, Integer> colProdStock;
    @FXML private TableColumn<Product, Integer> colProdReorder;
    @FXML private TableColumn<Product, String> colProdStatus;

    @FXML private TableView<Category> categoriesTable;
    @FXML private TableColumn<Category, Integer> colCatId;
    @FXML private TableColumn<Category, String> colCatName;
    @FXML private TableColumn<Category, String> colCatDesc;

    @FXML private CheckBox showInactiveSuppliersCheck;
    @FXML private TableView<Supplier> suppliersTable;
    @FXML private TableColumn<Supplier, String> colSupName;
    @FXML private TableColumn<Supplier, String> colSupContact;
    @FXML private TableColumn<Supplier, String> colSupPhone;
    @FXML private TableColumn<Supplier, String> colSupEmail;
    @FXML private TableColumn<Supplier, String> colSupAddress;
    @FXML private TableColumn<Supplier, String> colSupStatus;

    private final Map<Integer, String> categoryNames = new HashMap<>();
    private List<Category> cachedCategories = new ArrayList<>();
    private List<Supplier> cachedSuppliers = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTables();
        refreshAll();
    }

    private void setupTables() {
        colProdSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));
        colProdBarcode.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBarcode() != null ? c.getValue().getBarcode() : ""));
        colProdName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colProdCategory.setCellValueFactory(c -> new SimpleStringProperty(
                categoryNames.getOrDefault(c.getValue().getCategoryId(), "#" + c.getValue().getCategoryId())));
        colProdCost.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCostPrice()));
        colProdPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getSellingPrice()));
        colProdStock.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantityInStock()).asObject());
        colProdReorder.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getReorderLevel()).asObject());
        colProdStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));

        productsTable.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (!item.isActive()) {
                    setStyle("-fx-opacity: 0.6;");
                } else if (item.isOutOfStock()) {
                    setStyle("-fx-background-color: #fee2e2;");
                } else if (item.isLowStock()) {
                    setStyle("-fx-background-color: #fef3c7;");
                } else {
                    setStyle("");
                }
            }
        });

        colCatId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCategoryId()).asObject());
        colCatName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategoryName()));
        colCatDesc.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescription() != null ? c.getValue().getDescription() : ""));

        colSupName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSupplierName()));
        colSupContact.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getContactPerson() != null ? c.getValue().getContactPerson() : ""));
        colSupPhone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPhoneNumber() != null ? c.getValue().getPhoneNumber() : ""));
        colSupEmail.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colSupAddress.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAddress() != null ? c.getValue().getAddress() : ""));
        colSupStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));
    }

    @FXML
    public void refreshAll() {
        AsyncTask.run(() -> SessionContext.categories().listCategories(), list -> {
            cachedCategories = list;
            categoryNames.clear();
            for (Category c : list) {
                categoryNames.put(c.getCategoryId(), c.getCategoryName());
            }
            categoriesTable.getItems().setAll(list);
            loadProducts();
        });
        loadSuppliers();
    }

    @FXML
    public void loadProducts() {
        boolean activeOnly = !showInactiveProductsCheck.isSelected();
        if (lowStockOnlyCheck.isSelected()) {
            AsyncTask.run(
                    () -> SessionContext.products().listLowStock(SessionContext.getUserId()),
                    list -> productsTable.getItems().setAll(list)
            );
            return;
        }
        AsyncTask.run(
                () -> SessionContext.products().listProducts(activeOnly),
                list -> productsTable.getItems().setAll(list)
        );
    }

    @FXML
    public void loadSuppliers() {
        boolean activeOnly = !showInactiveSuppliersCheck.isSelected();
        AsyncTask.run(
                () -> SessionContext.suppliers().listSuppliers(activeOnly),
                list -> {
                    cachedSuppliers = list;
                    suppliersTable.getItems().setAll(list);
                }
        );
    }

    @FXML
    private void handleAddProduct() {
        showProductDialog(null).ifPresent(product -> AsyncTask.run(
                () -> SessionContext.products().addProduct(SessionContext.getUserId(), product),
                created -> {
                    UiAlerts.info("Product Added", created.getProductName() + " added (initial stock is 0; record a delivery or stock adjustment to add stock).");
                    loadProducts();
                }
        ));
    }

    @FXML
    private void handleEditProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a product to edit.");
            return;
        }
        showProductDialog(selected).ifPresent(updated -> {
            updated.setProductId(selected.getProductId());
            AsyncTask.run(
                    () -> SessionContext.products().updateProduct(SessionContext.getUserId(), updated),
                    saved -> loadProducts()
            );
        });
    }

    @FXML
    private void handleToggleProductActive() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a product first.");
            return;
        }
        boolean targetState = !selected.isActive();
        AsyncTask.runVoid(
                () -> SessionContext.products().setProductActive(SessionContext.getUserId(), selected.getProductId(), targetState),
                this::loadProducts,
                null
        );
    }

    @FXML
    private void handleAdjustStock() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a product to adjust its stock.");
            return;
        }

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Adjust Stock");
        dialog.setHeaderText(selected.getProductName() + " (Current stock: " + selected.getQuantityInStock() + ")");
        ButtonType saveType = new ButtonType("Apply Adjustment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField deltaField = new TextField();
        deltaField.setPromptText("e.g. +10 or -3");
        TextField reasonField = new TextField();
        reasonField.setPromptText("e.g. Damaged goods / Stock take");

        grid.add(new Label("Quantity Change (+/-):"), 0, 0);
        grid.add(deltaField, 1, 0);
        grid.add(new Label("Reason (Required):"), 0, 1);
        grid.add(reasonField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == saveType
                ? new String[] {deltaField.getText(), reasonField.getText()}
                : null);

        dialog.showAndWait().ifPresent(vals -> {
            int delta;
            try {
                delta = Integer.parseInt(vals[0].trim());
            } catch (NumberFormatException e) {
                UiAlerts.warn("Invalid Quantity", "Enter a whole number such as 5 or -2.");
                return;
            }
            AsyncTask.runVoid(
                    () -> SessionContext.stock().adjustStock(selected.getProductId(), SessionContext.getUserId(), delta, vals[1]),
                    () -> {
                        UiAlerts.info("Stock Adjusted", "Stock updated for " + selected.getProductName() + ".");
                        loadProducts();
                    },
                    null
            );
        });
    }

    private Optional<Product> showProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Product" : "Edit Product");
        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nameField = new TextField(existing != null ? existing.getProductName() : "");
        TextField skuField = new TextField(existing != null ? existing.getSku() : "");
        TextField barcodeField = new TextField(existing != null && existing.getBarcode() != null ? existing.getBarcode() : "");
        ComboBox<Category> catBox = new ComboBox<>(FXCollections.observableArrayList(cachedCategories));
        catBox.setCellFactory(l -> new ListCell<Category>() {
            @Override protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getCategoryName());
            }
        });
        catBox.setButtonCell(new ListCell<Category>() {
            @Override protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getCategoryName());
            }
        });
        if (existing != null) {
            cachedCategories.stream().filter(c -> c.getCategoryId() == existing.getCategoryId()).findFirst().ifPresent(catBox::setValue);
        } else if (!cachedCategories.isEmpty()) {
            catBox.setValue(cachedCategories.get(0));
        }

        List<Supplier> supplierChoices = new ArrayList<>();
        supplierChoices.add(null);
        supplierChoices.addAll(cachedSuppliers);
        ComboBox<Supplier> supBox = new ComboBox<>(FXCollections.observableArrayList(supplierChoices));
        supBox.setCellFactory(l -> new ListCell<Supplier>() {
            @Override protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "(None)" : item.getSupplierName());
            }
        });
        supBox.setButtonCell(new ListCell<Supplier>() {
            @Override protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "(None)" : item.getSupplierName());
            }
        });
        if (existing != null && existing.getSupplierId() != null) {
            cachedSuppliers.stream().filter(s -> s.getSupplierId() == existing.getSupplierId()).findFirst().ifPresent(supBox::setValue);
        }

        TextField unitField = new TextField(existing != null ? existing.getUnit() : "pcs");
        TextField costField = new TextField(existing != null ? existing.getCostPrice().toPlainString() : "0.00");
        TextField priceField = new TextField(existing != null ? existing.getSellingPrice().toPlainString() : "0.00");
        TextField reorderField = new TextField(existing != null ? String.valueOf(existing.getReorderLevel()) : "5");
        TextField descField = new TextField(existing != null && existing.getDescription() != null ? existing.getDescription() : "");

        grid.add(new Label("Product Name:*"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("SKU:*"), 0, 1);          grid.add(skuField, 1, 1);
        grid.add(new Label("Barcode:"), 0, 2);       grid.add(barcodeField, 1, 2);
        grid.add(new Label("Category:*"), 0, 3);     grid.add(catBox, 1, 3);
        grid.add(new Label("Default Supplier:"), 0, 4); grid.add(supBox, 1, 4);
        grid.add(new Label("Unit:"), 0, 5);          grid.add(unitField, 1, 5);
        grid.add(new Label("Cost Price:*"), 0, 6);   grid.add(costField, 1, 6);
        grid.add(new Label("Selling Price:*"), 0, 7);grid.add(priceField, 1, 7);
        grid.add(new Label("Reorder Level:*"), 0, 8);grid.add(reorderField, 1, 8);
        grid.add(new Label("Description:"), 0, 9);   grid.add(descField, 1, 9);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveType) {
                return null;
            }
            try {
                Product p = new Product();
                p.setProductName(nameField.getText());
                p.setSku(skuField.getText());
                p.setBarcode(barcodeField.getText());
                p.setCategoryId(catBox.getValue() != null ? catBox.getValue().getCategoryId() : 0);
                p.setSupplierId(supBox.getValue() != null ? supBox.getValue().getSupplierId() : null);
                p.setUnit(unitField.getText());
                p.setCostPrice(new BigDecimal(costField.getText().trim()));
                p.setSellingPrice(new BigDecimal(priceField.getText().trim()));
                p.setReorderLevel(Integer.parseInt(reorderField.getText().trim()));
                p.setDescription(descField.getText());
                return p;
            } catch (NumberFormatException e) {
                UiAlerts.warn("Invalid Numbers", "Cost price, selling price, and reorder level must be valid numbers.");
                return null;
            }
        });

        return dialog.showAndWait();
    }

    @FXML
    private void handleAddCategory() {
        showCategoryDialog(null).ifPresent(cat -> AsyncTask.run(
                () -> SessionContext.categories().addCategory(SessionContext.getUserId(), cat),
                saved -> refreshAll()
        ));
    }

    @FXML
    private void handleEditCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a category first.");
            return;
        }
        showCategoryDialog(selected).ifPresent(cat -> {
            cat.setCategoryId(selected.getCategoryId());
            AsyncTask.run(
                    () -> SessionContext.categories().updateCategory(SessionContext.getUserId(), cat),
                    saved -> refreshAll()
            );
        });
    }

    @FXML
    private void handleDeleteCategory() {
        Category selected = categoriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a category first.");
            return;
        }
        if (!UiAlerts.confirm("Delete Category", "Delete category '" + selected.getCategoryName() + "'?")) {
            return;
        }
        AsyncTask.runVoid(
                () -> SessionContext.categories().deleteCategory(SessionContext.getUserId(), selected.getCategoryId()),
                this::refreshAll,
                null
        );
    }

    private Optional<Category> showCategoryDialog(Category existing) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Category" : "Edit Category");
        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nameField = new TextField(existing != null ? existing.getCategoryName() : "");
        TextField descField = new TextField(existing != null && existing.getDescription() != null ? existing.getDescription() : "");

        grid.add(new Label("Category Name:*"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);    grid.add(descField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == saveType
                ? new Category(0, nameField.getText(), descField.getText())
                : null);
        return dialog.showAndWait();
    }

    @FXML
    private void handleAddSupplier() {
        showSupplierDialog(null).ifPresent(sup -> AsyncTask.run(
                () -> SessionContext.suppliers().addSupplier(SessionContext.getUserId(), sup),
                saved -> loadSuppliers()
        ));
    }

    @FXML
    private void handleEditSupplier() {
        Supplier selected = suppliersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a supplier first.");
            return;
        }
        showSupplierDialog(selected).ifPresent(sup -> {
            sup.setSupplierId(selected.getSupplierId());
            AsyncTask.run(
                    () -> SessionContext.suppliers().updateSupplier(SessionContext.getUserId(), sup),
                    saved -> loadSuppliers()
            );
        });
    }

    @FXML
    private void handleToggleSupplierActive() {
        Supplier selected = suppliersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Selection", "Select a supplier first.");
            return;
        }
        boolean target = !selected.isActive();
        AsyncTask.runVoid(
                () -> SessionContext.suppliers().setSupplierActive(SessionContext.getUserId(), selected.getSupplierId(), target),
                this::loadSuppliers,
                null
        );
    }

    private Optional<Supplier> showSupplierDialog(Supplier existing) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Supplier" : "Edit Supplier");
        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20, 20, 10, 20));

        TextField nameField = new TextField(existing != null ? existing.getSupplierName() : "");
        TextField contactField = new TextField(existing != null && existing.getContactPerson() != null ? existing.getContactPerson() : "");
        TextField phoneField = new TextField(existing != null && existing.getPhoneNumber() != null ? existing.getPhoneNumber() : "");
        TextField emailField = new TextField(existing != null && existing.getEmail() != null ? existing.getEmail() : "");
        TextField addressField = new TextField(existing != null && existing.getAddress() != null ? existing.getAddress() : "");

        grid.add(new Label("Supplier Name:*"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Contact Person:"), 0, 1); grid.add(contactField, 1, 1);
        grid.add(new Label("Phone Number:"), 0, 2);   grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);          grid.add(emailField, 1, 3);
        grid.add(new Label("Address:"), 0, 4);        grid.add(addressField, 1, 4);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveType) {
                return null;
            }
            Supplier s = new Supplier();
            s.setSupplierName(nameField.getText());
            s.setContactPerson(contactField.getText());
            s.setPhoneNumber(phoneField.getText());
            s.setEmail(emailField.getText());
            s.setAddress(addressField.getText());
            return s;
        });
        return dialog.showAndWait();
    }
}