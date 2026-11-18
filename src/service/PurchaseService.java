package service;

import dto.PurchaseRequest;
import model.Purchase;

public interface PurchaseService {
    /** Records a supplier delivery, raises stock and updates each product's cost price, all or nothing. */
    Purchase recordDelivery(PurchaseRequest request);
}
