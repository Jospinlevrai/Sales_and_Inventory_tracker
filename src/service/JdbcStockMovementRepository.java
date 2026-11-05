package service;

import model.Stock_movement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class JdbcStockMovementRepository implements StockMovementRepository {
    private final Database database;

    public JdbcStockMovementRepository(Database database) {
        this.database = database;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Stock_Movements(product_id, user_id, movement_type, quantity_change, sale_id, purchase_id, note, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING movement_id")) {
                statement.setInt(1, movement.getProductId());
                statement.setInt(2, movement.getUserId());
                statement.setString(3, movement.getMovementType().name());
                statement.setInt(4, movement.getQuantityChange());
                statement.setObject(5, movement.getSaleId(), Types.INTEGER);
                statement.setObject(6, movement.getPurchaseId(), Types.INTEGER);
                statement.setString(7, movement.getNote());
                statement.setObject(8, movement.getCreatedAt());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    movement.setMovementId(generated.getLong("movement_id"));
                }
                return movement;
            }
        });
    }
}
