package service;

import model.Product;

import java.util.List;

/** The catalogue side of products: lists, search and editing. Stock changes go through ProductRepository. */
public interface ProductCatalogRepository {
    List<Product> findAll();

    /** Active products whose name, SKU or barcode contains the text (ignoring case), optionally in one category. */
    List<Product> search(String text, Integer categoryId);

    /** Inserts the product and fills in productId and the dates. */
    Product save(Product product);

    /** Updates everything except the stock count and the active flag. */
    void update(Product product);

    void setActive(int productId, boolean active);

    boolean skuExists(String sku, int excludeProductId);

    boolean barcodeExists(String barcode, int excludeProductId);
}
