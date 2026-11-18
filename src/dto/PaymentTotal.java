package dto;

import java.math.BigDecimal;
import model.Sale;

public class PaymentTotal {
    private final Sale.PaymentMethod paymentMethod;
    private final int transactions;
    private final BigDecimal amount;

    public PaymentTotal(Sale.PaymentMethod paymentMethod, int transactions, BigDecimal amount) {
        this.paymentMethod = paymentMethod;
        this.transactions = transactions;
        this.amount = amount;
    }

    public Sale.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public int getTransactions() { return transactions; }
    public BigDecimal getAmount() { return amount; }
}
