package service;

import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {
    private final DBConnection database;

    public JdbcCategoryRepository(DBConnection database) {
        this.database = database;
    }

    @Override
    public Optional<Category> findById(int categoryId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Categories WHERE category_id = ?")) {
                statement.setInt(1, categoryId);
                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        return Optional.of(map(rows));
                    }
                    return Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Category> findAll() {
        return database.withConnection(connection -> {
            List<Category> categories = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Categories ORDER BY category_name");
                 ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    categories.add(map(rows));
                }
            }
            return categories;
        });
    }

    @Override
    public boolean nameExists(String name, int excludeCategoryId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM Categories WHERE LOWER(category_name) = LOWER(?) AND category_id <> ?")) {
                statement.setString(1, name);
                statement.setInt(2, excludeCategoryId);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        });
    }

    @Override
    public Category save(Category category) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Categories(category_name, description) VALUES (?, ?) RETURNING category_id")) {
                statement.setString(1, category.getCategoryName());
                statement.setString(2, category.getDescription());
                try (ResultSet generated = statement.executeQuery()) {
                    generated.next();
                    category.setCategoryId(generated.getInt("category_id"));
                }
                return category;
            }
        });
    }

    @Override
    public void update(Category category) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Categories SET category_name = ?, description = ? WHERE category_id = ?")) {
                statement.setString(1, category.getCategoryName());
                statement.setString(2, category.getDescription());
                statement.setInt(3, category.getCategoryId());
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public void delete(int categoryId) {
        database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Categories WHERE category_id = ?")) {
                statement.setInt(1, categoryId);
                return statement.executeUpdate();
            }
        });
    }

    @Override
    public boolean hasProducts(int categoryId) {
        return database.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Products WHERE category_id = ? LIMIT 1")) {
                statement.setInt(1, categoryId);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        });
    }

    private static Category map(ResultSet rows) throws SQLException {
        return new Category(rows.getInt("category_id"), rows.getString("category_name"), rows.getString("description"));
    }
}
