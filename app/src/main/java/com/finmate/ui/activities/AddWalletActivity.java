package com.finmate.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.finmate.core.ui.LocaleHelper;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.repository.WalletRepository;

public class AddWalletActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

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

        // Tạo UUID tạm thời cho wallet mới (sẽ được thay thế bằng ID từ backend khi sync)
        String tempId = java.util.UUID.randomUUID().toString();
        
        // ✅ Parse balance từ string sang double
        double balanceValue = 0.0;
        try {
            String balanceStr = balance.replaceAll("[^0-9.]", "");
            balanceValue = Double.parseDouble(balanceStr);
        } catch (Exception e) {
            balanceValue = 0.0;
        }
        
        // ✅ Tạo WalletEntity với currentBalance và initialBalance bằng nhau (ví mới)
        WalletEntity wallet = new WalletEntity(
                tempId, 
                name, 
                balance, // Formatted string
                balanceValue, // currentBalance
                balanceValue, // initialBalance (bằng currentBalance cho ví mới)
                R.drawable.ic_wallet
        );

        repo.insert(wallet);

        Toast.makeText(this, "Đã thêm ví!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
