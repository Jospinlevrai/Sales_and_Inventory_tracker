package service;

import model.Product;

import java.util.List;

public interface StockService {
    /** Corrects stock (expired, damaged, stock take). The reason is kept in the stock ledger. */
    void adjustStock(int productId, int userId, int quantityChange, String reason);

    /** Active products at or below their reorder level, lowest stock first. */
    List<Product> findProductsNeedingReorder();
}
