package com.college.inventory.model;

/**
 * Represents an asset or consumable item tracked in the college inventory.
 */
public class Item {
    private int id;
    private String itemCode;
    private String name;
    private int categoryId;
    private String categoryName; // helper for display
    private int departmentId;
    private String departmentName; // helper for display
    private int quantity;
    private int minThreshold;
    private double unitPrice;
    private String location;
    private String conditionStatus; // "Working", "Under Maintenance", "Damaged", "Scrapped"
    private int supplierId;
    private String supplierName; // helper for display
    private String description;
    private String lastUpdated;

    public Item() {}

    public Item(int id, String itemCode, String name, int categoryId, int departmentId,
                int quantity, int minThreshold, double unitPrice, String location,
                String conditionStatus, int supplierId, String description, String lastUpdated) {
        this.id = id;
        this.itemCode = itemCode;
        this.name = name;
        this.categoryId = categoryId;
        this.departmentId = departmentId;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.unitPrice = unitPrice;
        this.location = location;
        this.conditionStatus = conditionStatus;
        this.supplierId = supplierId;
        this.description = description;
        this.lastUpdated = lastUpdated;
    }

    public Item(String itemCode, String name, int categoryId, int departmentId,
                int quantity, int minThreshold, double unitPrice, String location,
                String conditionStatus, int supplierId, String description) {
        this(-1, itemCode, name, categoryId, departmentId, quantity, minThreshold,
             unitPrice, location, conditionStatus, supplierId, description, null);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinThreshold() { return minThreshold; }
    public void setMinThreshold(int minThreshold) { this.minThreshold = minThreshold; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTotalValuation() {
        return quantity * unitPrice;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isLowStock() {
        return quantity <= minThreshold;
    }

    @Override
    public String toString() {
        return itemCode + " - " + name + " (Qty: " + quantity + ")";
    }
}
