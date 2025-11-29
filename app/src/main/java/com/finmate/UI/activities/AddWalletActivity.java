package com.finmate.UI.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.finmate.entities.WalletEntity;
import com.finmate.repository.WalletRepository;

public class AddWalletActivity extends AppCompatActivity {

    private EditText edtWalletName, edtWalletBalance;
    private Button btnSave, btnCancel;
    private ImageView btnBack;

    private WalletRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        repo = new WalletRepository(this);

        mapViews();
        handleEvents();
    }

    private void mapViews() {
        edtWalletName = findViewById(R.id.edtWalletName);
        edtWalletBalance = findViewById(R.id.edtWalletBalance);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
    }

    private void handleEvents() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveWallet());
    }

    private void saveWallet() {
        String name = edtWalletName.getText().toString().trim();
        String balance = edtWalletBalance.getText().toString().trim();

        if (name.isEmpty()) {
            edtWalletName.setError("Nhập tên ví");
            return;
        }

        if (balance.isEmpty()) {
            edtWalletBalance.setError("Nhập số dư");
            return;
        }

        WalletEntity wallet = new WalletEntity(name, balance, R.drawable.ic_wallet);

        repo.insert(wallet);

        Toast.makeText(this, "Đã thêm ví!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
