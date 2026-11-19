package service;

import exception.BusinessRuleException;
import model.Product;
import model.Stock_movement;

import java.util.*;

public class StockServiceImpl implements StockService {
    private final ProductRepository products;
    private final TransactionManager transactions;
    private final StockLedger ledger;

    public StockServiceImpl(ProductRepository products, StockMovementRepository movements,
                            TransactionManager transactions) {
        this.products = products;
        this.transactions = transactions;
        this.ledger = new StockLedger(products, movements);
    }

    @Override
    public void adjustStock(int productId, int userId, int quantityChange, String reason) {
        if (quantityChange == 0) {
            throw new BusinessRuleException("The adjustment cannot be zero");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Give a reason for the adjustment");
        }
        if (userId <= 0) {
            throw new BusinessRuleException("The user adjusting stock is missing");
        }
        if (quantityChange < -1_000_000 || quantityChange > 1_000_000) {
            throw new BusinessRuleException("The stock adjustment must be between -1000000 and 1000000");
        }
        transactions.run(() -> {
            products.findById(productId)
                    .orElseThrow(() -> new BusinessRuleException("Product " + productId + " does not exist"));
            ledger.record(productId, userId, Stock_movement.MovementType.ADJUSTMENT, quantityChange,
                    null, null, reason.trim());
        });
    }

    @Override
    public List<Product> findProductsNeedingReorder() {
        List<Product> needReorder = new ArrayList<>();
        for (Product product : products.findActive()) {
            if (product.isLowStock()) {
                needReorder.add(product);
            }
        }
        needReorder.sort(Comparator.comparingInt(Product::getQuantityInStock));
        return needReorder;
    }
}
