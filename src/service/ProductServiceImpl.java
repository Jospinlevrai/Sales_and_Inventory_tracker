package service;

import exception.BusinessRuleException;
import model.Product;
import model.Role;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


public class ProductServiceImpl implements ProductService {
    private static final BigDecimal MAX_MONEY = new BigDecimal("9999999999.99");

    private final ProductRepository products;
    private final ProductCatalogRepository catalog;
    private final CategoryRepository categories;
    private final SupplierRepository suppliers;
    private final TransactionManager transactions;
    private final AccessControl access;

    public ProductServiceImpl(ProductRepository products, ProductCatalogRepository catalog, CategoryRepository categories,
                              SupplierRepository suppliers, UserRepository users, RoleRepository roles,
                              TransactionManager transactions) {
        this.products = products;
        this.catalog = catalog;
        this.categories = categories;
        this.suppliers = suppliers;
        this.transactions = transactions;
        this.access = new AccessControl(users, roles);
    }

    @Override
    public List<Product> listProducts(boolean activeOnly) {
        return activeOnly ? products.findActive() : catalog.findAll();
    }

    @Override
    public List<Product> listLowStock(int actingUserId) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        return products.findLowStock();
    }

    @Override
    public List<Product> searchProducts(String text, Integer categoryId) {
        return catalog.search(text, categoryId);
    }

    @Override
    public Product addProduct(int actingUserId, Product input) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        return transactions.execute(() -> {
            Product product = clean(input, 0);
            product.setQuantityInStock(0);
            product.setActive(true);
            return catalog.save(product);
        });
    }

    @Override
    public Product updateProduct(int actingUserId, Product input) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        return transactions.execute(() -> {
            if (input == null) {
                throw new BusinessRuleException("The product is missing");
            }
            Product existing = products.findById(input.getProductId())
                    .orElseThrow(() -> new BusinessRuleException("The product does not exist"));
            Product product = clean(input, existing.getProductId());
            product.setProductId(existing.getProductId());
            catalog.update(product);
            return products.findById(existing.getProductId()).orElse(product);
        });
    }

    @Override
    public void setProductActive(int actingUserId, int productId, boolean active) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        transactions.run(() -> {
            products.findById(productId).orElseThrow(() -> new BusinessRuleException("The product does not exist"));
            catalog.setActive(productId, active);
        });
    }

    /** Checks the fields and returns a clean copy; the stock count and the active flag are never taken from the input. */
    private Product clean(Product input, int excludeId) {
        if (input == null) {
            throw new BusinessRuleException("The product is missing");
        }
        String name = Text.required(input.getProductName(), 150, "Product name");
        String sku = Text.required(input.getSku(), 50, "SKU");
        String barcode = Text.optional(input.getBarcode(), 50, "Barcode");
        String unit = Text.optional(input.getUnit(), 20, "Unit");
        BigDecimal costPrice = money(input.getCostPrice(), "Cost price");
        BigDecimal sellingPrice = money(input.getSellingPrice(), "Selling price");
        if (input.getReorderLevel() < 0) {
            throw new BusinessRuleException("The reorder level cannot be negative");
        }
        if (!categories.findById(input.getCategoryId()).isPresent()) {
            throw new BusinessRuleException("Choose a category that exists");
        }
        if (input.getSupplierId() != null && !suppliers.findById(input.getSupplierId()).isPresent()) {
            throw new BusinessRuleException("Supplier " + input.getSupplierId() + " does not exist");
        }
        if (catalog.skuExists(sku, excludeId)) {
            throw new BusinessRuleException("The SKU " + sku + " is already used by another product");
        }
        if (barcode != null && catalog.barcodeExists(barcode, excludeId)) {
            throw new BusinessRuleException("The barcode " + barcode + " is already used by another product");
        }
        Product product = new Product();
        product.setCategoryId(input.getCategoryId());
        product.setSupplierId(input.getSupplierId());
        product.setSku(sku);
        product.setBarcode(barcode);
        product.setProductName(name);
        product.setDescription(Text.optional(input.getDescription(), 2000, "Description"));
        product.setUnit(unit == null ? "pcs" : unit);
        product.setCostPrice(costPrice);
        product.setSellingPrice(sellingPrice);
        product.setReorderLevel(input.getReorderLevel());
        return product;
    }

    private static BigDecimal money(BigDecimal value, String label) {
        if (value == null) {
            throw new BusinessRuleException(label + " is required");
        }
        if (value.signum() < 0) {
            throw new BusinessRuleException(label + " cannot be negative");
        }
        BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);
        if (rounded.compareTo(MAX_MONEY) > 0) {
            throw new BusinessRuleException(label + " is too large");
        }
        return rounded;
    }
}
