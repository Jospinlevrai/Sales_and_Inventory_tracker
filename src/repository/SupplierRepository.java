package service;

import model.Supplier;

import java.util.Optional;

public interface SupplierRepository {
    Optional<Supplier> findById(int supplierId);
}
