package client.controller;

import client.AsyncTask;
import client.SessionContext;
import client.UiAlerts;
import dto.DailySales;
import dto.DashboardSummary;
import dto.MovementEntry;
import dto.PaymentTotal;
import dto.ProductSales;
import dto.RecentSale;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import model.Sale;
import service.ReportService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private ComboBox<Integer> daysRangeBox;
    @FXML private Label kpiRevenueLabel;
    @FXML private Label kpiTransactionsLabel;
    @FXML private Label kpiProfitLabel;
    @FXML private Label kpiStockValueLabel;
    @FXML private Label kpiReorderLabel;
    @FXML private Label kpiOutOfStockLabel;

    @FXML private BarChart<String, Number> revenueChart;

    @FXML private TableView<PaymentTotal> paymentTable;
    @FXML private TableColumn<PaymentTotal, String> colPayMethod;
    @FXML private TableColumn<PaymentTotal, Integer> colPayCount;
    @FXML private TableColumn<PaymentTotal, BigDecimal> colPayAmount;

    @FXML private Button voidSaleButton;
    @FXML private TableView<RecentSale> recentSalesTable;
    @FXML private TableColumn<RecentSale, String> colSaleReceipt;
    @FXML private TableColumn<RecentSale, String> colSaleDate;
    @FXML private TableColumn<RecentSale, String> colSaleCashier;
    @FXML private TableColumn<RecentSale, String> colSaleMethod;
    @FXML private TableColumn<RecentSale, BigDecimal> colSaleTotal;
    @FXML private TableColumn<RecentSale, String> colSaleStatus;

    @FXML private TableView<ProductSales> topProductsTable;
    @FXML private TableColumn<ProductSales, String> colTopSku;
    @FXML private TableColumn<ProductSales, String> colTopName;
    @FXML private TableColumn<ProductSales, Integer> colTopUnits;
    @FXML private TableColumn<ProductSales, BigDecimal> colTopRevenue;
    @FXML private TableColumn<ProductSales, BigDecimal> colTopProfit;

    @FXML private TableView<MovementEntry> movementsTable;
    @FXML private TableColumn<MovementEntry, String> colMovDate;
    @FXML private TableColumn<MovementEntry, String> colMovProduct;
    @FXML private TableColumn<MovementEntry, String> colMovType;
    @FXML private TableColumn<MovementEntry, Integer> colMovQty;
    @FXML private TableColumn<MovementEntry, String> colMovUser;
    @FXML private TableColumn<MovementEntry, String> colMovNote;

    @FXML
    public void initialize() {
        daysRangeBox.setItems(FXCollections.observableArrayList(7, 14, 30));
        daysRangeBox.setValue(7);

        setupTables();
        refreshDashboard();
    }

    private void setupTables() {
        colPayMethod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod().name()));
        colPayCount.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTransactions()).asObject());
        colPayAmount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getAmount()));

        colSaleReceipt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReceiptNo()));
        colSaleDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSaleDate() != null ? c.getValue().getSaleDate().format(TS_FORMAT) : ""));
        colSaleCashier.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCashier()));
        colSaleMethod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod().name()));
        colSaleTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getTotalAmount()));
        colSaleStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));

        colTopSku.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSku()));
        colTopName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colTopUnits.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getUnitsSold()).asObject());
        colTopRevenue.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getRevenue()));
        colTopProfit.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getGrossProfit()));

        colMovDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(TS_FORMAT) : ""));
        colMovProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colMovType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMovementType().name()));
        colMovQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantityChange()).asObject());
        colMovUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDoneBy()));
        colMovNote.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNote() != null ? c.getValue().getNote() : ""));
    }

    @FXML
    public void refreshDashboard() {
        int userId = SessionContext.getUserId();
        int days = daysRangeBox.getValue() == null ? 7 : daysRangeBox.getValue();

        AsyncTask.run(() -> {
            ReportService reports = SessionContext.reports();
            DashboardData data = new DashboardData();
            data.summary = reports.getDashboardSummary(userId);
            data.dailySales = reports.getRevenueByDay(userId, days);
            data.payments = reports.getPaymentSplitToday(userId);
            data.topProducts = reports.getTopProducts(userId, days, 15);
            data.recentSales = reports.getRecentSales(userId, 30);
            data.movements = reports.getRecentMovements(userId, 30);
            return data;
        }, this::applyData);
    }

    private void applyData(DashboardData data) {
        DashboardSummary s = data.summary;
        kpiRevenueLabel.setText(s.getRevenueToday().toPlainString());
        kpiTransactionsLabel.setText(String.valueOf(s.getTransactionsToday()));
        kpiProfitLabel.setText(s.getGrossProfitToday().toPlainString());
        kpiStockValueLabel.setText(s.getStockValue().toPlainString());
        kpiReorderLabel.setText(String.valueOf(s.getNeedsReorder()));
        kpiOutOfStockLabel.setText(String.valueOf(s.getOutOfStock()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (DailySales day : data.dailySales) {
            series.getData().add(new XYChart.Data<>(day.getDay().toString(), day.getRevenue()));
        }
        revenueChart.getData().setAll(series);

        paymentTable.getItems().setAll(data.payments);
        topProductsTable.getItems().setAll(data.topProducts);
        recentSalesTable.getItems().setAll(data.recentSales);
        movementsTable.getItems().setAll(data.movements);
    }

    @FXML
    private void handleVoidSale() {
        RecentSale selected = recentSalesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiAlerts.warn("No Sale Selected", "Select a sale row first.");
            return;
        }
        if (selected.getStatus() == Sale.Status.VOIDED) {
            UiAlerts.warn("Already Voided", "Receipt " + selected.getReceiptNo() + " is already voided.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Void Sale");
        dialog.setHeaderText("Void receipt " + selected.getReceiptNo() + " (Total: " + selected.getTotalAmount() + ")");
        dialog.setContentText("Reason for voiding:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            voidSaleButton.setDisable(true);
            AsyncTask.runVoid(
                    () -> SessionContext.sales().voidSale(selected.getSaleId(), SessionContext.getUserId(), reason),
                    () -> {
                        UiAlerts.info("Sale Voided", "Receipt " + selected.getReceiptNo() + " has been voided and stock restored.");
                        refreshDashboard();
                    },
                    () -> voidSaleButton.setDisable(false)
            );
        });
    }

    private static final class DashboardData {
        DashboardSummary summary;
        List<DailySales> dailySales;
        List<PaymentTotal> payments;
        List<ProductSales> topProducts;
        List<RecentSale> recentSales;
        List<MovementEntry> movements;
    }
}