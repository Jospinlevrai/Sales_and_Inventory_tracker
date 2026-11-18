package service;

import exception.BusinessRuleException;
import model.Category;
import model.Role;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categories;
    private final TransactionManager transactions;
    private final AccessControl access;

    public CategoryServiceImpl(CategoryRepository categories, UserRepository users, RoleRepository roles,
                               TransactionManager transactions) {
        this.categories = categories;
        this.transactions = transactions;
        this.access = new AccessControl(users, roles);
    }

    @Override
    public List<Category> listCategories() {
        return categories.findAll();
    }

    @Override
    public Category addCategory(int actingUserId, Category input) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        return transactions.execute(() -> categories.save(clean(input, 0)));
    }

    @Override
    public Category updateCategory(int actingUserId, Category input) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        return transactions.execute(() -> {
            if (input == null || !categories.findById(input.getCategoryId()).isPresent()) {
                throw new BusinessRuleException("The category does not exist");
            }
            Category category = clean(input, input.getCategoryId());
            category.setCategoryId(input.getCategoryId());
            categories.update(category);
            return category;
        });
    }

    @Override
    public void deleteCategory(int actingUserId, int categoryId) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        transactions.run(() -> {
            Category category = categories.findById(categoryId)
                    .orElseThrow(() -> new BusinessRuleException("The category does not exist"));
            if (categories.hasProducts(categoryId)) {
                throw new BusinessRuleException(category.getCategoryName() + " still has products and cannot be deleted");
            }
            categories.delete(categoryId);
        });
    }

    private Category clean(Category input, int excludeId) {
        if (input == null) {
            throw new BusinessRuleException("The category is missing");
        }
        String name = Text.required(input.getCategoryName(), 100, "Category name");
        if (categories.nameExists(name, excludeId)) {
            throw new BusinessRuleException("A category called " + name + " already exists");
        }
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(Text.optional(input.getDescription(), 1000, "Description"));
        return category;
    }
}
