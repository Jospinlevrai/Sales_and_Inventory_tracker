package dto;

import model.Sale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleRequest implements ActingUserRequest, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static class Line implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private int productId;
        private int quantity;

        public Line() {
        }

        public Line(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    private int userId;
    private Sale.PaymentMethod paymentMethod = Sale.PaymentMethod.CASH;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal taxRate = BigDecimal.ZERO;
    private String notes;
    private List<Line> lines = new ArrayList<>();

    public SaleRequest() {
    }

    public SaleRequest(int userId, Sale.PaymentMethod paymentMethod) {
        this.userId = userId;
        this.paymentMethod = paymentMethod;
    }

    public SaleRequest addLine(int productId, int quantity) {
        lines.add(new Line(productId, quantity));
        return this;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Sale.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(Sale.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }
}
