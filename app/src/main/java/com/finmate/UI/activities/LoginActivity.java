package com.finmate.UI.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.finmate.R;
import com.finmate.adapters.ThemeHelper;
import com.finmate.models.AuthResponse;
import com.finmate.models.LoginRequest;
import com.finmate.network.ApiService;
import com.finmate.util.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginActivity extends BaseActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvForgot;
    private ProgressBar progressBar;
    private ImageView ivLanguageSetting, ivThemeSetting;

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (tokenManager != null && tokenManager.isLoggedIn()) {
            navigateToHome();
            return;
        }
        setContentView(R.layout.activity_login);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgot = findViewById(R.id.tvForgot);
        progressBar = findViewById(R.id.progressBar);
        ivLanguageSetting = findViewById(R.id.iv_language_setting);
        ivThemeSetting = findViewById(R.id.iv_theme_setting);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(user, pass);
        });

        // Chuyển sang màn hình Đăng ký (SignUpActivity)
        tvSignup.setOnClickListener(v -> {
            // Đảm bảo bạn đã tạo SignUpActivity
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Quên mật khẩu
        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );

        ivLanguageSetting.setOnClickListener(v -> showLanguageDialog());
        ivThemeSetting.setOnClickListener(v -> showThemeDialog());
    }

    private void performLogin(String username, String password) {
        setLoading(true);

        LoginRequest request = new LoginRequest(username, password);

        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();

                    String accessToken = auth.getAccessToken();
                    String refreshToken = auth.getRefreshToken();

                    if (accessToken != null) {
                        tokenManager.saveToken(accessToken, refreshToken);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi: Token không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Đăng nhập thất bại!";
                    if (response.code() == 401) {
                        errorMsg = "Sai tên đăng nhập hoặc mật khẩu.";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void navigateToHome() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showLanguageDialog() {
        final String[] langCodes = {"en", "vi"};
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentLangCode = prefs.getString("language", "en");
        int checkedItem = currentLangCode.equals("vi") ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_language)
                .setSingleChoiceItems(R.array.language_options, checkedItem, (dialog, which) -> {
                    String selectedLanguage = langCodes[which];
                    prefs.edit().putString("language", selectedLanguage).apply();
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showThemeDialog() {
        final String[] themeCodes = {ThemeHelper.LIGHT_MODE, ThemeHelper.DARK_MODE, ThemeHelper.SYSTEM_DEFAULT};
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentTheme = prefs.getString("theme", ThemeHelper.SYSTEM_DEFAULT);
        int checkedItem = 0;
        if (currentTheme.equals(ThemeHelper.DARK_MODE)) {
            checkedItem = 1;
        } else if (currentTheme.equals(ThemeHelper.SYSTEM_DEFAULT)) {
            checkedItem = 2;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_theme)
                .setSingleChoiceItems(R.array.theme_options, checkedItem, (dialog, which) -> {
                    String selectedTheme = themeCodes[which];
                    prefs.edit().putString("theme", selectedTheme).apply();
                    ThemeHelper.applyTheme(selectedTheme);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
