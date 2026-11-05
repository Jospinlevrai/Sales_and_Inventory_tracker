package model;

import java.time.LocalDateTime;

public class Supplier {
    private int supplierId;
    private String supplierName;
    private String contactPerson;
    private String phoneNumber;
    private String email;
    private String address;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Supplier() {
    }

    public Supplier(int supplierId, String supplierName, String contactPerson, String phoneNumber,
                    String email, String address, boolean active, LocalDateTime createdAt) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Supplier{supplierId=" + supplierId + ", supplierName=" + supplierName
                + ", phoneNumber=" + phoneNumber + ", active=" + active + "}";
    }
}