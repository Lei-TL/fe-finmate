package com.finmate.data.local.database.entity;

/**
 * ✅ DTO để chứa kết quả aggregate theo tháng cho chart
 * Chỉ chứa thông tin cần thiết: tháng, tổng income, tổng expense
 * 
 * Room sẽ tự động map từ SQL query nếu constructor parameters match với column names
 */
public class MonthlyAggregate {
    public String month; // Format: "yyyy-MM" (ví dụ: "2025-01")
    public double totalIncome;
    public double totalExpense;
    
    // ✅ Constructor để Room map từ SQL query
    // ✅ Column names trong SQL phải match với parameter names
    public MonthlyAggregate(String month, double totalIncome, double totalExpense) {
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }
}

