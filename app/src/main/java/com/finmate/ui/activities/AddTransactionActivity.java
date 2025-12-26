package com.finmate.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.finmate.ui.base.BaseActivity;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.finmate.core.network.ApiCallback;
import com.finmate.core.network.NetworkChecker;
import com.finmate.data.dto.CategoryResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.remote.dto.CreateTransactionRequest;
import com.finmate.data.repository.CategoryRepository;
import com.finmate.data.repository.CategoryRemoteRepository;
import com.finmate.data.repository.TransactionRemoteRepository;
import com.finmate.data.repository.TransactionRepository;
import com.finmate.data.repository.WalletRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddTransactionActivity extends BaseActivity {

    @Inject
    CategoryRepository categoryRepository;
    
    @Inject
    CategoryRemoteRepository categoryRemoteRepository;
    
    @Inject
    WalletRepository walletRepository;
    
    @Inject
    TransactionRepository transactionRepository;
    
    @Inject
    TransactionRemoteRepository transactionRemoteRepository;
    
    @Inject
    NetworkChecker networkChecker;

    EditText etGroup, etCategory, etTitle, etAmount, etWallet, etNote;
    TextView tvDate, tvAddTitle;
    Button btnCancel, btnSave;

    String selectedGroup = ""; // "INCOME" hoặc "EXPENSE"
    String selectedCategoryId = null;
    String selectedCategoryName = null;
    String selectedWalletId = null;
    String selectedWalletName = null;
    boolean isEditMode = false;
    String transactionId = null; // ID của transaction đang edit

    // Ngày
    String startDate = "";
    String endDate = "";

    // ✅ Track dialogs to prevent window leaks
    private BottomSheetDialog currentDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupEvents();
    }

    private void initViews() {
        etGroup    = findViewById(R.id.etGroup);
        etCategory = findViewById(R.id.etCategory);
        etTitle    = findViewById(R.id.etTitle);
        etAmount   = findViewById(R.id.etAmount);
        etWallet   = findViewById(R.id.etWallet);
        tvDate     = findViewById(R.id.tvDate);
        etNote     = findViewById(R.id.etNote);
        tvAddTitle = findViewById(R.id.tvAddTitle);

        btnCancel  = findViewById(R.id.btnCancel);
        btnSave    = findViewById(R.id.btnSave);

        // Check if edit mode
        transactionId = getIntent().getStringExtra("transaction_id");
        if (transactionId != null && !transactionId.isEmpty()) {
            isEditMode = true;
            if (tvAddTitle != null) {
                tvAddTitle.setText(R.string.edit_transaction);
            }
            // TODO: Load transaction data và populate fields
        } else {
            isEditMode = false;
            if (tvAddTitle != null) {
                tvAddTitle.setText(R.string.add_transaction_title);
            }
        }
    }

    // ✅ Xóa TextWatcher đổi màu cứng - dùng theme thay thế
    // EditText đã được config textColor và textColorHint trong XML

    private void setupEvents() {
        // Field 1: Nhóm giao dịch (INCOME/EXPENSE)
        etGroup.setOnClickListener(v -> showGroupBottomSheet());

        // Field 2: Danh mục (Ăn uống, Lương...)
        etCategory.setOnClickListener(v -> {
            if (selectedGroup.isEmpty()) {
                Toast.makeText(this, "Hãy chọn nhóm giao dịch trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            showCategoryItemsBottomSheet();
        });

        // Field 3: Tên giao dịch - cho gõ tự do, auto-fill theo category nhưng có thể chỉnh
        // Không cần click listener, user có thể gõ tự do

        etWallet.setOnClickListener(v -> showWalletBottomSheet());

        // Chọn ngày giao dịch
        tvDate.setOnClickListener(v -> showDueDateBottomSheet());

        btnCancel.setOnClickListener(v -> handleCancel());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Dismiss any open dialogs to prevent window leaks
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }

    // ================================
    // 1) BottomSheet: Chọn nhóm
    // ================================
    private void showGroupBottomSheet() {
        // ✅ Dismiss previous dialog if exists
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        currentDialog = dialog;
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_group, null);

        TextView tvIncome  = view.findViewById(R.id.tvIncomeGroup);
        TextView tvExpense = view.findViewById(R.id.tvExpenseGroup);

        tvIncome.setOnClickListener(v -> {
            selectedGroup = "INCOME";
            etGroup.setText(getString(R.string.income_label));
            etCategory.setText(""); // Clear category khi đổi nhóm
            // Auto-fill title nếu đang rỗng, nếu đã có text thì giữ nguyên
            if (etTitle.getText().toString().trim().isEmpty()) {
                etTitle.setText("");
            }
            selectedCategoryId = null;
            selectedCategoryName = null;
            dialog.dismiss();
        });

        tvExpense.setOnClickListener(v -> {
            selectedGroup = "EXPENSE";
            etGroup.setText(getString(R.string.expense_label));
            etCategory.setText(""); // Clear category khi đổi nhóm
            // Auto-fill title nếu đang rỗng, nếu đã có text thì giữ nguyên
            if (etTitle.getText().toString().trim().isEmpty()) {
                etTitle.setText("");
            }
            selectedCategoryId = null;
            selectedCategoryName = null;
            dialog.dismiss();
            currentDialog = null;
        });

        dialog.setContentView(view);
        dialog.show();
    }

    // ================================
    // 2) BottomSheet: Mục con - Load từ database
    // ================================
    private void showCategoryItemsBottomSheet() {
        if (selectedGroup.isEmpty() || (!selectedGroup.equals("INCOME") && !selectedGroup.equals("EXPENSE"))) {
            Toast.makeText(this, "Hãy chọn nhóm giao dịch trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Dismiss previous dialog if exists
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        currentDialog = dialog;
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_category_items, null);

        LinearLayout container = view.findViewById(R.id.layoutCategoryItems);
        container.removeAllViews();

        // ✅ Load categories từ database theo type
        // Sử dụng selectedGroup trực tiếp (uppercase) vì CategoryEntity có thể lưu cả uppercase và lowercase
        // Nếu không tìm thấy, thử lowercase
        String type = selectedGroup; // "INCOME" hoặc "EXPENSE"
        categoryRepository.getByType(type).observe(this, categories -> {
            if (categories == null || categories.isEmpty()) {
                // Thử lowercase nếu không tìm thấy
                String typeLower = selectedGroup.toLowerCase();
                categoryRepository.getByType(typeLower).observe(this, categoriesLower -> {
                    if (categoriesLower == null || categoriesLower.isEmpty()) {
                        // Nếu chưa có categories, sync từ backend
                        categoryRepository.fetchRemoteCategoriesByType(selectedGroup);
                        return;
                    }
                    displayCategories(categoriesLower, container, dialog);
                });
                return;
            }
            displayCategories(categories, container, dialog);
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void displayCategories(List<CategoryEntity> categories, LinearLayout container, BottomSheetDialog dialog) {
        container.removeAllViews();

        for (CategoryEntity category : categories) {
            // ✅ Tạo item view với icon và tên
            View itemView = createCategoryItemView(category, dialog);
            container.addView(itemView);
        }
    }

    // ✅ Tạo view cho mỗi category item (có icon và tên)
    private View createCategoryItemView(CategoryEntity category, BottomSheetDialog dialog) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setPadding(32, 32, 32, 32);
        itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
        itemLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // ✅ Icon
        ImageView iconView = new ImageView(this);
        int iconResId = getIconResourceId(category.getIcon());
        iconView.setImageResource(iconResId);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
        iconView.setPadding(0, 0, 16, 0);
        itemLayout.addView(iconView);

        // ✅ Category name
        TextView tvName = new TextView(this);
        tvName.setText(category.getName());
        tvName.setTextSize(17);
        tvName.setTextColor(Color.WHITE);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        itemLayout.addView(tvName);

        // ✅ Click listener
        itemLayout.setOnClickListener(v -> {
            // Set category name
            selectedCategoryName = category.getName();
            etCategory.setText(category.getName());
            
            // ✅ Auto-fill Title nếu đang rỗng, nếu đã có text thì giữ nguyên
            if (etTitle.getText().toString().trim().isEmpty()) {
                etTitle.setText(category.getName());
            }
            
            dialog.dismiss();
            currentDialog = null;
            
            // ✅ Lấy categoryId từ local DB (nếu có remoteId) hoặc query từ backend
            // TODO: Cần thêm remoteId vào CategoryEntity để lưu categoryId từ backend
            // Hiện tại vẫn query từ backend async
            fetchCategoryIdFromBackend(category.getName(), category.getType());
        });

        return itemLayout;
    }

    // ✅ Helper method để lấy icon resource ID từ icon name
    private int getIconResourceId(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_default_category;
        }

        Resources resources = getResources();
        String packageName = getPackageName();
        String trimmedName = iconName.trim();
        int resId = resources.getIdentifier(trimmedName, "drawable", packageName);

        // Nếu không tìm thấy, thử lowercase
        if (resId == 0) {
            resId = resources.getIdentifier(trimmedName.toLowerCase(), "drawable", packageName);
        }

        return resId != 0 ? resId : R.drawable.ic_default_category;
    }

    // ✅ Fetch categoryId from backend by category name (async, không block UI)
    private void fetchCategoryIdFromBackend(String categoryName, String categoryType) {
        if (!networkChecker.isNetworkAvailable()) {
            // No network - categoryId will be null, but categoryName is already set
            selectedCategoryId = null;
            return;
        }

        // Query categories from backend to find categoryId (async)
        categoryRemoteRepository.fetchCategoriesByType(categoryType, new ApiCallback<List<CategoryResponse>>() {
            @Override
            public void onSuccess(List<CategoryResponse> categories) {
                if (categories != null) {
                    for (CategoryResponse cat : categories) {
                        if (cat.getName().equals(categoryName)) {
                            selectedCategoryId = cat.getId();
                            // categoryName and etTitle are already set, no need to update UI
                            return;
                        }
                    }
                }
                // Category not found - categoryId will be null, but categoryName is already set
                selectedCategoryId = null;
            }

            @Override
            public void onError(String message) {
                // Error fetching - categoryId will be null, but categoryName is already set
                selectedCategoryId = null;
            }
        });
    }

    // ================================
    // 3) BottomSheet: Danh sách ví
    // ================================
    private void showWalletBottomSheet() {
        // ✅ Dismiss previous dialog if exists
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        currentDialog = dialog;
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_friends, null);

        LinearLayout container = view.findViewById(R.id.layoutFriendItems);
        TextView tvNone = view.findViewById(R.id.tvFriendNone);
        
        // Đổi text "Không có bạn" thành "Không chọn"
        tvNone.setText("Không chọn");

        container.removeAllViews();

        // ✅ Load wallets từ database
        walletRepository.getAll(new WalletRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> wallets) {
                // ✅ Run on UI thread to avoid "Can't toast on a thread that has not called Looper.prepare()" error
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (wallets == null || wallets.isEmpty()) {
                        Toast.makeText(AddTransactionActivity.this, getString(R.string.no_wallets), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        currentDialog = null;
                        return;
                    }

                    // Tạo TextView cho mỗi wallet
                    for (WalletEntity wallet : wallets) {
                        TextView tv = new TextView(AddTransactionActivity.this);
                        tv.setText(wallet.name);
                        tv.setTextSize(17);
                        tv.setPadding(32, 32, 32, 32);
                        tv.setTextColor(Color.WHITE);
                        tv.setBackgroundResource(android.R.drawable.list_selector_background);

                        tv.setOnClickListener(v -> {
                            selectedWalletId = wallet.id;
                            selectedWalletName = wallet.name;
                            etWallet.setText(wallet.name);
                            etWallet.setTextColor(Color.BLACK);
                            dialog.dismiss();
                            currentDialog = null;
                        });

                        container.addView(tv);
                    }
                });
            }
        });

        tvNone.setOnClickListener(v -> {
            selectedWalletId = null;
            selectedWalletName = null;
            etWallet.setText("");
            etWallet.setTextColor(Color.WHITE);
            dialog.dismiss();
            currentDialog = null;
        });

        dialog.setContentView(view);
        dialog.show();
    }

    // ================================
    // 4) BottomSheet: Chọn ngày
    // ================================
    private void showDueDateBottomSheet() {
        // ✅ Dismiss previous dialog if exists
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        currentDialog = dialog;
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_due_date, null);

        TextView tvToday  = view.findViewById(R.id.tvDueToday);
        TextView tvSingle = view.findViewById(R.id.tvDueSingle);
        TextView tvRange  = view.findViewById(R.id.tvDueRange);
        TextView tvClear  = view.findViewById(R.id.tvDueClear);

        // 1) Trong ngày
        tvToday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH) + 1;
            int d = c.get(Calendar.DAY_OF_MONTH);

            tvDate.setText(d + "/" + m + "/" + y);
            tvDate.setTextColor(Color.BLACK);
            dialog.dismiss();
            currentDialog = null;
        });

        // 2) Chọn 1 ngày
        tvSingle.setOnClickListener(v -> {
            dialog.dismiss();
            currentDialog = null;
            showSingleDatePicker();
        });

        // 3) Chọn từ → đến
        tvRange.setOnClickListener(v -> {
            dialog.dismiss();
            currentDialog = null;
            showStartDatePicker();
        });

        // 4) Xóa
        tvClear.setOnClickListener(v -> {
            tvDate.setText("Ngày giao dịch");
            tvDate.setTextColor(getResources().getColor(android.R.color.darker_gray));
            startDate = "";
            endDate = "";
            dialog.dismiss();
            currentDialog = null;
        });

        dialog.setContentView(view);
        dialog.show();
    }

    // 1 NGÀY
    private void showSingleDatePicker() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    tvDate.setText(d + "/" + (m + 1) + "/" + y);
                    tvDate.setTextColor(Color.BLACK);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        picker.setTitle("Chọn ngày");
        picker.show();
    }

    // CHỌN TỪ NGÀY
    private void showStartDatePicker() {

        Calendar c = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    startDate = d + "/" + (m + 1) + "/" + y;
                    showEndDatePicker();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        picker.setTitle("Chọn ngày bắt đầu");
        picker.show();
    }

    // CHỌN ĐẾN NGÀY
    private void showEndDatePicker() {

        Calendar c = Calendar.getInstance();

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    endDate = d + "/" + (m + 1) + "/" + y;

                    tvDate.setText(startDate + " → " + endDate);
                    tvDate.setTextColor(Color.BLACK);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        picker.setTitle("Chọn ngày kết thúc");
        picker.show();
    }

    // ================================
    // 5) Lưu giao dịch
    // ================================
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate group
        if (selectedGroup.isEmpty() || etGroup.getText().toString().trim().isEmpty()) {
            etGroup.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            etGroup.setError(null);
        }

        // Validate category
        if (selectedCategoryName == null || etCategory.getText().toString().trim().isEmpty()) {
            etCategory.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            etCategory.setError(null);
        }

        // Validate title - không bắt buộc, nhưng nếu rỗng thì dùng category name
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty() && selectedCategoryName != null) {
            title = selectedCategoryName; // Auto-fill từ category nếu rỗng
        }
        etTitle.setError(null);

        // Validate amount - thống nhất parse logic
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            etAmount.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            try {
                // ✅ Thống nhất: loại bỏ tất cả ký tự không phải số (VND không có phần lẻ)
                String cleanedAmount = amountStr.replaceAll("[^0-9]", "");
                if (cleanedAmount.isEmpty()) {
                    etAmount.setError(getString(R.string.amount_invalid));
                    isValid = false;
                } else {
                    long amount = Long.parseLong(cleanedAmount);
                    if (amount <= 0) {
                        etAmount.setError(getString(R.string.amount_invalid));
                        isValid = false;
                    } else {
                        etAmount.setError(null);
                    }
                }
            } catch (NumberFormatException e) {
                etAmount.setError(getString(R.string.amount_invalid));
                isValid = false;
            }
        }

        // ✅ Validate wallet
        if (selectedWalletId == null || selectedWalletName == null || etWallet.getText().toString().trim().isEmpty()) {
            etWallet.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            etWallet.setError(null);
        }

        if (!isValid) {
            Toast.makeText(this, R.string.fill_all_info, Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private void saveTransaction() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        String title = etTitle.getText().toString().trim();
        // ✅ Nếu title rỗng, dùng category name
        if (title.isEmpty() && selectedCategoryName != null) {
            title = selectedCategoryName;
        }
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        // ✅ Parse amount - thống nhất với validateInputs()
        double amountDouble;
        try {
            // Loại bỏ tất cả ký tự không phải số (VND không có phần lẻ)
            String cleanedAmount = amountStr.replaceAll("[^0-9]", "");
            if (cleanedAmount.isEmpty()) {
                Toast.makeText(this, getString(R.string.amount_invalid), Toast.LENGTH_SHORT).show();
                return;
            }
            amountDouble = Long.parseLong(cleanedAmount);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.amount_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        // Format amount string for display
        String amountFormatted = String.format(Locale.getDefault(), "%,.0f", amountDouble);

        // Parse date
        String dateStr = tvDate.getText().toString().trim();
        String dateISO = "";
        
        if (dateStr.isEmpty() || dateStr.equals("Ngày giao dịch") || dateStr.equals(getString(R.string.due_date))) {
            // Use current date if not selected
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateISO = sdf.format(c.getTime());
        } else {
            // Parse from display format (dd/MM/yyyy) to ISO (yyyy-MM-dd)
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                // Handle date range format (start → end)
                if (dateStr.contains("→")) {
                    String[] parts = dateStr.split("→");
                    if (parts.length == 2) {
                        dateStr = parts[0].trim(); // Use start date
                    }
                }
                
                java.util.Date date = displayFormat.parse(dateStr);
                if (date != null) {
                    dateISO = isoFormat.format(date);
                } else {
                    dateISO = dateStr; // Fallback to original
                }
            } catch (Exception e) {
                // If parsing fails, use current date
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dateISO = sdf.format(c.getTime());
            }
        }

        // Create TransactionEntity for local storage
        TransactionEntity transaction = new TransactionEntity(
                title,                                    // name
                selectedCategoryName != null ? selectedCategoryName : "", // category
                amountFormatted,                          // amount (formatted string)
                selectedWalletName != null ? selectedWalletName : "", // wallet
                dateISO,                                 // date (ISO format)
                selectedGroup,                            // type (INCOME/EXPENSE)
                amountDouble                              // amountDouble
        );

        // ✅ Tạo final variables để dùng trong inner class
        final TransactionEntity finalTransaction = transaction;
        final String finalDateISO = dateISO;
        final String finalNote = note;
        final String finalSelectedWalletId = selectedWalletId;
        final String finalSelectedCategoryId = selectedCategoryId;
        final String finalSelectedWalletName = selectedWalletName; // ✅ Final variable cho wallet name

        // Save to local database first (offline-first)
        // ✅ Insert với callback để đảm bảo có id trước khi sync
        transactionRepository.insert(finalTransaction, new TransactionRepository.OnResultCallback<Long>() {
            @Override
            public void onResult(Long id) {
                // ✅ Check if Activity is still valid
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                // ✅ Đảm bảo id được set vào finalTransaction
                if (id != null && id > 0) {
                    finalTransaction.id = id.intValue();
                }
                
                try {
                    // ✅ Update wallet balance ngay sau khi insert transaction
                    // ✅ Sử dụng finalSelectedWalletName thay vì selectedWalletName
                    // ✅ Delay nhỏ để đảm bảo transaction đã được commit vào database
                    if (finalSelectedWalletName != null && !finalSelectedWalletName.isEmpty()) {
                        // ✅ Sử dụng Handler để delay một chút, đảm bảo transaction đã được commit
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            try {
                                com.finmate.data.local.database.AppDatabase db = com.finmate.data.local.database.AppDatabase.getDatabase(getApplicationContext());
                                walletRepository.updateWalletBalance(finalSelectedWalletName, db.transactionDao());
                            } catch (Exception e) {
                                android.util.Log.e("AddTransactionActivity", "Error updating wallet balance: " + e.getMessage(), e);
                                // ✅ Không crash app nếu update wallet balance fail
                            }
                        }, 100); // Delay 100ms để đảm bảo transaction đã được commit
                    }
                    
                    // ✅ Transaction đã được lưu local với id → sẽ hiện ra Home ngay khi quay lại
                    // ✅ Try to sync with backend ngay nếu có mạng và đủ thông tin
                    // ✅ Chỉ sync lên backend nếu walletId KHÔNG phải ví local-only (id prefix \"local-\")
                    if (networkChecker.isNetworkAvailable()
                            && finalSelectedWalletId != null
                            && !finalSelectedWalletId.startsWith("local-")
                            && finalSelectedCategoryId != null) {
                        // ✅ Truyền final variables vào syncTransactionToBackend
                        syncTransactionToBackend(finalTransaction, finalDateISO, finalNote, finalSelectedWalletId, finalSelectedCategoryId);
                    } else {
                        // No network or missing IDs - transaction vẫn được lưu local
                        // Sẽ tự động sync sau khi có mạng (qua TransactionSyncManager)
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            try {
                                if (isEditMode) {
                                    Toast.makeText(AddTransactionActivity.this, "Đã cập nhật giao dịch!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AddTransactionActivity.this, R.string.transaction_saved, Toast.LENGTH_SHORT).show();
                                }
                                safeFinish();
                            } catch (Exception e) {
                                android.util.Log.e("AddTransactionActivity", "Error showing toast: " + e.getMessage(), e);
                                safeFinish();
                            }
                        });
                    }
                } catch (Exception e) {
                    android.util.Log.e("AddTransactionActivity", "Error in insert callback: " + e.getMessage(), e);
                    // ✅ Nếu có lỗi, vẫn finish activity
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(AddTransactionActivity.this, "Đã lưu giao dịch", Toast.LENGTH_SHORT).show();
                            safeFinish();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        handleCancel();
    }

    /**
     * ✅ Finish activity an toàn: Kiểm tra xem có activity nào trong stack không
     * Nếu không có, start HomeActivity trước khi finish để tránh app tắt
     */
    private void safeFinish() {
        try {
            if (isTaskRoot()) {
                // Nếu đây là root activity, start HomeActivity trước khi finish
                Intent intent = new Intent(this, com.finmate.ui.home.HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                // Có activity trong stack, chỉ cần finish
                finish();
            }
        } catch (Exception e) {
            android.util.Log.e("AddTransactionActivity", "Error in safeFinish: " + e.getMessage(), e);
            // Fallback: thử finish trực tiếp
            try {
                finish();
            } catch (Exception e2) {
                android.util.Log.e("AddTransactionActivity", "Error finishing activity: " + e2.getMessage(), e2);
            }
        }
    }

    private void handleCancel() {
        safeFinish();
    }

    private void syncTransactionToBackend(TransactionEntity transaction, String dateISO, String note, String walletId, String categoryId) {
        // Convert dateISO (yyyy-MM-dd) to ISO datetime string with timezone (yyyy-MM-dd'T'HH:mm:ss'Z')
        // ✅ Backend expect Instant which requires ISO-8601 format with timezone (Z for UTC)
        String occurredAtISO = dateISO + "T00:00:00Z";
        
        // Create request - ✅ Sử dụng walletId và categoryId được truyền vào (đã final)
        CreateTransactionRequest request = new CreateTransactionRequest(
                walletId,
                categoryId,
                transaction.type, // ✅ Sử dụng type từ transaction thay vì selectedGroup
                BigDecimal.valueOf(transaction.amountDouble),
                "VND", // Default currency
                occurredAtISO,
                note, // ✅ Gửi đúng note từ etNote
                null // transferRefId
        );

        transactionRemoteRepository.createTransaction(request, new ApiCallback<TransactionResponse>() {
            @Override
            public void onSuccess(TransactionResponse response) {
                // ✅ Check if Activity is still valid
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                // ✅ Update transaction local với remoteId từ backend để tránh duplicate
                // ✅ Chỉ update nếu transaction có id hợp lệ (> 0)
                try {
                    if (response != null && response.getId() != null && !response.getId().isEmpty()) {
                        final String remoteId = response.getId();
                        final int localId = transaction.id;
                        
                        android.util.Log.d("AddTransactionActivity", "Updating transaction with localId=" + localId + ", remoteId=" + remoteId);
                        
                        // ✅ Đảm bảo transaction.id đã được set (từ insert callback)
                        if (localId > 0) {
                            // Set remoteId và update
                            transaction.remoteId = remoteId;
                            transactionRepository.update(transaction);
                            android.util.Log.d("AddTransactionActivity", "Transaction updated successfully with remoteId");
                        } else {
                            android.util.Log.w("AddTransactionActivity", "Transaction id not set yet (id=" + localId + "), remoteId will be set on next sync. remoteId=" + remoteId);
                        }
                    } else {
                        android.util.Log.w("AddTransactionActivity", "Response is null or missing id");
                    }
                } catch (Exception e) {
                    android.util.Log.e("AddTransactionActivity", "Error updating transaction remoteId: " + e.getMessage(), e);
                    e.printStackTrace();
                    // ✅ Không crash app, chỉ log error - transaction đã được lưu local
                }
                
                // ✅ Update wallet balance sau khi sync thành công với backend
                // ✅ Sử dụng transaction.wallet thay vì finalSelectedWalletName (vì không có trong scope)
                if (transaction.wallet != null && !transaction.wallet.isEmpty()) {
                    try {
                        com.finmate.data.local.database.AppDatabase db = com.finmate.data.local.database.AppDatabase.getDatabase(getApplicationContext());
                        walletRepository.updateWalletBalance(transaction.wallet, db.transactionDao());
                        android.util.Log.d("AddTransactionActivity", "Wallet balance updated after sync");
                    } catch (Exception e) {
                        android.util.Log.e("AddTransactionActivity", "Error updating wallet balance after sync: " + e.getMessage(), e);
                        // ✅ Không crash app nếu update wallet balance fail
                    }
                }
                
                // Transaction synced successfully
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    try {
                        if (isEditMode) {
                            Toast.makeText(AddTransactionActivity.this, "Đã cập nhật giao dịch!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddTransactionActivity.this, R.string.transaction_saved, Toast.LENGTH_SHORT).show();
                        }
                        // ✅ Kiểm tra xem có activity nào trong stack không trước khi finish
                        safeFinish();
                    } catch (Exception e) {
                        android.util.Log.e("AddTransactionActivity", "Error showing toast or finishing: " + e.getMessage(), e);
                        // ✅ Nếu có lỗi, vẫn cố finish an toàn
                        safeFinish();
                    }
                });
            }

            @Override
            public void onError(String message) {
                // ✅ Check if Activity is still valid
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                
                // Sync failed, but transaction is already saved locally
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    try {
                        Toast.makeText(AddTransactionActivity.this, "Đã lưu (chưa đồng bộ)", Toast.LENGTH_SHORT).show();
                        safeFinish();
                    } catch (Exception e) {
                        android.util.Log.e("AddTransactionActivity", "Error showing toast: " + e.getMessage(), e);
                        safeFinish();
                    }
                });
            }
        });
    }
}
