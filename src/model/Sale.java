package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    public enum PaymentMethod {
        CASH, MPESA, CARD
    }

    public enum Status {
        COMPLETED, VOIDED
    }

    private int saleId;
    private String receiptNo;
    private int userId;
    private LocalDateTime saleDate = LocalDateTime.now();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal taxRate = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    private Status status = Status.COMPLETED;
    private String notes;

    public Sale() {
    }

    public Sale(int saleId, String receiptNo, int userId, LocalDateTime saleDate,
                BigDecimal subtotal, BigDecimal discount, BigDecimal taxRate,
                BigDecimal totalAmount, PaymentMethod paymentMethod, Status status, String notes) {
        this.saleId = saleId;
        this.receiptNo = receiptNo;
        this.userId = userId;
        this.saleDate = saleDate;
        this.subtotal = subtotal;
        this.discount = discount;
        this.taxRate = taxRate;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.notes = notes;
    }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public void calculateTotals(List<Sale_item> items) {
        subtotal = BigDecimal.ZERO;
        for (Sale_item item : items) {
            subtotal = subtotal.add(item.getLineTotal());
        }
        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        totalAmount = taxable.multiply(BigDecimal.ONE.add(taxRate.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Sale{saleId=" + saleId + ", receiptNo=" + receiptNo + ", userId=" + userId
                + ", totalAmount=" + totalAmount + ", paymentMethod=" + paymentMethod + ", status="
                + status + "}";
    }
}