package service;

import model.Purchase;
import model.PurchaseItem;

public interface PurchaseRepository {
    /** Inserts the purchase and fills in purchaseId. */
    Purchase save(Purchase purchase);

    /** Inserts one line of the purchase given by item.getPurchaseId(). */
    PurchaseItem saveItem(PurchaseItem item);

    boolean invoiceExists(int supplierId, String invoiceNo);
}
