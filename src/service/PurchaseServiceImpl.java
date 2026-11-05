package service;

import dto.PurchaseRequest;
import exception.BusinessRuleException;
import model.Product;
import model.Purchase;
import model.PurchaseItem;
import model.Stock_movement;
import model.Supplier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PurchaseServiceImpl implements PurchaseService {
    private final SupplierRepository suppliers;
    private final ProductRepository products;
    private final PurchaseRepository purchases;
    private final TransactionManager transactions;
    private final StockLedger ledger;

    public PurchaseServiceImpl(SupplierRepository suppliers, ProductRepository products, PurchaseRepository purchases,
                               StockMovementRepository movements, TransactionManager transactions) {
        this.suppliers = suppliers;
        this.products = products;
        this.purchases = purchases;
        this.transactions = transactions;
        this.ledger = new StockLedger(products, movements);
    }

    @Override
    public Purchase recordDelivery(PurchaseRequest request) {
        validate(request);

        return transactions.execute(() -> {
            Supplier supplier = suppliers.findById(request.getSupplierId())
                    .orElseThrow(() -> new BusinessRuleException("Supplier " + request.getSupplierId() + " does not exist"));
            if (!supplier.isActive()) {
                throw new BusinessRuleException(supplier.getSupplierName() + " is not an active supplier");
            }
            String invoiceNo = blankToNull(request.getInvoiceNo());
            if (invoiceNo != null && purchases.invoiceExists(supplier.getSupplierId(), invoiceNo)) {
                throw new BusinessRuleException("Invoice " + invoiceNo + " from " + supplier.getSupplierName()
                        + " is already recorded");
            }

            List<PurchaseItem> items = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (PurchaseRequest.Line line : request.getLines()) {
                Product product = products.findById(line.getProductId())
                        .orElseThrow(() -> new BusinessRuleException("Product " + line.getProductId() + " does not exist"));
                if (!product.isActive()) {
                    throw new BusinessRuleException(product.getProductName()
                            + " is inactive; reactivate it before receiving stock");
                }
                BigDecimal unitCost = line.getUnitCost().setScale(2, RoundingMode.HALF_UP);
                PurchaseItem item = new PurchaseItem(product.getProductId(), line.getQuantity(), unitCost);
                items.add(item);
                total = total.add(item.getLineTotal());
            }

            Purchase purchase = new Purchase();
            purchase.setSupplierId(supplier.getSupplierId());
            purchase.setUserId(request.getUserId());
            purchase.setInvoiceNo(invoiceNo);
            purchase.setPurchaseDate(request.getPurchaseDate() == null ? LocalDate.now() : request.getPurchaseDate());
            purchase.setTotalCost(total);
            purchase.setNotes(request.getNotes());
            purchase = purchases.save(purchase);

            for (PurchaseItem item : items) {
                item.setPurchaseId(purchase.getPurchaseId());
                purchases.saveItem(item);
                ledger.record(item.getProductId(), purchase.getUserId(), StockMovement.MovementType.PURCHASE,
                        item.getQuantity(), null, purchase.getPurchaseId(), null);
                products.updateCostPrice(item.getProductId(), item.getUnitCost());
            }
            return purchase;
        });
    }

    private void validate(PurchaseRequest request) {
        if (request == null) {
            throw new BusinessRuleException("The delivery is missing");
        }
        if (request.getSupplierId() <= 0) {
            throw new BusinessRuleException("Choose the supplier");
        }
        if (request.getUserId() <= 0) {
            throw new BusinessRuleException("The user recording the delivery is missing");
        }
        if (request.getPurchaseDate() != null && request.getPurchaseDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("The delivery date cannot be in the future");
        }
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessRuleException("Add at least one product to the delivery");
        }
        Set<Integer> seen = new HashSet<>();
        for (PurchaseRequest.Line line : request.getLines()) {
            if (line.getQuantity() <= 0) {
                throw new BusinessRuleException("Each quantity must be at least 1");
            }
            if (line.getUnitCost() == null || line.getUnitCost().signum() < 0) {
                throw new BusinessRuleException("Each unit cost must be zero or more");
            }
            if (!seen.add(line.getProductId())) {
                throw new BusinessRuleException("Product " + line.getProductId() + " appears twice; combine the lines");
            }
        }
    }

    private static String blankToNull(String text) {
        return text == null || text.trim().isEmpty() ? null : text.trim();
    }
}
