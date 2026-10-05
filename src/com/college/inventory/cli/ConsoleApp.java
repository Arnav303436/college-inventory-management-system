package com.college.inventory.cli;

import com.college.inventory.model.*;
import com.college.inventory.service.AuthService;
import com.college.inventory.service.InventoryService;
import com.college.inventory.service.ReportService;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Text-based interactive console interface for terminal environments.
 */
public class ConsoleApp {
    private final InventoryService inventoryService;
    private final AuthService authService;
    private final ReportService reportService;
    private final Scanner scanner;

    public ConsoleApp(InventoryService inventoryService, AuthService authService) {
        this.inventoryService = inventoryService;
        this.authService = authService;
        this.reportService = new ReportService(inventoryService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printBanner();

        if (!loginFlow()) {
            System.out.println("Login cancelled or failed. Exiting...");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Select an option (1-9): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewDashboard();
                case "2" -> listAllItems();
                case "3" -> searchItems();
                case "4" -> addNewItem();
                case "5" -> adjustStock();
                case "6" -> issueItem();
                case "7" -> returnItem();
                case "8" -> exportReports();
                case "9" -> {
                    System.out.println("\nThank you for using CampusAsset Pro. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please enter a number between 1 and 9.");
            }
        }
    }

    private void printBanner() {
        System.out.println("==================================================================");
        System.out.println("     CAMPUSASSET PRO - COLLEGE INVENTORY MANAGEMENT SYSTEM       ");
        System.out.println("       Robust SQLite Database-Driven Asset Tracking Console       ");
        System.out.println("==================================================================");
    }

    private boolean loginFlow() {
        System.out.println("\n--- User Authentication ---");
        System.out.println("(Default login: 'admin' with password 'admin123')");
        for (int i = 0; i < 3; i++) {
            System.out.print("Username: ");
            String user = scanner.nextLine().trim();
            System.out.print("Password: ");
            String pass = scanner.nextLine().trim();

            if (authService.login(user, pass)) {
                System.out.println("\n>>> Login successful! Welcome, " + authService.getCurrentUser().getFullName() + " (" + authService.getCurrentUser().getRole() + ") <<<\n");
                return true;
            } else {
                System.out.println("Invalid username or password. Remaining attempts: " + (2 - i));
            }
        }
        return false;
    }

    private void printMainMenu() {
        System.out.println("--------------------------------------------------");
        System.out.println("                 MAIN MENU                        ");
        System.out.println("--------------------------------------------------");
        System.out.println(" [1] View Inventory Dashboard & Statistics");
        System.out.println(" [2] List All Inventory Items / Assets");
        System.out.println(" [3] Search Inventory Items");
        System.out.println(" [4] Register New Equipment / Asset");
        System.out.println(" [5] Adjust Stock (Restock / Scrap / Audit)");
        System.out.println(" [6] Issue Equipment to Student / Faculty");
        System.out.println(" [7] Process Item Return");
        System.out.println(" [8] Export CSV Reports");
        System.out.println(" [9] Exit Application");
        System.out.println("--------------------------------------------------");
    }

    private void viewDashboard() {
        Map<String, Object> stats = inventoryService.getDashboardStats();
        System.out.println("\n========== CAMPUS INVENTORY DASHBOARD ==========");
        System.out.println(" Total Units in Stock : " + stats.get("totalUnits"));
        System.out.println(" Unique Catalog Items : " + stats.get("uniqueItems"));
        System.out.println(" Active Equipment Loans: " + stats.get("activeLoans"));
        System.out.println(" Low-Stock Alert Items: " + stats.get("lowStockCount"));
        System.out.printf(" Total Asset Valuation: INR ₹ %,.2f%n", (Double) stats.get("totalValuation"));
        System.out.println(" Total Departments    : " + stats.get("totalDepartments"));
        System.out.println(" Registered Vendors   : " + stats.get("totalSuppliers"));
        System.out.println("================================================\n");
    }

    private void listAllItems() {
        List<Item> items = inventoryService.getItemDao().getAll();
        printItemTable(items);
    }

    private void searchItems() {
        System.out.print("\nEnter keyword to search (code, name, lab, or notes): ");
        String kw = scanner.nextLine().trim();
        List<Item> items = inventoryService.getItemDao().search(kw, null, null, false);
        printItemTable(items);
    }

    private void printItemTable(List<Item> items) {
        if (items.isEmpty()) {
            System.out.println("\nNo inventory items found.\n");
            return;
        }

        System.out.println("\n" + "=".repeat(110));
        System.out.printf("%-5s | %-12s | %-32s | %-6s | %-5s | %-12s | %-18s | %-10s%n",
            "ID", "Item Code", "Item Name", "Stock", "Min", "Price (₹)", "Department", "Status");
        System.out.println("-".repeat(110));

        for (Item it : items) {
            String status = it.isLowStock() ? "[LOW STOCK]" : "[OK]";
            String name = it.getName();
            if (name.length() > 30) name = name.substring(0, 27) + "...";

            String dept = it.getDepartmentName() != null ? it.getDepartmentName() : "N/A";
            if (dept.length() > 18) dept = dept.substring(0, 15) + "...";

            System.out.printf("%-5d | %-12s | %-32s | %-6d | %-5d | %-12.2f | %-18s | %-10s%n",
                it.getId(), it.getItemCode(), name, it.getQuantity(), it.getMinThreshold(),
                it.getUnitPrice(), dept, status);
        }
        System.out.println("=".repeat(110) + "\n");
    }

    private void addNewItem() {
        System.out.println("\n--- Register New Asset ---");
        System.out.print("Item Code (e.g. CS-PC-050): ");
        String code = scanner.nextLine().trim();

        System.out.print("Item / Asset Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Low Stock Threshold: ");
        int threshold = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Unit Price (INR ₹): ");
        double price = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Location (e.g. Lab 2, Rack 1): ");
        String loc = scanner.nextLine().trim();

        Item item = new Item(code, name, 1, 1, qty, threshold, price, loc, "Working", 1, "Registered via CLI");
        String result = inventoryService.addItem(item, authService.getCurrentUser().getFullName());
        if ("SUCCESS".equalsIgnoreCase(result)) {
            System.out.println(">>> Item created successfully with ID: " + item.getId() + " <<<\n");
        } else {
            System.out.println("Failed to add item: " + result + "\n");
        }
    }

    private void adjustStock() {
        System.out.print("\nEnter Item ID to adjust: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        Item item = inventoryService.getItemDao().getById(id);
        if (item == null) {
            System.out.println("Item not found with ID: " + id);
            return;
        }

        System.out.println("Current Stock for " + item.getItemCode() + " (" + item.getName() + "): " + item.getQuantity() + " units");
        System.out.println("1. Restock / Receive Units (+)");
        System.out.println("2. Deduct Damaged / Scrapped Units (-)");
        System.out.print("Choose action (1 or 2): ");
        String act = scanner.nextLine().trim();

        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Reason / Reference: ");
        String reason = scanner.nextLine().trim();

        int delta = "2".equals(act) ? -qty : qty;
        String txType = "2".equals(act) ? "SCRAP" : "STOCK_IN";

        String res = inventoryService.adjustStock(id, delta, txType, reason, authService.getCurrentUser().getFullName());
        if ("SUCCESS".equalsIgnoreCase(res)) {
            System.out.println(">>> Stock adjusted successfully! <<<\n");
        } else {
            System.out.println("Adjustment failed: " + res + "\n");
        }
    }

    private void issueItem() {
        System.out.println("\n--- Issue Equipment ---");
        System.out.print("Enter Item ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Borrower Type (STUDENT / FACULTY / STAFF): ");
        String bType = scanner.nextLine().trim().toUpperCase();

        System.out.print("Roll No or Faculty ID: ");
        String bId = scanner.nextLine().trim();

        System.out.print("Borrower Full Name: ");
        String bName = scanner.nextLine().trim();

        System.out.print("Borrower Department: ");
        String dept = scanner.nextLine().trim();

        System.out.print("Quantity to Issue: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Expected Return Date (YYYY-MM-DD): ");
        String date = scanner.nextLine().trim();

        System.out.print("Purpose / Remarks: ");
        String notes = scanner.nextLine().trim();

        String res = inventoryService.issueItem(id, bType, bId, bName, "", dept, qty, date, notes, authService.getCurrentUser().getFullName());
        if ("SUCCESS".equalsIgnoreCase(res)) {
            System.out.println(">>> Equipment issued successfully to " + bName + "! <<<\n");
        } else {
            System.out.println("Issue failed: " + res + "\n");
        }
    }

    private void returnItem() {
        System.out.println("\n--- Return Equipment ---");
        List<IssueRecord> active = inventoryService.getIssueRecordDao().getAll("ISSUED");
        if (active.isEmpty()) {
            System.out.println("No active equipment loans at this time.\n");
            return;
        }

        System.out.println("Active Loans:");
        for (IssueRecord r : active) {
            System.out.printf("  Record #%d: [%s] %s | Borrower: %s (%s) | Qty: %d | Due: %s%n",
                r.getId(), r.getItemCode(), r.getItemName(), r.getBorrowerName(), r.getBorrowerId(), r.getQuantity(), r.getExpectedReturnDate());
        }

        System.out.print("\nEnter Record ID to return: ");
        int recId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Condition / Return Remarks: ");
        String rem = scanner.nextLine().trim();

        String res = inventoryService.returnItem(recId, rem, authService.getCurrentUser().getFullName());
        if ("SUCCESS".equalsIgnoreCase(res)) {
            System.out.println(">>> Return processed and item stock restored! <<<\n");
        } else {
            System.out.println("Return failed: " + res + "\n");
        }
    }

    private void exportReports() {
        System.out.println("\n--- Export Reports to CSV ---");
        File dir = new File("exports");
        if (!dir.exists()) dir.mkdirs();

        File invFile = new File(dir, "inventory_export.csv");
        if (reportService.exportInventoryToCsv(invFile)) {
            System.out.println(" [✔] Exported Inventory Catalog to: " + invFile.getAbsolutePath());
        }

        File loansFile = new File(dir, "loans_export.csv");
        if (reportService.exportIssuesToCsv(loansFile, "ALL")) {
            System.out.println(" [✔] Exported Loan Records to: " + loansFile.getAbsolutePath());
        }

        File auditFile = new File(dir, "audit_log_export.csv");
        if (reportService.exportAuditLogToCsv(auditFile)) {
            System.out.println(" [✔] Exported Audit Log to: " + auditFile.getAbsolutePath());
        }
        System.out.println();
    }
}
