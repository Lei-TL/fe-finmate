package com.finmate.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.finmate.core.network.ApiCallback;
import com.finmate.core.network.NetworkChecker;
import com.finmate.core.ui.LocaleHelper;
import com.finmate.data.dto.CreateWalletRequest;
import com.finmate.data.dto.WalletResponse;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.repository.WalletRepository;
import com.finmate.data.repository.WalletRemoteRepository;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddWalletActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    private EditText edtWalletName, edtWalletBalance;
    private Button btnSave, btnCancel;
    private ImageView btnBack;

    private WalletRepository repo;
    
    @Inject
    WalletRemoteRepository walletRemoteRepository;
    
    @Inject
    NetworkChecker networkChecker;

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

        // ✅ Lưu vào local DB trước
        repo.insert(wallet);
        android.util.Log.d("AddWalletActivity", "Wallet saved locally with tempId: " + tempId);

        // ✅ Sync lên backend nếu có mạng
        if (networkChecker.isNetworkAvailable() && walletRemoteRepository != null) {
            android.util.Log.d("AddWalletActivity", "Syncing wallet to backend...");
            syncWalletToBackend(wallet, balanceValue);
        } else {
            android.util.Log.d("AddWalletActivity", "No network or WalletRemoteRepository is null, wallet saved locally only");
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã thêm ví! (Chưa sync)", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
    
    private void syncWalletToBackend(WalletEntity wallet, double balanceValue) {
        // ✅ Tạo request để gửi lên backend
        CreateWalletRequest request = new CreateWalletRequest(
                wallet.name,
                "CASH", // Default type, có thể thêm UI để chọn sau
                "VND", // Default currency, có thể thêm UI để chọn sau
                BigDecimal.valueOf(balanceValue),
                false, // archived
                null // color
        );
        
        android.util.Log.d("AddWalletActivity", "Creating CreateWalletRequest: name=" + request.getName() + ", initialBalance=" + request.getInitialBalance());
        
        walletRemoteRepository.createWallet(request, new ApiCallback<WalletResponse>() {
            @Override
            public void onSuccess(WalletResponse response) {
                android.util.Log.d("AddWalletActivity", "Wallet synced to backend successfully. Response ID: " + (response != null ? response.getId() : "null"));
                
                if (response != null && response.getId() != null) {
                    // ✅ Cập nhật wallet local với ID từ backend
                    // Tìm wallet cũ theo tempId và update với ID mới
                    repo.getAll(new WalletRepository.Callback() {
                        @Override
                        public void onResult(List<WalletEntity> wallets) {
                            WalletEntity oldWallet = null;
                            for (WalletEntity w : wallets) {
                                if (w.id != null && w.id.equals(wallet.id)) {
                                    oldWallet = w;
                                    break;
                                }
                            }
                            
                            if (oldWallet != null) {
                                // ✅ Tạo wallet mới với ID từ backend
                                WalletEntity updatedWallet = new WalletEntity(
                                        response.getId(), // ✅ Dùng ID từ backend
                                        response.getName(),
                                        String.format("%,.0f %s", response.getCurrentBalance(), response.getCurrency() != null ? response.getCurrency() : ""),
                                        response.getCurrentBalance(),
                                        response.getInitialBalance(),
                                        wallet.iconRes
                                );
                                
                                // ✅ Xóa wallet cũ và insert wallet mới với ID từ backend
                                AppDatabase db = AppDatabase.getDatabase(AddWalletActivity.this);
                                db.walletDao().delete(oldWallet);
                                db.walletDao().insert(updatedWallet);
                                
                                android.util.Log.d("AddWalletActivity", "Wallet updated locally with backend ID: " + response.getId());
                            }
                        }
                    });
                }
                
                runOnUiThread(() -> {
                    Toast.makeText(AddWalletActivity.this, "Đã thêm ví!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            
            @Override
            public void onError(String message) {
                android.util.Log.e("AddWalletActivity", "Error syncing wallet to backend: " + message, new Exception(message));
                runOnUiThread(() -> {
                    Toast.makeText(AddWalletActivity.this, "Đã thêm ví! (Chưa sync: " + message + ")", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
}
