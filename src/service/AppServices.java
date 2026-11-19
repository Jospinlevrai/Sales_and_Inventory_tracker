package service;

/** Creates every repository and service once, wired together. The server builds one of these at start-up. */
public class AppServices {
    private final AuthService auth;
    private final UserService users;
    private final CategoryService categories;
    private final SupplierService suppliers;
    private final ProductService products;
    private final SaleService sales;
    private final PurchaseService purchases;
    private final StockService stock;
    private final ReportService reports;
    private final BackupService backups;

    public AppServices(DBConnection database) {
        TransactionManager transactions = new JdbcTransactionManager(database);
        JdbcUserRepository userRepository = new JdbcUserRepository(database);
        JdbcRoleRepository roleRepository = new JdbcRoleRepository(database);
        JdbcCategoryRepository categoryRepository = new JdbcCategoryRepository(database);
        JdbcSupplierRepository supplierRepository = new JdbcSupplierRepository(database);
        JdbcProductRepository productRepository = new JdbcProductRepository(database);
        JdbcSaleRepository saleRepository = new JdbcSaleRepository(database);
        JdbcPurchaseRepository purchaseRepository = new JdbcPurchaseRepository(database);
        JdbcStockMovementRepository movementRepository = new JdbcStockMovementRepository(database);
        JdbcReportRepository reportRepository = new JdbcReportRepository(database);

        this.auth = new AuthServiceImpl(userRepository, roleRepository);
        this.users = new UserServiceImpl(userRepository, roleRepository, transactions);
        this.categories = new CategoryServiceImpl(categoryRepository, userRepository, roleRepository, transactions);
        this.suppliers = new SupplierServiceImpl(supplierRepository, supplierRepository, userRepository, roleRepository, transactions);
        this.products = new ProductServiceImpl(productRepository, productRepository, categoryRepository, supplierRepository,
                userRepository, roleRepository, transactions);
        this.sales = new SaleServiceImpl(productRepository, saleRepository, movementRepository, transactions);
        this.purchases = new PurchaseServiceImpl(supplierRepository, productRepository, purchaseRepository,
                movementRepository, transactions);
        this.stock = new StockServiceImpl(productRepository, movementRepository, transactions);
        this.reports = new ReportServiceImpl(reportRepository, userRepository, roleRepository);
        this.backups = new BackupServiceImpl(database, userRepository, roleRepository);
    }

    public AuthService auth() { return auth; }
    public UserService users() { return users; }
    public CategoryService categories() { return categories; }
    public SupplierService suppliers() { return suppliers; }
    public ProductService products() { return products; }
    public SaleService sales() { return sales; }
    public PurchaseService purchases() { return purchases; }
    public StockService stock() { return stock; }
    public ReportService reports() { return reports; }
    public BackupService backups() { return backups; }
}
