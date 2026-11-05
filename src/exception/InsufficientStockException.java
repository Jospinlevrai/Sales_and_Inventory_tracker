package exception;

public class InsufficientStockException extends BusinessRuleException {
    private static final long serialVersionUID = 1L;

    private final String productName;
    private final int available;
    private final int requested;

    public InsufficientStockException(String productName, int available, int requested) {
        super("Not enough stock for " + productName + ": " + available + " in stock, " + requested + " requested");
        this.productName = productName;
        this.available = available;
        this.requested = requested;
    }

    public String getProductName() { return productName; }

    public int getAvailable() { return available; }

    public int getRequested() { return requested; }
}
