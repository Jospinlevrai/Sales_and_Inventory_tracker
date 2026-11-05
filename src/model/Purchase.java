package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Purchase {
    private int purchaseId;
    private int supplierId;
    private int userId;
    private String invoiceNo;
    private LocalDate purchaseDate = LocalDate.now();
    private BigDecimal totalCost = BigDecimal.ZERO;
    private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Purchase() {
    }

    public Purchase(int purchaseId, int supplierId, int userId, String invoiceNo,
                    LocalDate purchaseDate, BigDecimal totalCost, String notes,
                    LocalDateTime createdAt) {
        this.purchaseId = purchaseId;
        this.supplierId = supplierId;
        this.userId = userId;
        this.invoiceNo = invoiceNo;
        this.purchaseDate = purchaseDate;
        this.totalCost = totalCost;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getPurchaseId() { return purchaseId; }
    public void setPurchaseId(int purchaseId) { this.purchaseId = purchaseId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void calculateTotalCost(List<PurchaseItem> items) {
        totalCost = BigDecimal.ZERO;
        for (PurchaseItem item : items) {
            totalCost = totalCost.add(item.getLineTotal());
        }
    }

    @Override
    public String toString() {
        return "Purchase{purchaseId=" + purchaseId + ", supplierId=" + supplierId + ", invoiceNo="
                + invoiceNo + ", purchaseDate=" + purchaseDate + ", totalCost=" + totalCost + "}";
    }
}