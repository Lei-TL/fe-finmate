package com.finmate.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.finmate.ui.base.BaseActivity;
import com.finmate.R;
import com.finmate.core.ui.ThemeHelper;

public class AccountActivity extends BaseActivity {

    private ImageView btnBack, btnCamera, btnEdit;
    private EditText edtName, edtEmail, edtBirthday, edtNote;
    private Spinner spnLanguage, spnTheme;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initViews();
        handleEvents();
        setupLanguageSpinner();
        loadLanguageSelection();
        setupThemeSpinner();
        loadThemeSelection();

        setEditingEnabled(false);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnCamera = findViewById(R.id.btnCamera);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtBirthday = findViewById(R.id.edtBirthday);
        edtNote = findViewById(R.id.edtNote);
        spnLanguage = findViewById(R.id.spnLanguage);
        spnTheme = findViewById(R.id.spnTheme);
        btnSave = findViewById(R.id.btnSave);
    }

    private void handleEvents() {

        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v ->
                Toast.makeText(this, R.string.toast_change_avatar, Toast.LENGTH_SHORT).show()
        );

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, R.string.toast_edit_mode, Toast.LENGTH_SHORT).show();
            setEditingEnabled(true);
        });

        btnSave.setOnClickListener(v -> {
            saveLanguageSelection();
            saveThemeSelection();
            setEditingEnabled(false);
            recreate();
            Toast.makeText(this, R.string.toast_changes_saved, Toast.LENGTH_SHORT).show();
        });
    }

    private void setEditingEnabled(boolean enabled) {
        edtName.setEnabled(enabled);
        edtEmail.setEnabled(enabled);
        edtBirthday.setEnabled(enabled);
        edtNote.setEnabled(enabled);
        spnLanguage.setEnabled(enabled);
        spnTheme.setEnabled(enabled);
        btnSave.setVisibility(enabled ? View.VISIBLE : View.GONE);

        btnCamera.setVisibility(enabled ? View.VISIBLE : View.GONE);

        int color = enabled ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.darker_gray);
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLanguage.setAdapter(adapter);
    }

    private void saveLanguageSelection() {
        int selectedPosition = spnLanguage.getSelectedItemPosition();
        String selectedLanguage = selectedPosition == 0 ? "en" : "vi";
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putString("language", selectedLanguage).apply();
    }

    private void loadLanguageSelection() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentLanguage = prefs.getString("language", "en");

        if (currentLanguage.equals("vi")) {
            spnLanguage.setSelection(1);
        } else {
            spnLanguage.setSelection(0);
        }
    }


    private void setupThemeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.theme_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTheme.setAdapter(adapter);
    }

    private void saveThemeSelection() {
        int selectedPosition = spnTheme.getSelectedItemPosition();
        String selectedTheme;
        switch (selectedPosition) {
            case 0:
                selectedTheme = ThemeHelper.LIGHT_MODE;
                break;
            case 1:
                selectedTheme = ThemeHelper.DARK_MODE;
                break;
            default:
                selectedTheme = ThemeHelper.SYSTEM_DEFAULT;
                break;
        }
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putString("theme", selectedTheme).apply();
        ThemeHelper.applyTheme(selectedTheme);
    }

    private void loadThemeSelection() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentTheme = prefs.getString("theme", ThemeHelper.SYSTEM_DEFAULT);
        switch (currentTheme) {
            case ThemeHelper.LIGHT_MODE:
                spnTheme.setSelection(0);
                break;
            case ThemeHelper.DARK_MODE:
                spnTheme.setSelection(1);
                break;
            default:
                spnTheme.setSelection(2);
                break;
        }
    }
}
