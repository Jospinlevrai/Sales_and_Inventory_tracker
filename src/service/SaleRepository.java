package service;

import model.Sale;
import model.Sale_item;

import java.util.List;
import java.util.Optional;

public interface SaleRepository {
    /** Inserts the sale and fills in saleId and receiptNo, which the database generates. */
    Sale save(Sale sale);

    /** Inserts one line of the sale given by item.getSaleId(). */
    SaleItem saveItem(SaleItem item);

    Optional<Sale> findById(int saleId);

    List<SaleItem> findItems(int saleId);

    void updateStatus(int saleId, Sale.Status status);
}
