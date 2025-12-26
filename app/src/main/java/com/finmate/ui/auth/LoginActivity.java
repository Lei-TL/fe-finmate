package com.finmate.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.finmate.R;
import com.finmate.core.ui.LocaleHelper;
import com.finmate.core.ui.ThemeHelper;
import com.finmate.ui.settings.LanguageSettingActivity;
import com.finmate.ui.dialogs.ThemeDialog;
import com.finmate.ui.home.HomeActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvSignup, tvForgot;
    ImageView ivLanguageSetting, ivThemeSetting;
    
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Ánh xạ view
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvForgot = findViewById(R.id.tvForgot);
        ivLanguageSetting = findViewById(R.id.iv_language_setting);
        ivThemeSetting = findViewById(R.id.iv_theme_setting);
        
        observeViewModel();

        // Xử lý đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            
            // Validation
            if (!validateInputs(email, pass)) {
                return;
            }
            
            loginViewModel.login(email, pass);
        });

        // Chuyển sang SignUp
        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Forgot password
        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
        );

        ivLanguageSetting.setOnClickListener(v -> {
            startActivity(new Intent(this, LanguageSettingActivity.class));
        });

        ivThemeSetting.setOnClickListener(v -> {
            ThemeDialog dialog = new ThemeDialog();
            dialog.show(getSupportFragmentManager(), "theme_dialog");
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;
        
        // Validate email
        if (email.isEmpty()) {
            etUsername.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUsername.setError(getString(R.string.invalid_email));
            isValid = false;
        } else {
            etUsername.setError(null);
        }
        
        // Validate password
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.required_field));
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_too_short));
            isValid = false;
        } else {
            etPassword.setError(null);
        }
        
        return isValid;
    }
    
    private void observeViewModel() {
        loginViewModel.isLoading.observe(this, isLoading -> {
            btnLogin.setEnabled(!isLoading);
            btnLogin.setText(isLoading ? getString(R.string.syncing) : getString(R.string.login));
            // Show progress indicator
            if (isLoading) {
                // TODO: Show progress bar/dialog if needed
            }
        });
        
        loginViewModel.loginSuccess.observe(this, success -> {
            if (success) {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
        
        loginViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
