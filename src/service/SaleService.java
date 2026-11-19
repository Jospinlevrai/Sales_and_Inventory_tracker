package service;

import dto.SaleRequest;
import model.Sale;

public interface SaleService {
    
    Sale completeSale(SaleRequest request);

    /** Cancels a completed sale and puts its items back in stock. */
    void voidSale(int saleId, @ActingUser int userId, String reason);
}
