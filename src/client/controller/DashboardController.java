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
import javafx.stage.FileChooser;
import model.Sale;
import service.ReportService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardController {
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DashboardData lastData;

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
        lastData = data;

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

    @FXML
    private void handleExportCsv() {
        if (lastData == null) {
            UiAlerts.warn("No Report Data", "Refresh the dashboard before exporting.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Dashboard Report");
        chooser.setInitialFileName("dashboard-report.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(revenueChart.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writeCsvReport(writer);
            UiAlerts.info("Report Exported", "Dashboard report saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            UiAlerts.error("Export Failed", "Could not save the report: " + e.getMessage());
        }
    }

    private void writeCsvReport(PrintWriter writer) {
        DashboardSummary summary = lastData.summary;
        writer.println("DASHBOARD SUMMARY");
        writer.println("Metric,Value");
        writer.println("Revenue today," + csv(summary.getRevenueToday().toPlainString()));
        writer.println("Transactions today," + summary.getTransactionsToday());
        writer.println("Gross profit today," + csv(summary.getGrossProfitToday().toPlainString()));
        writer.println("Stock value," + csv(summary.getStockValue().toPlainString()));
        writer.println("Needs reorder," + summary.getNeedsReorder());
        writer.println("Out of stock," + summary.getOutOfStock());

        writer.println();
        writer.println("DAILY SALES");
        writer.println("Date,Transactions,Revenue");
        for (DailySales day : lastData.dailySales) {
            writer.println(csv(day.getDay().toString()) + "," + day.getTransactions() + ","
                    + csv(day.getRevenue().toPlainString()));
        }

        writer.println();
        writer.println("TOP SELLING PRODUCTS");
        writer.println("SKU,Product,Units sold,Revenue,Gross profit");
        for (ProductSales product : lastData.topProducts) {
            writer.println(csv(product.getSku()) + "," + csv(product.getProductName()) + ","
                    + product.getUnitsSold() + "," + csv(product.getRevenue().toPlainString()) + ","
                    + csv(product.getGrossProfit().toPlainString()));
        }

        writer.println();
        writer.println("RECENT SALES");
        writer.println("Receipt,Date,Cashier,Payment,Total,Status");
        for (RecentSale sale : lastData.recentSales) {
            writer.println(csv(sale.getReceiptNo()) + ","
                    + csv(sale.getSaleDate() == null ? "" : sale.getSaleDate().format(TS_FORMAT)) + ","
                    + csv(sale.getCashier()) + "," + sale.getPaymentMethod() + ","
                    + csv(sale.getTotalAmount().toPlainString()) + "," + sale.getStatus());
        }

        writer.println();
        writer.println("RECENT STOCK MOVEMENTS");
        writer.println("Date,Product,Type,Quantity,User,Note");
        for (MovementEntry movement : lastData.movements) {
            writer.println(csv(movement.getCreatedAt() == null ? "" : movement.getCreatedAt().format(TS_FORMAT)) + ","
                    + csv(movement.getProductName()) + "," + movement.getMovementType() + ","
                    + movement.getQuantityChange() + "," + csv(movement.getDoneBy()) + ","
                    + csv(movement.getNote()));
        }
    }

    private static String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
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