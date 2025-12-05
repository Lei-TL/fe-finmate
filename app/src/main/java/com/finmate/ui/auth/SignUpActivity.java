package com.finmate.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.finmate.R;
import com.finmate.ui.activities.HomeActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignUpActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
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
        btnSignup = findViewById(R.id.btnSignup);
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);
        
        observeViewModel();

        // Xử lý nút SIGNUP
        btnSignup.setOnClickListener(view -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // VALIDATE
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải ≥ 6 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            signUpViewModel.register(email, pass);
        });

        // Quay lại Login
        tvAlreadyAccount.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });
    }
    
    private void observeViewModel() {
        signUpViewModel.isLoading.observe(this, isLoading -> {
            btnSignup.setEnabled(!isLoading);
            btnSignup.setText(isLoading ? "Đang xử lý..." : "Signup");
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
