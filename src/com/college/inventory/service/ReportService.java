package com.college.inventory.service;

import com.college.inventory.model.IssueRecord;
import com.college.inventory.model.Item;
import com.college.inventory.model.StockTransaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles exporting reports to CSV format for college administrative and audit requirements.
 */
public class ReportService {
    private final InventoryService inventoryService;

    public ReportService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public boolean exportInventoryToCsv(File file) {
        List<Item> items = inventoryService.getItemDao().getAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Item Code,Name,Category,Department,Quantity,Min Threshold,Unit Price (INR),Total Valuation (INR),Location,Status,Supplier");
            for (Item it : items) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f,%.2f,\"%s\",\"%s\",\"%s\"%n",
                    escapeCsv(it.getItemCode()),
                    escapeCsv(it.getName()),
                    escapeCsv(it.getCategoryName() != null ? it.getCategoryName() : "N/A"),
                    escapeCsv(it.getDepartmentName() != null ? it.getDepartmentName() : "N/A"),
                    it.getQuantity(),
                    it.getMinThreshold(),
                    it.getUnitPrice(),
                    it.getTotalValuation(),
                    escapeCsv(it.getLocation() != null ? it.getLocation() : ""),
                    escapeCsv(it.getConditionStatus()),
                    escapeCsv(it.getSupplierName() != null ? it.getSupplierName() : "N/A")
                );
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportIssuesToCsv(File file, String statusFilter) {
        List<IssueRecord> records = inventoryService.getIssueRecordDao().getAll(statusFilter);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Record ID,Item Code,Item Name,Borrower Type,Borrower ID,Borrower Name,Borrower Dept,Email,Quantity,Issue Date,Due Date,Return Date,Status,Remarks");
            for (IssueRecord r : records) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    r.getId(),
                    escapeCsv(r.getItemCode()),
                    escapeCsv(r.getItemName()),
                    escapeCsv(r.getBorrowerType()),
                    escapeCsv(r.getBorrowerId()),
                    escapeCsv(r.getBorrowerName()),
                    escapeCsv(r.getBorrowerDepartment()),
                    escapeCsv(r.getBorrowerEmail()),
                    r.getQuantity(),
                    escapeCsv(r.getIssueDate()),
                    escapeCsv(r.getExpectedReturnDate()),
                    escapeCsv(r.getActualReturnDate() != null ? r.getActualReturnDate() : "N/A"),
                    escapeCsv(r.getStatus()),
                    escapeCsv(r.getRemarks() != null ? r.getRemarks() : "")
                );
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportAuditLogToCsv(File file) {
        List<StockTransaction> logs = inventoryService.getTransactionDao().getAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Log ID,Item Code,Item Name,Transaction Type,Qty Change,Resulting Qty,Reference / Note,Performed By,Timestamp");
            for (StockTransaction t : logs) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",%+d,%d,\"%s\",\"%s\",\"%s\"%n",
                    t.getId(),
                    escapeCsv(t.getItemCode()),
                    escapeCsv(t.getItemName()),
                    escapeCsv(t.getTransactionType()),
                    t.getQuantityChange(),
                    t.getResultingQuantity(),
                    escapeCsv(t.getReferenceNote()),
                    escapeCsv(t.getPerformedBy()),
                    escapeCsv(t.getTimestamp())
                );
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
