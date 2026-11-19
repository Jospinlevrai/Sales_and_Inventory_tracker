package service;

import model.Product;

import java.util.List;

public interface ProductService {
    List<Product> listProducts(boolean activeOnly);

    /** Active products matching the text in the name, SKU or barcode, optionally in one category. For the cashier screen. */
    List<Product> searchProducts(String text, Integer categoryId);

    /** Administrators and managers. A new product always starts with no stock: record a delivery or an adjustment. */
    Product addProduct(int actingUserId, Product product);

    /** Administrators and managers. Changes everything except the stock count. */
    Product updateProduct(int actingUserId, Product product);

    /** Administrators and managers. Products are deactivated, never deleted. */
    void setProductActive(int actingUserId, int productId, boolean active);

    /** Active products that are at or below their reorder level. */
    List<Product> listLowStock(int actingUserId);
}
