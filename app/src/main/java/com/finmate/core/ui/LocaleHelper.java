package com.finmate.core.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_LANGUAGE = "language";

    /**
     * Được BaseActivity gọi tự động trong attachBaseContext
     * Đọc language từ SharedPreferences (synchronous)
     * Default là "vi" (tiếng Việt)
     */
    public static Context applyLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANGUAGE, "vi"); // mặc định tiếng Việt
        return setLocale(context, lang);
    }

    /**
     * Áp dụng locale cho context
     */
    public static Context setLocale(Context context, String lang) {
        return updateResources(context, lang);
    }

    /**
     * Lưu language vào SharedPreferences (để LocaleHelper có thể đọc synchronous)
     * UserPreferencesLocalDataSource sẽ gọi method này khi save language
     */
    public static void saveLanguageToPrefs(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    /**
     * Lấy language hiện tại từ SharedPreferences
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "vi"); // default là tiếng Việt
    }

    private static Context updateResources(Context context, String lang) {
        Locale locale;
        
        // Nếu là "system", dùng locale của hệ thống
        if ("system".equals(lang) || lang == null || lang.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            // Nếu hệ thống không có locale, default về "vi"
            if (locale == null) {
                locale = new Locale("vi");
            }
        } else {
            locale = new Locale(lang);
        }
        
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0+
            return context.createConfigurationContext(config);
        } else {
            // Android cũ hơn
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }
}
