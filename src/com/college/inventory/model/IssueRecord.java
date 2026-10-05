package com.college.inventory.model;

/**
 * Tracks items issued to Students, Faculty, or Staff members for projects, labs, or lectures.
 */
public class IssueRecord {
    private int id;
    private int itemId;
    private String itemCode; // helper for display
    private String itemName; // helper for display
    private String borrowerType; // "STUDENT", "FACULTY", "STAFF"
    private String borrowerId;   // Roll number or Faculty Emp ID
    private String borrowerName;
    private String borrowerEmail;
    private String borrowerDepartment;
    private int quantity;
    private String issueDate;
    private String expectedReturnDate;
    private String actualReturnDate;
    private String status; // "ISSUED", "RETURNED", "OVERDUE", "LOST", "DAMAGED"
    private String remarks;

    public IssueRecord() {}

    public IssueRecord(int id, int itemId, String borrowerType, String borrowerId,
                       String borrowerName, String borrowerEmail, String borrowerDepartment,
                       int quantity, String issueDate, String expectedReturnDate,
                       String actualReturnDate, String status, String remarks) {
        this.id = id;
        this.itemId = itemId;
        this.borrowerType = borrowerType;
        this.borrowerId = borrowerId;
        this.borrowerName = borrowerName;
        this.borrowerEmail = borrowerEmail;
        this.borrowerDepartment = borrowerDepartment;
        this.quantity = quantity;
        this.issueDate = issueDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.status = status;
        this.remarks = remarks;
    }

    public IssueRecord(int itemId, String borrowerType, String borrowerId,
                       String borrowerName, String borrowerEmail, String borrowerDepartment,
                       int quantity, String expectedReturnDate, String remarks) {
        this(-1, itemId, borrowerType, borrowerId, borrowerName, borrowerEmail,
             borrowerDepartment, quantity, null, expectedReturnDate, null, "ISSUED", remarks);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getBorrowerType() { return borrowerType; }
    public void setBorrowerType(String borrowerType) { this.borrowerType = borrowerType; }

    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public String getBorrowerEmail() { return borrowerEmail; }
    public void setBorrowerEmail(String borrowerEmail) { this.borrowerEmail = borrowerEmail; }

    public String getBorrowerDepartment() { return borrowerDepartment; }
    public void setBorrowerDepartment(String borrowerDepartment) { this.borrowerDepartment = borrowerDepartment; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(String expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public String getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(String actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isCurrentlyIssued() {
        return "ISSUED".equalsIgnoreCase(status) || "OVERDUE".equalsIgnoreCase(status);
    }
}
