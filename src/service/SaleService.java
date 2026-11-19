package service;

import dto.SaleRequest;
import model.Sale;

public interface SaleService {
    
    Sale completeSale(SaleRequest request);

    /** Cancels a completed sale and puts its items back in stock. */
    void voidSale(int saleId, @ActingUser int userId, String reason);

    /** Returns quantities from a sale to stock and records a refund. */
    void refundSale(int saleId, @ActingUser int userId, java.util.Map<Integer, Integer> quantities, String reason);

    /** Returns the original lines for a sale. */
    java.util.List<model.Sale_item> getSaleItems(int saleId);
}
