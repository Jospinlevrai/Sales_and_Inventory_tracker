package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import model.Sale;
import java.io.Serializable;

public class  RecentSale  implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private final int saleId;
    private final String receiptNo;
    private final LocalDateTime saleDate;
    private final String cashier;
    private final Sale.PaymentMethod paymentMethod;
    private final BigDecimal totalAmount;
    private final Sale.Status status;

    public RecentSale(int saleId,
                      String receiptNo,
                      LocalDateTime saleDate,
                      String cashier,
                      Sale.PaymentMethod paymentMethod,
                      BigDecimal totalAmount,
                      Sale.Status status) {
        this.saleId = saleId;
        this.receiptNo = receiptNo;
        this.saleDate = saleDate;
        this.cashier = cashier;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getSaleId() { return saleId; }
    public String getReceiptNo() { return receiptNo; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public String getCashier() { return cashier; }
    public Sale.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Sale.Status getStatus() { return status; }
}
