package dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashierSaleRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String receiptNo;
    private final LocalDateTime saleDate;
    private final String paymentMethod;
    private final BigDecimal totalAmount;
    private final String status;

    public CashierSaleRow(String receiptNo, LocalDateTime saleDate, String paymentMethod,
                          BigDecimal totalAmount, String status) {
        this.receiptNo = receiptNo;
        this.saleDate = saleDate;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getReceiptNo() { return receiptNo; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}