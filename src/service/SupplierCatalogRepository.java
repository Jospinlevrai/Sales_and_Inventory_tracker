package service;

import model.Supplier;

import java.util.List;

public interface SupplierCatalogRepository {
    List<Supplier> findAll(boolean activeOnly);

    /** Inserts the supplier and fills in supplierId. */
    Supplier save(Supplier supplier);

    /** Updates everything except the active flag. */
    void update(Supplier supplier);

    void setActive(int supplierId, boolean active);

    boolean nameExists(String name, int excludeSupplierId);
}
