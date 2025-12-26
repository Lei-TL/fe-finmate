package com.finmate.ui.statistics;

import com.finmate.data.local.database.entity.TransactionEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for statistics calculations and data grouping.
 * Shared logic between StatisticActivity and IncomeStatisticActivity.
 */
public class StatisticsUtils {

    private static final int MAX_MONTHS_DISPLAY = 6; // Limit chart to last 6 months
    
    /**
     * Parse date string with multiple format support (ISO datetime or date-only)
     */
    public static java.util.Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        // Try ISO format first: "yyyy-MM-dd'T'HH:mm:ss"
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            return isoFormat.parse(dateStr);
        } catch (Exception e) {
            // Try date-only format: "yyyy-MM-dd"
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                return dateFormat.parse(dateStr);
            } catch (Exception e2) {
                return null;
            }
        }
    }
    
    /**
     * Group transactions by hour (for Today filter)
     * Returns a map: hourKey (0-23) -> total amount
     */
    public static Map<Integer, Double> groupByHour(List<TransactionEntity> transactions) {
        Map<Integer, Double> hourlyData = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        
        // Initialize all 24 hours with 0
        for (int i = 0; i < 24; i++) {
            hourlyData.put(i, 0.0);
        }
        
        for (TransactionEntity t : transactions) {
            if (t.date != null && !t.date.isEmpty()) {
                java.util.Date date = parseDate(t.date);
                if (date != null) {
                    cal.setTime(date);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    
                    // ✅ Parse amount từ String
                    double amount = 0.0;
                    if (t.amount != null && !t.amount.isEmpty()) {
                        try {
                            String cleanAmount = t.amount.replaceAll("[^0-9.]", "");
                            if (!cleanAmount.isEmpty()) {
                                amount = Double.parseDouble(cleanAmount);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    hourlyData.put(hour, hourlyData.getOrDefault(hour, 0.0) + amount);
                }
            }
        }
        
        return hourlyData;
    }
    
    /**
     * Group transactions by day of week (for 7 days ago filter)
     * Returns a map: dayKey (day name) -> total amount
     * Keys are ordered from 7 days ago to today
     */
    public static Map<String, Double> groupByDayOfWeek(List<TransactionEntity> transactions) {
        Map<String, Double> dailyData = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        
        // Initialize all 7 days with 0 (from 7 days ago to today)
        Calendar tempCal = Calendar.getInstance();
        List<String> dayKeys = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            tempCal.setTime(new java.util.Date());
            tempCal.add(Calendar.DAY_OF_YEAR, -i);
            String dayName = dayFormat.format(tempCal.getTime());
            String dateStr = dateFormat.format(tempCal.getTime());
            String dayKey = dayName + " (" + dateStr + ")";
            dayKeys.add(dayKey);
            dailyData.put(dayKey, 0.0);
        }
        
        for (TransactionEntity t : transactions) {
            if (t.date != null && !t.date.isEmpty()) {
                java.util.Date date = parseDate(t.date);
                if (date != null) {
                    cal.setTime(date);
                    String dayName = dayFormat.format(cal.getTime());
                    String dateStr = dateFormat.format(cal.getTime());
                    String dayKey = dayName + " (" + dateStr + ")";
                    if (dailyData.containsKey(dayKey)) {
                        // ✅ Parse amount từ String
                        double amount = 0.0;
                        if (t.amount != null && !t.amount.isEmpty()) {
                            try {
                                String cleanAmount = t.amount.replaceAll("[^0-9.]", "");
                                if (!cleanAmount.isEmpty()) {
                                    amount = Double.parseDouble(cleanAmount);
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                        dailyData.put(dayKey, dailyData.get(dayKey) + amount);
                    }
                }
            }
        }
        
        return dailyData;
    }
    
    /**
     * Group transactions by week in month (for 1 month ago filter)
     * Returns a map: weekKey (Week 1, Week 2, ...) -> total amount
     * Groups transactions from last 30 days into weeks
     */
    public static Map<String, Double> groupByWeekInMonth(List<TransactionEntity> transactions) {
        Map<String, Double> weeklyData = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat weekFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        
        // Group by week (7 days each) from last 30 days
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        // Initialize 4-5 weeks (last 30 days)
        for (int week = 0; week < 5; week++) {
            Calendar weekStart = (Calendar) today.clone();
            weekStart.add(Calendar.DAY_OF_YEAR, -30 + (week * 7));
            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_YEAR, 6);
            
            String weekKey = weekFormat.format(weekStart.getTime()) + " - " + weekFormat.format(weekEnd.getTime());
            weeklyData.put(weekKey, 0.0);
        }
        
        for (TransactionEntity t : transactions) {
            if (t.date != null && !t.date.isEmpty()) {
                java.util.Date date = parseDate(t.date);
                if (date != null) {
                    cal.setTime(date);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    
                    long transactionTime = cal.getTimeInMillis();
                    long daysDiff = (today.getTimeInMillis() - transactionTime) / (24 * 60 * 60 * 1000);
                    
                    // ✅ Parse amount từ String
                    double amount = 0.0;
                    if (t.amount != null && !t.amount.isEmpty()) {
                        try {
                            String cleanAmount = t.amount.replaceAll("[^0-9.]", "");
                            if (!cleanAmount.isEmpty()) {
                                amount = Double.parseDouble(cleanAmount);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    if (daysDiff >= 0 && daysDiff < 30) {
                        int weekIndex = (int) (daysDiff / 7);
                        if (weekIndex >= 0 && weekIndex < 5) {
                            List<String> weekKeys = new ArrayList<>(weeklyData.keySet());
                            if (weekIndex < weekKeys.size()) {
                                String weekKey = weekKeys.get(weekIndex);
                                weeklyData.put(weekKey, weeklyData.get(weekKey) + amount);
                            }
                        }
                    }
                }
            }
        }
        
        return weeklyData;
    }
    
    /**
     * Group transactions by month and calculate totals
     * Returns a map: monthKey (yyyy-MM) -> total amount
     */
    public static Map<String, Double> groupByMonth(List<TransactionEntity> transactions) {
        Map<String, Double> monthlyData = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        
        for (TransactionEntity t : transactions) {
            if (t.date != null && !t.date.isEmpty()) {
                java.util.Date date = parseDate(t.date);
                if (date != null) {
                    cal.setTime(date);
                    String monthKey = monthFormat.format(cal.getTime());
                    
                    // ✅ Parse amount từ String
                    double amount = 0.0;
                    if (t.amount != null && !t.amount.isEmpty()) {
                        try {
                            String cleanAmount = t.amount.replaceAll("[^0-9.]", "");
                            if (!cleanAmount.isEmpty()) {
                                amount = Double.parseDouble(cleanAmount);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    monthlyData.put(monthKey, monthlyData.getOrDefault(monthKey, 0.0) + amount);
                }
            }
        }
        
        return monthlyData;
    }
    
    /**
     * Group transactions by year (for All time filter)
     * Returns a map: yearKey (yyyy) -> total amount
     * Only includes years that have transactions
     */
    public static Map<String, Double> groupByYear(List<TransactionEntity> transactions) {
        Map<String, Double> yearlyData = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        
        for (TransactionEntity t : transactions) {
            if (t.date != null && !t.date.isEmpty()) {
                java.util.Date date = parseDate(t.date);
                if (date != null) {
                    cal.setTime(date);
                    String yearKey = yearFormat.format(cal.getTime());
                    
                    // ✅ Parse amount từ String
                    double amount = 0.0;
                    if (t.amount != null && !t.amount.isEmpty()) {
                        try {
                            String cleanAmount = t.amount.replaceAll("[^0-9.]", "");
                            if (!cleanAmount.isEmpty()) {
                                amount = Double.parseDouble(cleanAmount);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    
                    yearlyData.put(yearKey, yearlyData.getOrDefault(yearKey, 0.0) + amount);
                }
            }
        }
        
        return yearlyData;
    }
    
    /**
     * Get sorted month keys, limited to last N months
     */
    public static List<String> getSortedMonthsLimited(Map<String, Double> monthlyData, int maxMonths) {
        List<String> sortedMonths = new ArrayList<>(monthlyData.keySet());
        Collections.sort(sortedMonths);
        
        // Limit to last N months
        if (sortedMonths.size() > maxMonths) {
            sortedMonths = sortedMonths.subList(sortedMonths.size() - maxMonths, sortedMonths.size());
        }
        
        return sortedMonths;
    }
    
    /**
     * Group transactions by category and calculate totals
     * Returns a map: categoryName -> total amount
     */
    public static Map<String, Double> groupByCategory(List<TransactionEntity> transactions) {
        Map<String, Double> categoryData = new HashMap<>();
        for (TransactionEntity t : transactions) {
            String category = (t.category != null && !t.category.isEmpty()) ? t.category : "Khác";
            double amount = 0.0;
            if (t.amount != null && !t.amount.isEmpty()) {
                try {
                    String cleanAmount = t.amount.replaceAll("[^0-9.]", "");
                    if (!cleanAmount.isEmpty()) {
                        amount = Double.parseDouble(cleanAmount);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            categoryData.put(category, categoryData.getOrDefault(category, 0.0) + amount);
        }
        return categoryData;
    }
    
    /**
     * Calculate total amount from transactions
     */
    public static double calculateTotal(List<TransactionEntity> transactions) {
        double total = 0.0;
        for (TransactionEntity t : transactions) {
            if (t.amount != null && !t.amount.isEmpty()) {
                try {
                    String cleanAmount = t.amount.replaceAll("[^0-9.]", "");
                    if (!cleanAmount.isEmpty()) {
                        total += Double.parseDouble(cleanAmount);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return total;
    }
    
    /**
     * Format amount with K/M/B suffixes
     */
    public static String formatAmount(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format(Locale.getDefault(), "%,.1fB", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return String.format(Locale.getDefault(), "%,.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format(Locale.getDefault(), "%,.1fK", amount / 1_000);
        } else {
            return String.format(Locale.getDefault(), "%,.0f", amount);
        }
    }
    
    /**
     * Format month label for display (MM/yyyy)
     */
    public static String formatMonthLabel(String monthKey) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(monthKey));
        } catch (Exception e) {
            return monthKey;
        }
    }
    
    /**
     * Format hour label for display (HH:00)
     */
    public static String formatHourLabel(int hour) {
        return String.format(Locale.getDefault(), "%02d:00", hour);
    }
    
    /**
     * Get default max months for chart display
     */
    public static int getMaxMonthsDisplay() {
        return MAX_MONTHS_DISPLAY;
    }
}

