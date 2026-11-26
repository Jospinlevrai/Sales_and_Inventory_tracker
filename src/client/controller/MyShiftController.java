package client.controller;

import client.AsyncTask;
import client.SessionContext;
import dto.CashierSummary;
import dto.UserInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MyShiftController {
    @FXML private Label cashierNameLabel;
    @FXML private Label shiftDateLabel;
    @FXML private Label totalTakingsLabel;
    @FXML private Label salesCountLabel;
    @FXML private Label avgBasketLabel;
    @FXML private Label cashTotalLabel;
    @FXML private Label mpesaTotalLabel;
    @FXML private Label cardTotalLabel;

    private final NumberFormat currencyFmt = NumberFormat.getNumberInstance(Locale.US);

    @FXML
    public void initialize() {
        currencyFmt.setMinimumFractionDigits(2);
        currencyFmt.setMaximumFractionDigits(2);

        UserInfo user = SessionContext.getUser();
        if (user != null) {
            cashierNameLabel.setText("Cashier: " + user.getFullName() + " (@" + user.getUsername() + ")");
        }
        shiftDateLabel.setText("Shift Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")));

        refreshStats();
    }

    @FXML
    public void refreshStats() {
        int userId = SessionContext.getUserId();
        AsyncTask.run(
                () -> SessionContext.reports().getMyShiftToday(userId),
                this::renderSummary,
                null
        );
    }

    private void renderSummary(CashierSummary summary) {
        if (summary == null) return;

        BigDecimal takings = safe(summary.getTakings());
        int count = summary.getSalesCount();
        BigDecimal avg = count > 0
                ? takings.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        totalTakingsLabel.setText("KES " + currencyFmt.format(takings));
        salesCountLabel.setText(String.valueOf(count));
        avgBasketLabel.setText("KES " + currencyFmt.format(avg));
        cashTotalLabel.setText("KES " + currencyFmt.format(safe(summary.getCash())));
        mpesaTotalLabel.setText("KES " + currencyFmt.format(safe(summary.getMpesa())));
        cardTotalLabel.setText("KES " + currencyFmt.format(safe(summary.getCard())));
    }

    private static BigDecimal safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}