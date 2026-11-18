package service;

import model.Supplier;

import java.util.List;

public interface SupplierService {
    List<Supplier> listSuppliers(boolean activeOnly);

    /** Administrators and managers. */
    Supplier addSupplier(int actingUserId, Supplier supplier);

    /** Administrators and managers. */
    Supplier updateSupplier(int actingUserId, Supplier supplier);

    /** Administrators and managers. Suppliers are deactivated, never deleted. */
    void setSupplierActive(int actingUserId, int supplierId, boolean active);
}
