package com.finmate.UI.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.adapters.LanguageAdapter;
import com.finmate.models.Language;

import java.util.ArrayList;
import java.util.List;

public class LanguageSettingActivity extends BaseActivity implements LanguageAdapter.OnItemClickListener {

    private RecyclerView rvLanguages;
    private LanguageAdapter adapter;
    private List<Language> languageList;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        rvLanguages = findViewById(R.id.rvLanguages);
        rvLanguages.setLayoutManager(new LinearLayoutManager(this));

        // Prepare data and adapter
        prepareLanguageList();
        adapter = new LanguageAdapter(this, languageList, this);
        rvLanguages.setAdapter(adapter);
    }

    private void prepareLanguageList() {
        languageList = new ArrayList<>();
        languageList.add(new Language("English", "en", R.drawable.ic_flag_uk)); // Assume you have these drawables
        languageList.add(new Language("Tiếng Việt", "vi", R.drawable.ic_flag_vn));

        String currentLangCode = prefs.getString("language", "en");

        for (Language lang : languageList) {
            if (lang.getCode().equals(currentLangCode)) {
                lang.setSelected(true);
                break;
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        // Get selected language
        Language selectedLanguage = languageList.get(position);

        // Save the new language preference
        prefs.edit().putString("language", selectedLanguage.getCode()).apply();

        // Update the selection state in the list
        for (int i = 0; i < languageList.size(); i++) {
            languageList.get(i).setSelected(i == position);
        }

        // Notify the adapter that the data has changed to update the checkmark
        adapter.notifyDataSetChanged();

        // Restart the current activity to apply the new language
        recreate();

        // Optionally, restart the main activity to ensure the whole app is updated
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
