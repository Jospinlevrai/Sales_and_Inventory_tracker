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
import java.util.*;

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
        BigDecimal discount = Text.money(orZero(request.getDiscount()), "Discount");
        BigDecimal taxRate = Text.money(orZero(request.getTaxRate()), "Tax rate");
        if (taxRate.compareTo(HUNDRED) > 0) {
            throw new BusinessRuleException("The tax rate must be between 0 and 100");
        }
        Map<Integer, Integer> quantities = mergeLines(request.getLines());

        return transactions.execute(() -> {
            List<Sale_item> items = buildItems(quantities);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (Sale_item item : items) {
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

            for (Sale_item item : items) {
                item.setSaleId(sale.getSaleId());
                sales.saveItem(item);
                ledger.record(item.getProductId(), sale.getUserId(), Stock_movement.MovementType.SALE,
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
            if (sale.getStatus() == Sale.Status.REFUNDED) {
                throw new BusinessRuleException("Sale " + sale.getReceiptNo() + " is already refunded");
            }
            for (Sale_item item : sales.findItems(saleId)) {
                if (sales.refundedQuantity(saleId, item.getProductId()) > 0) {
                    throw new BusinessRuleException("A sale with refunds cannot be voided");
                }
            }
            sales.updateStatus(saleId, Sale.Status.VOIDED);
            for (Sale_item item : sales.findItems(saleId)) {
                ledger.record(item.getProductId(), userId, Stock_movement.MovementType.SALE_VOID,
                        item.getQuantity(), saleId, null, reason.trim());
            }
        });
    }

    @Override
    public void refundSale(int saleId, int userId, Map<Integer, Integer> quantities, String reason) {
        if (userId <= 0) {
            throw new BusinessRuleException("The user refunding the sale is missing");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessRuleException("Give a reason for the refund");
        }
        if (quantities == null || quantities.isEmpty()) {
            throw new BusinessRuleException("Choose at least one item to refund");
        }

        transactions.run(() -> {
            Sale sale = sales.findById(saleId)
                    .orElseThrow(() -> new BusinessRuleException("Sale " + saleId + " does not exist"));
            if (sale.getStatus() == Sale.Status.VOIDED) {
                throw new BusinessRuleException("A voided sale cannot be refunded");
            }

            Map<Integer, Integer> original = new HashMap<>();
            for (Sale_item item : sales.findItems(saleId)) {
                original.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            }

            boolean fullyRefunded = true;
            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();
                if (!original.containsKey(productId)) {
                    throw new BusinessRuleException("Product " + productId + " is not on the sale");
                }
                if (quantity <= 0) {
                    throw new BusinessRuleException("Refund quantities must be at least 1");
                }

                int alreadyRefunded = sales.refundedQuantity(saleId, productId);
                int maximum = original.get(productId) - alreadyRefunded;
                if (quantity > maximum) {
                    throw new BusinessRuleException("Only " + maximum + " units of product " + productId
                            + " can be refunded");
                }

                ledger.record(productId, userId, Stock_movement.MovementType.SALE_REFUND,
                        quantity, saleId, null, reason.trim());
            }

            for (Map.Entry<Integer, Integer> entry : original.entrySet()) {
                int refunded = sales.refundedQuantity(saleId, entry.getKey());
                if (refunded < entry.getValue()) {
                    fullyRefunded = false;
                    break;
                }
            }
            if (fullyRefunded) {
                sales.updateStatus(saleId, Sale.Status.REFUNDED);
            }
        });
    }

    @Override
    public java.util.List<Sale_item> getSaleItems(int saleId) {
        return sales.findItems(saleId);
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
            if (line.getProductId() <= 0) {
                throw new BusinessRuleException("Choose a valid product");
            }
            if (line.getQuantity() < 1 || line.getQuantity() > 1_000_000) {
                throw new BusinessRuleException("Each sale quantity must be between 1 and 1000000");
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
    private List<Sale_item> buildItems(Map<Integer, Integer> quantities) {
        List<Sale_item> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> line : quantities.entrySet()) {
            Product product = products.findById(line.getKey())
                    .orElseThrow(() -> new BusinessRuleException("Product " + line.getKey() + " does not exist"));
            if (!product.isActive()) {
                throw new BusinessRuleException(product.getProductName() + " is not available for sale");
            }
            if (product.getQuantityInStock() < line.getValue()) {
                throw new InsufficientStockException(product.getProductName(), product.getQuantityInStock(), line.getValue());
            }
            items.add(new Sale_item(product.getProductId(), line.getValue(), product.getSellingPrice(), product.getCostPrice()));
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
