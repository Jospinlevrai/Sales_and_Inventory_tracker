package service;

import model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> listCategories();

    /** Administrators and managers. */
    Category addCategory(int actingUserId, Category category);

    /** Administrators and managers. */
    Category updateCategory(int actingUserId, Category category);

    /** Administrators and managers. Only a category with no products can be deleted. */
    void deleteCategory(int actingUserId, int categoryId);
}
