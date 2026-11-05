package service;

import dto.SaleRequest;
import exception.BusinessRuleException;
import exception.InsufficientStockException;
import model.Product;
import model.Sale;
import model.Sale_item;
import model.Stock_movement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SaleServiceImpl implements SaleService {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final ProductRepository products;
    private final SaleRepository sales;
    private final TransactionManager transactions;
    private final StockLedger ledger;

    public SaleServiceImpl(ProductRepository products, SaleRepository sales,
                           StockMovementRepository movements, TransactionManager transactions) {
        this.products = products;
        this.sales = sales;
        this.transactions = transactions;
        this.ledger = new StockLedger(products, movements);
    }

    @Override
    public Sale completeSale(SaleRequest request) {
        validate(request);
        BigDecimal discount = orZero(request.getDiscount());
        BigDecimal taxRate = orZero(request.getTaxRate());
        Map<Integer, Integer> quantities = mergeLines(request.getLines());

        return transactions.execute(() -> {
            List<SaleItem> items = buildItems(quantities);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (SaleItem item : items) {
                subtotal = subtotal.add(item.getLineTotal());
            }
            if (discount.compareTo(subtotal) > 0) {
                throw new BusinessRuleException("The discount cannot be more than the subtotal (" + subtotal + ")");
            }

            Sale sale = new Sale();
            sale.setUserId(request.getUserId());
            sale.setPaymentMethod(request.getPaymentMethod());
            sale.setSubtotal(subtotal);
            sale.setDiscount(discount);
            sale.setTaxRate(taxRate);
            sale.setTotalAmount(totalOf(subtotal, discount, taxRate));
            sale.setNotes(request.getNotes());
            sale = sales.save(sale);

            for (SaleItem item : items) {
                item.setSaleId(sale.getSaleId());
                sales.saveItem(item);
                ledger.record(item.getProductId(), sale.getUserId(), StockMovement.MovementType.SALE,
                        -item.getQuantity(), sale.getSaleId(), null, null);
            }
            return sale;
        });
    }

    @Override
    public void voidSale(int saleId, int userId, String reason) {
        if (userId <= 0) {
            throw new BusinessRuleException("The user voiding the sale is missing");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Give a reason for voiding the sale");
        }
        transactions.run(() -> {
            Sale sale = sales.findById(saleId)
                    .orElseThrow(() -> new BusinessRuleException("Sale " + saleId + " does not exist"));
            if (sale.getStatus() == Sale.Status.VOIDED) {
                throw new BusinessRuleException("Sale " + sale.getReceiptNo() + " is already voided");
            }
            sales.updateStatus(saleId, Sale.Status.VOIDED);
            for (SaleItem item : sales.findItems(saleId)) {
                ledger.record(item.getProductId(), userId, StockMovement.MovementType.SALE_VOID,
                        item.getQuantity(), saleId, null, reason.trim());
            }
        });
    }

    private void validate(SaleRequest request) {
        if (request == null) {
            throw new BusinessRuleException("The sale is missing");
        }
        if (request.getUserId() <= 0) {
            throw new BusinessRuleException("The cashier is missing");
        }
        if (request.getPaymentMethod() == null) {
            throw new BusinessRuleException("Choose a payment method");
        }
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessRuleException("Add at least one item to the sale");
        }
        for (SaleRequest.Line line : request.getLines()) {
            if (line.getQuantity() <= 0) {
                throw new BusinessRuleException("Each quantity must be at least 1");
            }
        }
        if (orZero(request.getDiscount()).signum() < 0) {
            throw new BusinessRuleException("The discount cannot be negative");
        }
        BigDecimal taxRate = orZero(request.getTaxRate());
        if (taxRate.signum() < 0 || taxRate.compareTo(HUNDRED) > 0) {
            throw new BusinessRuleException("The tax rate must be between 0 and 100");
        }
    }

    /** The same product scanned twice becomes one line (a sale holds each product once). */
    private Map<Integer, Integer> mergeLines(List<SaleRequest.Line> lines) {
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (SaleRequest.Line line : lines) {
            quantities.merge(line.getProductId(), line.getQuantity(), Integer::sum);
        }
        return quantities;
    }

    /** Checks every product and copies its current price and cost onto the sale line. */
    private List<SaleItem> buildItems(Map<Integer, Integer> quantities) {
        List<SaleItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> line : quantities.entrySet()) {
            Product product = products.findById(line.getKey())
                    .orElseThrow(() -> new BusinessRuleException("Product " + line.getKey() + " does not exist"));
            if (!product.isActive()) {
                throw new BusinessRuleException(product.getProductName() + " is not available for sale");
            }
            if (product.getQuantityInStock() < line.getValue()) {
                throw new InsufficientStockException(product.getProductName(), product.getQuantityInStock(), line.getValue());
            }
            items.add(new SaleItem(product.getProductId(), line.getValue(), product.getSellingPrice(), product.getCostPrice()));
        }
        return items;
    }

    /** (subtotal - discount) plus tax, rounded to 2 decimals. */
    private BigDecimal totalOf(BigDecimal subtotal, BigDecimal discount, BigDecimal taxRate) {
        BigDecimal factor = BigDecimal.ONE.add(taxRate.movePointLeft(2));
        return subtotal.subtract(discount).multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
