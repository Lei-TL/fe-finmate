package com.finmate.core.ui;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ✅ Utility class để quản lý và share time filter state giữa các activities
 */
public class TimeFilterManager {
    
    private static final String PREF_NAME = "time_filter_prefs";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";
    private static final String KEY_FILTER_TEXT = "filter_text";
    
    /**
     * ✅ Lưu time filter state
     */
    public static void saveTimeFilter(Context context, Long startDate, Long endDate, String filterText) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        if (startDate != null) {
            editor.putLong(KEY_START_DATE, startDate);
        } else {
            editor.remove(KEY_START_DATE);
        }
        
        if (endDate != null) {
            editor.putLong(KEY_END_DATE, endDate);
        } else {
            editor.remove(KEY_END_DATE);
        }
        
        if (filterText != null) {
            editor.putString(KEY_FILTER_TEXT, filterText);
        } else {
            editor.remove(KEY_FILTER_TEXT);
        }
        
        editor.apply();
    }
    
    /**
     * ✅ Lấy time filter state
     */
    public static TimeFilterState getTimeFilter(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        Long startDate = null;
        Long endDate = null;
        String filterText = null;
        
        if (prefs.contains(KEY_START_DATE)) {
            startDate = prefs.getLong(KEY_START_DATE, 0);
        }
        
        if (prefs.contains(KEY_END_DATE)) {
            endDate = prefs.getLong(KEY_END_DATE, 0);
        }
        
        if (prefs.contains(KEY_FILTER_TEXT)) {
            filterText = prefs.getString(KEY_FILTER_TEXT, null);
        }
        
        return new TimeFilterState(startDate, endDate, filterText);
    }
    
    /**
     * ✅ Clear time filter state
     */
    public static void clearTimeFilter(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    
    /**
     * ✅ Data class để chứa time filter state
     */
    public static class TimeFilterState {
        public final Long startDate;
        public final Long endDate;
        public final String filterText;
        
        public TimeFilterState(Long startDate, Long endDate, String filterText) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.filterText = filterText;
        }
    }
}



