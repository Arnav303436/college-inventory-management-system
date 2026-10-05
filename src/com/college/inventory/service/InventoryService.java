package com.college.inventory.service;

import com.college.inventory.dao.*;
import com.college.inventory.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core business logic for College Inventory operations, transactions, and stock validation.
 */
public class InventoryService {
    private final ItemDao itemDao;
    private final IssueRecordDao issueRecordDao;
    private final StockTransactionDao transactionDao;
    private final DepartmentDao departmentDao;
    private final CategoryDao categoryDao;
    private final SupplierDao supplierDao;

    public InventoryService() {
        this.itemDao = new ItemDao();
        this.issueRecordDao = new IssueRecordDao();
        this.transactionDao = new StockTransactionDao();
        this.departmentDao = new DepartmentDao();
        this.categoryDao = new CategoryDao();
        this.supplierDao = new SupplierDao();
    }

    public synchronized String addItem(Item item, String performedBy) {
        if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
            return "Error: Item code is required.";
        }
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            return "Error: Item name is required.";
        }
        if (item.getQuantity() < 0) {
            return "Error: Initial quantity cannot be negative.";
        }
        if (itemDao.getByCode(item.getItemCode().trim()) != null) {
            return "Error: An item with code '" + item.getItemCode().trim() + "' already exists.";
        }

        boolean created = itemDao.add(item);
        if (!created) {
            return "Error: Failed to insert item into database.";
        }

        // Record initial stock transaction
        if (item.getQuantity() > 0) {
            StockTransaction tx = new StockTransaction(
                item.getId(),
                "STOCK_IN",
                item.getQuantity(),
                item.getQuantity(),
                "Initial catalog entry",
                performedBy != null ? performedBy : "System"
            );
            transactionDao.recordTransaction(tx);
        }

        return "SUCCESS";
    }

    public synchronized String updateItem(Item item, String performedBy) {
        Item existing = itemDao.getById(item.getId());
        if (existing == null) {
            return "Error: Item not found.";
        }
        // Verify unique code if changed
        Item codeCheck = itemDao.getByCode(item.getItemCode());
        if (codeCheck != null && codeCheck.getId() != item.getId()) {
            return "Error: Another item already uses the code '" + item.getItemCode() + "'.";
        }

        boolean updated = itemDao.update(item);
        if (!updated) {
            return "Error: Failed to update item.";
        }

        // If quantity was altered directly in edit dialog
        if (existing.getQuantity() != item.getQuantity()) {
            int diff = item.getQuantity() - existing.getQuantity();
            StockTransaction tx = new StockTransaction(
                item.getId(),
                diff > 0 ? "STOCK_IN" : "ADJUSTMENT",
                diff,
                item.getQuantity(),
                "Direct quantity adjustment in catalog editor",
                performedBy != null ? performedBy : "System"
            );
            transactionDao.recordTransaction(tx);
        }

        return "SUCCESS";
    }

    public synchronized String adjustStock(int itemId, int quantityChange, String transactionType, String reason, String performedBy) {
        Item item = itemDao.getById(itemId);
        if (item == null) {
            return "Error: Item not found.";
        }
        int newQty = item.getQuantity() + quantityChange;
        if (newQty < 0) {
            return "Error: Cannot reduce stock below zero. Current stock: " + item.getQuantity();
        }

        boolean updated = itemDao.updateQuantity(itemId, newQty);
        if (!updated) {
            return "Error: Failed to update stock quantity.";
        }

        StockTransaction tx = new StockTransaction(
            itemId,
            transactionType,
            quantityChange,
            newQty,
            reason,
            performedBy != null ? performedBy : "System"
        );
        transactionDao.recordTransaction(tx);

        return "SUCCESS";
    }

    public synchronized String issueItem(int itemId, String borrowerType, String borrowerId,
                                        String borrowerName, String borrowerEmail, String borrowerDept,
                                        int qty, String dueDate, String remarks, String performedBy) {
        if (qty <= 0) {
            return "Error: Issue quantity must be at least 1.";
        }
        Item item = itemDao.getById(itemId);
        if (item == null) {
            return "Error: Item not found.";
        }
        if (item.getQuantity() < qty) {
            return "Error: Insufficient stock. Available: " + item.getQuantity() + ", Requested: " + qty;
        }
        if (borrowerName == null || borrowerName.trim().isEmpty()) {
            return "Error: Borrower name is required.";
        }
        if (borrowerId == null || borrowerId.trim().isEmpty()) {
            return "Error: Borrower ID / Roll No is required.";
        }

        // 1. Decrement stock
        int newQty = item.getQuantity() - qty;
        boolean stockUpdated = itemDao.updateQuantity(itemId, newQty);
        if (!stockUpdated) {
            return "Error: Failed to update stock for item.";
        }

        // 2. Add Issue Record
        IssueRecord record = new IssueRecord(
            itemId,
            borrowerType,
            borrowerId.trim(),
            borrowerName.trim(),
            borrowerEmail != null ? borrowerEmail.trim() : "",
            borrowerDept != null ? borrowerDept.trim() : "",
            qty,
            dueDate,
            remarks
        );
        boolean recordSaved = issueRecordDao.add(record);
        if (!recordSaved) {
            // Rollback stock
            itemDao.updateQuantity(itemId, item.getQuantity());
            return "Error: Failed to save issue record.";
        }

        // 3. Log Stock Transaction
        StockTransaction tx = new StockTransaction(
            itemId,
            "ISSUE",
            -qty,
            newQty,
            "Issued to " + borrowerType + " " + borrowerName + " (" + borrowerId + ")",
            performedBy != null ? performedBy : "Staff"
        );
        transactionDao.recordTransaction(tx);

        return "SUCCESS";
    }

    public synchronized String returnItem(int issueRecordId, String returnRemarks, String performedBy) {
        IssueRecord record = issueRecordDao.getById(issueRecordId);
        if (record == null) {
            return "Error: Issue record not found.";
        }
        if (!"ISSUED".equalsIgnoreCase(record.getStatus())) {
            return "Error: This record is already marked as " + record.getStatus();
        }

        Item item = itemDao.getById(record.getItemId());
        if (item == null) {
            return "Error: Associated inventory item not found.";
        }

        // 1. Increment Stock
        int newQty = item.getQuantity() + record.getQuantity();
        boolean stockUpdated = itemDao.updateQuantity(item.getId(), newQty);
        if (!stockUpdated) {
            return "Error: Failed to restore stock quantity.";
        }

        // 2. Mark Record as RETURNED
        boolean marked = issueRecordDao.markReturned(issueRecordId, null, returnRemarks);
        if (!marked) {
            // Rollback stock
            itemDao.updateQuantity(item.getId(), item.getQuantity());
            return "Error: Failed to update issue status.";
        }

        // 3. Log Stock Transaction
        StockTransaction tx = new StockTransaction(
            item.getId(),
            "RETURN",
            record.getQuantity(),
            newQty,
            "Returned by " + record.getBorrowerName() + " (" + record.getBorrowerId() + ") - " + (returnRemarks != null ? returnRemarks : "OK"),
            performedBy != null ? performedBy : "Staff"
        );
        transactionDao.recordTransaction(tx);

        return "SUCCESS";
    }

    public synchronized String deleteItem(int itemId) {
        Item item = itemDao.getById(itemId);
        if (item == null) {
            return "Error: Item not found.";
        }
        boolean deleted = itemDao.delete(itemId);
        if (deleted) {
            return "SUCCESS";
        }
        return "Error: Failed to delete item. It may be referenced in transactions or active loans.";
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Item> allItems = itemDao.getAll();
        int uniqueItems = allItems.size();
        int totalUnits = 0;
        int lowStockCount = 0;
        double totalValuation = 0.0;

        for (Item it : allItems) {
            totalUnits += it.getQuantity();
            totalValuation += it.getTotalValuation();
            if (it.isLowStock()) {
                lowStockCount++;
            }
        }

        int activeLoans = issueRecordDao.getActiveIssuedCount();
        int totalSuppliers = supplierDao.getAll().size();
        int totalDepartments = departmentDao.getAll().size();

        stats.put("uniqueItems", uniqueItems);
        stats.put("totalUnits", totalUnits);
        stats.put("lowStockCount", lowStockCount);
        stats.put("totalValuation", totalValuation);
        stats.put("activeLoans", activeLoans);
        stats.put("totalSuppliers", totalSuppliers);
        stats.put("totalDepartments", totalDepartments);
        return stats;
    }

    // Accessors for DAOs
    public ItemDao getItemDao() { return itemDao; }
    public IssueRecordDao getIssueRecordDao() { return issueRecordDao; }
    public StockTransactionDao getTransactionDao() { return transactionDao; }
    public DepartmentDao getDepartmentDao() { return departmentDao; }
    public CategoryDao getCategoryDao() { return categoryDao; }
    public SupplierDao getSupplierDao() { return supplierDao; }
}
