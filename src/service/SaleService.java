package service;

import dto.SaleRequest;
import model.Sale;

public interface SaleService {
    /** Records a sale at the products' current prices and reduces stock, all or nothing. */
    Sale completeSale(SaleRequest request);

    /** Cancels a completed sale and puts its items back in stock. */
    void voidSale(int saleId, int userId, String reason);
}
