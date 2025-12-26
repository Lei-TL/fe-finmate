package com.finmate.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.finmate.R;
import com.finmate.core.ui.LocaleHelper;
import com.finmate.ui.home.HomeActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    EditText etName, etEmail, etPassword, etConfirmPassword;
    Button btnSignup;
    TextView tvAlreadyAccount;
    
    private SignUpViewModel signUpViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // Ánh xạ thành phần giao diện
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);
        
        observeViewModel();

        // Xử lý nút SIGNUP
        btnSignup.setOnClickListener(view -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            // VALIDATE
            if (!validateInputs(name, email, pass, confirmPass)) {
                return;
            }
            
            String fullName = etName.getText().toString().trim();
            String defaultAvatarUrl = "ic_avatar"; // Default avatar resource name
            signUpViewModel.register(email, pass, fullName, defaultAvatarUrl);
        });

        // Quay lại Login
        tvAlreadyAccount.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });
    }
    
    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;
        
        // Validate name
        if (name.isEmpty()) {
            etName.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            etName.setError(null);
        }
        
        // Validate email
        if (email.isEmpty()) {
            etEmail.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.invalid_email));
            isValid = false;
        } else {
            etEmail.setError(null);
        }
        
        // Validate password với các yêu cầu mạnh hơn
        if (password.isEmpty()) {
            etPassword.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            // Kiểm tra độ dài tối thiểu 8 ký tự
            if (password.length() < 8) {
                etPassword.setError(getString(R.string.password_too_short));
                isValid = false;
            }
            // Kiểm tra có chữ hoa
            else if (!password.matches(".*[A-Z].*")) {
                etPassword.setError(getString(R.string.password_no_uppercase));
                isValid = false;
            }
            // Kiểm tra có chữ thường
            else if (!password.matches(".*[a-z].*")) {
                etPassword.setError(getString(R.string.password_no_lowercase));
                isValid = false;
            }
            // Kiểm tra có số
            else if (!password.matches(".*[0-9].*")) {
                etPassword.setError(getString(R.string.password_no_digit));
                isValid = false;
            }
            // Kiểm tra có ký tự đặc biệt
            else if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                etPassword.setError(getString(R.string.password_no_special_char));
                isValid = false;
            } else {
                etPassword.setError(null);
            }
        }
        
        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
            isValid = false;
        } else {
            etConfirmPassword.setError(null);
        }
        
        if (!isValid) {
            Toast.makeText(this, R.string.fill_all_info, Toast.LENGTH_SHORT).show();
        }
        
        return isValid;
    }
    
    private void observeViewModel() {
        signUpViewModel.isLoading.observe(this, isLoading -> {
            btnSignup.setEnabled(!isLoading);
            btnSignup.setText(isLoading ? getString(R.string.syncing) : getString(R.string.signup_button));
            // Show progress indicator
            if (isLoading) {
                // TODO: Show progress bar/dialog if needed
            }
        });
        
        signUpViewModel.registerSuccess.observe(this, success -> {
            if (success) {
                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
        
        signUpViewModel.errorMessage.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
