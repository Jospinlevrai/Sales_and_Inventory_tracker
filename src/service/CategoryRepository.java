package service;

import model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(int categoryId);

    List<Category> findAll();

    /** True when another category (not excludeCategoryId; pass 0 for none) has this name, ignoring case. */
    boolean nameExists(String name, int excludeCategoryId);

    /** Inserts the category and fills in categoryId. */
    Category save(Category category);

    void update(Category category);

    void delete(int categoryId);

    /** True when any product, active or not, uses this category. */
    boolean hasProducts(int categoryId);
}
