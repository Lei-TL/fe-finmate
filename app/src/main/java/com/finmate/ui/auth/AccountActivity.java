package com.finmate.ui.auth;

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
import com.finmate.data.dto.UserInfoResponse;
import com.finmate.data.remote.api.AuthService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AccountActivity extends BaseActivity {

    @Inject
    AuthService authService;

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
        
        // ✅ Load thông tin người dùng từ API
        loadUserInfo();
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
        String currentLanguage = prefs.getString("language", "vi"); // Default là tiếng Việt

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
                selectedTheme = ThemeHelper.THEME_LIGHT;
                break;
            case 1:
                selectedTheme = ThemeHelper.THEME_DARK;
                break;
            default:
                selectedTheme = ThemeHelper.THEME_SYSTEM;
                break;
        }
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putString("theme", selectedTheme).apply();
        ThemeHelper.applyTheme(selectedTheme);
    }

    private void loadThemeSelection() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentTheme = prefs.getString("theme", ThemeHelper.THEME_SYSTEM);
        switch (currentTheme) {
            case ThemeHelper.THEME_LIGHT:
                spnTheme.setSelection(0);
                break;
            case ThemeHelper.THEME_DARK:
                spnTheme.setSelection(1);
                break;
            default:
                spnTheme.setSelection(2);
                break;
        }
    }

    /**
     * ✅ Load thông tin người dùng từ API /auth/me và hiển thị vào các EditText
     */
    private void loadUserInfo() {
        authService.getCurrentUser().enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfoResponse userInfo = response.body();
                    
                    // ✅ Load thông tin vào các EditText
                    if (userInfo.getFullName() != null && !userInfo.getFullName().isEmpty()) {
                        edtName.setText(userInfo.getFullName());
                    }
                    
                    if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
                        edtEmail.setText(userInfo.getEmail());
                    }
                    
                    // ✅ Load birthday: Format từ yyyy-MM-dd sang dd/MM/yyyy hoặc hiển thị "--/--/--" nếu null
                    String birthdayDisplay = formatBirthdayForDisplay(userInfo.getBirthday());
                    edtBirthday.setText(birthdayDisplay);
                    
                    // ✅ Lưu fullName vào SharedPreferences để dùng ở HomeActivity
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    if (userInfo.getFullName() != null && !userInfo.getFullName().isEmpty()) {
                        prefs.edit().putString("full_name", userInfo.getFullName()).apply();
                    }
                    
                    // ✅ TODO: Load avatar nếu có avatarUrl
                    // if (userInfo.getAvatarUrl() != null && !userInfo.getAvatarUrl().isEmpty()) {
                    //     // Load avatar image vào imgAvatar
                    // }
                } else {
                    // ✅ Fallback: Load từ SharedPreferences nếu API fail
                    loadUserInfoFromSharedPreferences();
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                // ✅ Fallback: Load từ SharedPreferences nếu API fail
                loadUserInfoFromSharedPreferences();
            }
        });
    }

    /**
     * ✅ Fallback: Load thông tin từ SharedPreferences nếu API fail
     */
    private void loadUserInfoFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        
        // Load fullName
        String fullName = prefs.getString("full_name", "");
        if (fullName != null && !fullName.isEmpty()) {
            edtName.setText(fullName);
        }
        
        // Load email (nếu có lưu trong SharedPreferences)
        String email = prefs.getString("user_email", "");
        if (email != null && !email.isEmpty()) {
            edtEmail.setText(email);
        }
        
        // Load birthday (nếu có lưu trong SharedPreferences)
        String birthday = prefs.getString("user_birthday", "");
        if (birthday != null && !birthday.isEmpty()) {
            String birthdayDisplay = formatBirthdayForDisplay(birthday);
            edtBirthday.setText(birthdayDisplay);
        } else {
            // Nếu không có birthday, hiển thị "--/--/--"
            edtBirthday.setText("--/--/--");
        }
    }

    /**
     * ✅ Format birthday từ yyyy-MM-dd sang dd/MM/yyyy để hiển thị
     * Nếu birthday là null hoặc rỗng, trả về "--/--/--"
     */
    private String formatBirthdayForDisplay(String birthday) {
        if (birthday == null || birthday.isEmpty()) {
            return "--/--/--";
        }
        
        try {
            // Parse từ yyyy-MM-dd
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(birthday);
            
            // Format sang dd/MM/yyyy
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Nếu parse fail, trả về "--/--/--"
            return "--/--/--";
        }
    }
}
