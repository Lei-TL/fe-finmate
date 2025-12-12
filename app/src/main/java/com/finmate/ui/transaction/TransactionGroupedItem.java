package com.finmate.ui.transaction;

/**
 * Model để group transactions theo ngày
 * Có 2 loại: HEADER (hiển thị ngày) và TRANSACTION (hiển thị transaction)
 */
public class TransactionGroupedItem {
    
    public enum ItemType {
        HEADER,  // Header ngày
        TRANSACTION  // Transaction item
    }
    
    private ItemType type;
    private String dateHeader;  // Format: "dd/MM/yyyy" (cho HEADER)
    private String dayOfWeek;   // Format: "Thứ 2", "Thứ 3", ... (cho HEADER)
    private TransactionUIModel transaction;  // Transaction data (cho TRANSACTION)
    
    // Constructor cho HEADER
    public TransactionGroupedItem(String dateHeader, String dayOfWeek) {
        this.type = ItemType.HEADER;
        this.dateHeader = dateHeader;
        this.dayOfWeek = dayOfWeek;
    }
    
    // Constructor cho TRANSACTION
    public TransactionGroupedItem(TransactionUIModel transaction) {
        this.type = ItemType.TRANSACTION;
        this.transaction = transaction;
    }
    
    public ItemType getType() {
        return type;
    }
    
    public String getDateHeader() {
        return dateHeader;
    }
    
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    
    public TransactionUIModel getTransaction() {
        return transaction;
    }
}



