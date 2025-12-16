package com.finmate.ui.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.finmate.core.ui.LocaleHelper;
import com.finmate.core.ui.ThemeHelper;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Áp dụng theme TRƯỚC super.onCreate()
        ThemeHelper.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
    }
}
