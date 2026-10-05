package com.college.inventory.model;

/**
 * Audit log recording any stock movement or modification.
 */
public class StockTransaction {
    private int id;
    private int itemId;
    private String itemCode; // helper for display
    private String itemName; // helper for display
    private String transactionType; // "STOCK_IN", "ISSUE", "RETURN", "SCRAP", "ADJUSTMENT"
    private int quantityChange; // +ve for additions/returns, -ve for issues/scraps
    private int resultingQuantity;
    private String referenceNote; // e.g. "Invoice #9081", "Loan to Roll 21CS045"
    private String performedBy;
    private String timestamp;

    public StockTransaction() {}

    public StockTransaction(int id, int itemId, String transactionType, int quantityChange,
                            int resultingQuantity, String referenceNote, String performedBy, String timestamp) {
        this.id = id;
        this.itemId = itemId;
        this.transactionType = transactionType;
        this.quantityChange = quantityChange;
        this.resultingQuantity = resultingQuantity;
        this.referenceNote = referenceNote;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public StockTransaction(int itemId, String transactionType, int quantityChange,
                            int resultingQuantity, String referenceNote, String performedBy) {
        this(-1, itemId, transactionType, quantityChange, resultingQuantity, referenceNote, performedBy, null);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }

    public int getResultingQuantity() { return resultingQuantity; }
    public void setResultingQuantity(int resultingQuantity) { this.resultingQuantity = resultingQuantity; }

    public String getReferenceNote() { return referenceNote; }
    public void setReferenceNote(String referenceNote) { this.referenceNote = referenceNote; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
