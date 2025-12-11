package com.finmate.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class để format TransactionEntity cho UI.
 * Tách biệt domain model (raw data) và UI presentation.
 */
public class TransactionFormatter {

    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    /**
     * Format amount từ double sang String hiển thị (có dấu phẩy ngăn cách hàng nghìn)
     * Ví dụ: 1000.0 → "1,000"
     */
    public static String formatAmount(double amount) {
        return String.format(Locale.getDefault(), "%,.0f", amount);
    }

    /**
     * Format occurredAt (ISO datetime string) sang format hiển thị cho UI
     * Ví dụ: "2024-01-15T10:30:00" → "15/01/2024"
     */
    public static String formatDate(String occurredAt) {
        if (occurredAt == null || occurredAt.isEmpty()) {
            return "";
        }
        try {
            Date date = ISO_FORMAT.parse(occurredAt);
            if (date != null) {
                return DISPLAY_DATE_FORMAT.format(date);
            }
        } catch (ParseException e) {
            // Nếu không parse được ISO format, trả về nguyên bản
            // (có thể là format khác từ BE hoặc đã format sẵn)
            return occurredAt;
        }
        return occurredAt;
    }

    /**
     * Parse amount từ String input của user (có thể có dấu phẩy, khoảng trắng)
     * Ví dụ: "1,000" → 1000.0, "1 000" → 1000.0
     */
    public static double parseAmount(String amountInput) throws NumberFormatException {
        if (amountInput == null || amountInput.trim().isEmpty()) {
            throw new NumberFormatException("Amount cannot be empty");
        }
        // Loại bỏ dấu phẩy, khoảng trắng, và các ký tự không phải số
        String cleaned = amountInput.replace(",", "").replace(" ", "").trim();
        return Double.parseDouble(cleaned);
    }

    /**
     * Convert date từ format hiển thị (dd/MM/yyyy) sang ISO format
     * Ví dụ: "15/01/2024" → "2024-01-15T00:00:00"
     */
    public static String parseDateToISO(String displayDate) throws ParseException {
        if (displayDate == null || displayDate.trim().isEmpty()) {
            throw new ParseException("Date cannot be empty", 0);
        }
        Date date = DISPLAY_DATE_FORMAT.parse(displayDate);
        if (date != null) {
            return ISO_FORMAT.format(date);
        }
        throw new ParseException("Invalid date format: " + displayDate, 0);
    }

    /**
     * Convert Calendar date sang ISO format string
     * Helper method cho AddTransactionActivity
     */
    public static String calendarToISO(int year, int month, int day) {
        try {
            // month is 0-based in Calendar, but we pass 1-based
            String dateStr = String.format(Locale.US, "%04d-%02d-%02dT00:00:00", year, month, day);
            return dateStr;
        } catch (Exception e) {
            return "";
        }
    }
}

