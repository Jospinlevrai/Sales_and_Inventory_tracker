package service;

import model.Sale;
import model.Sale_item;

import java.util.List;
import java.util.Optional;

public interface SaleRepository {
    /** Inserts the sale and fills in saleId and receiptNo, which the database generates. */
    Sale save(Sale sale);

    /** Inserts one line of the sale given by item.getSaleId(). */
    Sale_item saveItem(Sale_item item);

    Optional<Sale> findById(int saleId);

    List<Sale_item> findItems(int saleId);

    void updateStatus(int saleId, Sale.Status status);
}
