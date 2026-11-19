package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRequest implements ActingUserRequest, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static class Line implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int productId;
        private int quantity;
        private BigDecimal unitCost;

        public Line() {
        }

        public Line(int productId, int quantity, BigDecimal unitCost) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitCost = unitCost;
        }

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public BigDecimal getUnitCost() { return unitCost; }
        public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    }

    private int supplierId;
    private int userId;
    private String invoiceNo;
    private LocalDate purchaseDate = LocalDate.now();
    private String notes;
    private List<Line> lines = new ArrayList<>();

    public PurchaseRequest() {
    }

    public PurchaseRequest(int supplierId, int userId, String invoiceNo) {
        this.supplierId = supplierId;
        this.userId = userId;
        this.invoiceNo = invoiceNo;
    }

    public PurchaseRequest addLine(int productId, int quantity, BigDecimal unitCost) {
        lines.add(new Line(productId, quantity, unitCost));
        return this;
    }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }
}
