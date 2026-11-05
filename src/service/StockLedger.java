package service;

import exception.InsufficientStockException;
import model.Product;
import model.Stock_movement;

/** The one place where stock changes: updates the product's count and writes the ledger row together. */
final class StockLedger {
    private final ProductRepository products;
    private final StockMovementRepository movements;

    StockLedger(ProductRepository products, StockMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    void record(int productId, int userId, StockMovement.MovementType type, int quantityChange,
                Integer saleId, Integer purchaseId, String note) {
        if (!products.changeStock(productId, quantityChange)) {
            Product product = products.findById(productId).orElse(null);
            String name = product == null ? "product " + productId : product.getProductName();
            int available = product == null ? 0 : product.getQuantityInStock();
            throw new InsufficientStockException(name, available, -quantityChange);
        }
        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setUserId(userId);
        movement.setMovementType(type);
        movement.setQuantityChange(quantityChange);
        movement.setSaleId(saleId);
        movement.setPurchaseId(purchaseId);
        movement.setNote(note);
        movements.save(movement);
    }
}
