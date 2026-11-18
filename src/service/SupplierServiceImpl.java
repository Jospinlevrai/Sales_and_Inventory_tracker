package service;

import exception.BusinessRuleException;
import model.Role;
import model.Supplier;

import java.util.List;

public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository suppliers;
    private final SupplierCatalogRepository catalog;
    private final TransactionManager transactions;
    private final AccessControl access;

    public SupplierServiceImpl(SupplierRepository suppliers, SupplierCatalogRepository catalog,
                               UserRepository users, RoleRepository roles, TransactionManager transactions) {
        this.suppliers = suppliers;
        this.catalog = catalog;
        this.transactions = transactions;
        this.access = new AccessControl(users, roles);
    }

    @Override
    public List<Supplier> listSuppliers(boolean activeOnly) {
        return catalog.findAll(activeOnly);
    }

    @Override
    public Supplier addSupplier(int actingUserId, Supplier input) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        return transactions.execute(() -> catalog.save(clean(input, 0)));
    }

    @Override
    public Supplier updateSupplier(int actingUserId, Supplier input) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        return transactions.execute(() -> {
            if (input == null) {
                throw new BusinessRuleException("The supplier is missing");
            }
            Supplier existing = suppliers.findById(input.getSupplierId())
                    .orElseThrow(() -> new BusinessRuleException("The supplier does not exist"));
            Supplier supplier = clean(input, existing.getSupplierId());
            supplier.setSupplierId(existing.getSupplierId());
            supplier.setActive(existing.isActive());
            catalog.update(supplier);
            return supplier;
        });
    }

    @Override
    public void setSupplierActive(int actingUserId, int supplierId, boolean active) {
        access.require(actingUserId, Role.ADMIN, Role.MANAGER);
        transactions.run(() -> {
            suppliers.findById(supplierId).orElseThrow(() -> new BusinessRuleException("The supplier does not exist"));
            catalog.setActive(supplierId, active);
        });
    }

    private Supplier clean(Supplier input, int excludeId) {
        if (input == null) {
            throw new BusinessRuleException("The supplier is missing");
        }
        String name = Text.required(input.getSupplierName(), 150, "Supplier name");
        if (catalog.nameExists(name, excludeId)) {
            throw new BusinessRuleException("A supplier called " + name + " already exists");
        }
        Supplier supplier = new Supplier();
        supplier.setSupplierName(name);
        supplier.setContactPerson(Text.optional(input.getContactPerson(), 100, "Contact person"));
        supplier.setPhoneNumber(Text.optionalPhone(input.getPhoneNumber()));
        supplier.setEmail(Text.optionalEmail(input.getEmail()));
        supplier.setAddress(Text.optional(input.getAddress(), 500, "Address"));
        return supplier;
    }
}
