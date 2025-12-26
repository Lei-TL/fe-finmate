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
import com.finmate.core.network.NetworkChecker;
import com.finmate.data.dto.UpdateUserRequest;
import com.finmate.data.dto.UserInfoResponse;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.remote.api.AuthService;
import com.finmate.data.repository.TransactionRepository;
import com.finmate.data.repository.WalletRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    
    @Inject
    TransactionRepository transactionRepository;
    
    @Inject
    WalletRepository walletRepository;
    
    @Inject
    NetworkChecker networkChecker;

    private ImageView btnBack, btnCamera, btnEdit;
    private EditText edtName, edtEmail, edtBirthday, edtNote;
    private Spinner spnLanguage, spnTheme;
    private Button btnSave;
    private android.widget.TextView tvIncomeValue, tvExpenseValue, tvBalanceValue;

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
        
        // ✅ Load và hiển thị chi tiêu, thu nhập, số dư
        loadFinancialSummary();
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
        
        // ✅ TextView cho thu chi số dư
        tvIncomeValue = findViewById(R.id.tvIncomeValue);
        tvExpenseValue = findViewById(R.id.tvExpenseValue);
        tvBalanceValue = findViewById(R.id.tvBalanceValue);
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
        
        // ✅ DatePickerDialog cho birthday
        edtBirthday.setOnClickListener(v -> {
            if (edtBirthday.isEnabled()) {
                showDatePickerDialog();
            }
        });

        btnSave.setOnClickListener(v -> {
            // ✅ Validate và save user info
            if (validateUserInfo()) {
                saveUserInfo();
            }
            
            // ✅ Save language và theme (local preferences)
            saveLanguageSelection();
            saveThemeSelection();
            
            // ✅ Disable editing mode sau khi save
            setEditingEnabled(false);
            
            // ✅ Reload user info từ backend để đảm bảo sync
            loadUserInfo();
            
            // ✅ Recreate để apply theme/language changes
            recreate();
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
    
    /**
     * ✅ Load và hiển thị tổng chi tiêu, thu nhập và số dư
     */
    private void loadFinancialSummary() {
        // Load tất cả transactions
        transactionRepository.getAll(new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
            @Override
            public void onResult(List<TransactionEntity> transactions) {
                // Tính tổng thu nhập và chi tiêu
                final double[] totalIncome = {0};
                final double[] totalExpense = {0};
                
                if (transactions != null) {
                    for (TransactionEntity t : transactions) {
                        if (t.type != null && t.amountDouble != 0) {
                            if ("INCOME".equals(t.type)) {
                                totalIncome[0] += t.amountDouble;
                            } else if ("EXPENSE".equals(t.type)) {
                                totalExpense[0] += t.amountDouble;
                            }
                        }
                    }
                }
                
                // Load wallets để tính tổng số dư
                walletRepository.getAll(new WalletRepository.Callback() {
                    @Override
                    public void onResult(List<WalletEntity> wallets) {
                        final double[] totalBalance = {0};
                        
                        if (wallets != null) {
                            for (WalletEntity w : wallets) {
                                totalBalance[0] += w.currentBalance;
                            }
                        }
                        
                        // ✅ Update UI trên main thread
                        runOnUiThread(() -> {
                            if (tvIncomeValue != null) {
                                tvIncomeValue.setText("+" + formatAmount(totalIncome[0]) + " VND");
                            }
                            if (tvExpenseValue != null) {
                                tvExpenseValue.setText("-" + formatAmount(totalExpense[0]) + " VND");
                            }
                            if (tvBalanceValue != null) {
                                tvBalanceValue.setText(formatAmount(totalBalance[0]) + " VND");
                            }
                        });
                    }
                });
            }
        });
    }
    
    /**
     * ✅ Format amount concisely to avoid breaking the layout
     * Giống với HomeActivity.formatAmount()
     */
    private String formatAmount(double amount) {
        if (amount >= 1_000_000_000) {
            // >= 1 billion: show as 1.2B
            return String.format(Locale.getDefault(), "%.1fB", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            // >= 1 million: show as 1.2M
            return String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            // >= 1 thousand: show as 1.2K
            return String.format(Locale.getDefault(), "%.1fK", amount / 1_000);
        } else {
            // < 1 thousand: show full amount
            return String.format(Locale.getDefault(), "%,.0f", amount);
        }
    }
    
    /**
     * ✅ Validate thông tin user trước khi save
     */
    private boolean validateUserInfo() {
        boolean isValid = true;
        
        // Validate name
        String name = edtName.getText().toString().trim();
        if (name.isEmpty()) {
            edtName.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            edtName.setError(null);
        }
        
        // Validate email
        String email = edtEmail.getText().toString().trim();
        if (email.isEmpty()) {
            edtEmail.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError(getString(R.string.invalid_email));
            isValid = false;
        } else {
            edtEmail.setError(null);
        }
        
        // Birthday và Note là optional, không cần validate
        
        if (!isValid) {
            Toast.makeText(this, R.string.fill_all_info, Toast.LENGTH_SHORT).show();
        }
        
        return isValid;
    }
    
    /**
     * ✅ Save user info lên backend
     */
    private void saveUserInfo() {
        String fullName = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String birthdayDisplay = edtBirthday.getText().toString().trim();
        String note = edtNote.getText().toString().trim();
        
        // ✅ Parse birthday từ dd/MM/yyyy sang yyyy-MM-dd
        String birthdayISO = parseBirthdayToISO(birthdayDisplay);
        
        // ✅ Tạo UpdateUserRequest
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName(fullName);
        request.setAvatarUrl(null); // TODO: Implement avatar upload later
        request.setBirthday(birthdayISO);
        request.setNote(note);
        
        // ✅ Gọi API update nếu có mạng
        if (networkChecker != null && networkChecker.isNetworkAvailable()) {
            authService.updateCurrentUser(request).enqueue(new Callback<UserInfoResponse>() {
                @Override
                public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserInfoResponse userInfo = response.body();
                        
                        // ✅ Lưu fullName vào SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        if (userInfo.getFullName() != null && !userInfo.getFullName().isEmpty()) {
                            prefs.edit().putString("full_name", userInfo.getFullName()).apply();
                        }
                        
                        // ✅ Lưu email và birthday vào SharedPreferences
                        if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
                            prefs.edit().putString("user_email", userInfo.getEmail()).apply();
                        }
                        if (userInfo.getBirthday() != null && !userInfo.getBirthday().isEmpty()) {
                            prefs.edit().putString("user_birthday", userInfo.getBirthday()).apply();
                        }
                        
                        Toast.makeText(AccountActivity.this, R.string.toast_changes_saved, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountActivity.this, "Lỗi cập nhật thông tin: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                    Toast.makeText(AccountActivity.this, "Lỗi cập nhật thông tin: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // ✅ Offline: Lưu vào SharedPreferences để sync sau
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("full_name", fullName)
                    .putString("user_email", email)
                    .putString("user_birthday", birthdayISO)
                    .putString("user_note", note)
                    .apply();
            
            Toast.makeText(this, "Đã lưu thông tin (chưa sync)", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * ✅ Parse birthday từ dd/MM/yyyy sang yyyy-MM-dd (ISO format)
     * Nếu birthday là "--/--/--" hoặc invalid, trả về null
     */
    private String parseBirthdayToISO(String birthdayDisplay) {
        if (birthdayDisplay == null || birthdayDisplay.isEmpty() || birthdayDisplay.equals("--/--/--")) {
            return null;
        }
        
        try {
            // Parse từ dd/MM/yyyy
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(birthdayDisplay);
            
            // Format sang yyyy-MM-dd (ISO)
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Nếu parse fail, trả về null
            return null;
        }
    }
    
    /**
     * ✅ Show DatePickerDialog để chọn birthday
     */
    private void showDatePickerDialog() {
        // ✅ Parse ngày hiện tại từ edtBirthday (nếu có)
        Calendar calendar = Calendar.getInstance();
        String currentBirthday = edtBirthday.getText().toString().trim();
        
        if (currentBirthday != null && !currentBirthday.isEmpty() && !currentBirthday.equals("--/--/--")) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = format.parse(currentBirthday);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (ParseException e) {
                // Nếu parse fail, dùng ngày hiện tại
            }
        }
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // ✅ Format ngày đã chọn thành dd/MM/yyyy
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = format.format(selectedDate.getTime());
                    edtBirthday.setText(formattedDate);
                },
                year, month, day
        );
        
        // ✅ Set max date là hôm nay (không cho chọn ngày tương lai)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }
}
