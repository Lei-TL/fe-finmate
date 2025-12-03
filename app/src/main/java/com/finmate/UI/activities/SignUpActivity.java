package com.finmate.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.finmate.R;
import com.finmate.models.AuthResponse;
import com.finmate.models.RegisterRequest;
import com.finmate.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class SignUpActivity extends BaseActivity {

    EditText etName, etEmail, etPassword;
    Button btnSignup;
    TextView tvAlreadyAccount;

    @Inject
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Ánh xạ thành phần giao diện
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvAlreadyAccount = findViewById(R.id.tvAlreadyAccount);

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

            register(name, email, pass);
        });

        // Quay lại Login
        tvAlreadyAccount.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });
    }

    private void register(String username, String email, String password) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        
        btnSignup.setEnabled(false);

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                btnSignup.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển về Login
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Đăng ký thất bại! Có thể username đã tồn tại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                btnSignup.setEnabled(true);
                Toast.makeText(SignUpActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
