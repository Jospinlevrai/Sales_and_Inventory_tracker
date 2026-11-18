package service;

import model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(int productId);

    List<Product> findActive();

    /**
     * Adds delta (negative to remove) to the stock in one atomic statement, for example
     * UPDATE Products SET quantity_in_stock = quantity_in_stock + ? WHERE product_id = ? AND quantity_in_stock + ? >= 0.
     * Returns false, changing nothing, when the product is missing or the stock would go below zero.
     */
    boolean changeStock(int productId, int delta);

    void updateCostPrice(int productId, BigDecimal costPrice);
}
