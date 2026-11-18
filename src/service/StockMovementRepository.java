package service;

import model.Stock_movement;

public interface StockMovementRepository {
    /** Inserts the movement and fills in movementId. */
    Stock_movement save(Stock_movement movement);
}
