package com.finmate.UI.activities;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import com.finmate.adapters.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, getLanguagePreference(newBase)));
    }

    public static String getLanguagePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("language", "en"); // Default to English
    }
}
