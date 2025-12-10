package com.finmate.ui.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.finmate.ui.base.BaseActivity;

import androidx.annotation.Nullable;

import com.finmate.R;
import com.finmate.core.util.NetworkUtils;
import com.finmate.core.util.TransactionFormatter;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.repository.TransactionLocalRepository;
import com.finmate.data.repository.TransactionRemoteRepository;
import com.finmate.ui.base.BaseActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.util.Calendar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddTransactionActivity extends BaseActivity {

    @Inject
    TransactionLocalRepository localRepository;
    @Inject
    TransactionRemoteRepository remoteRepository;

    EditText etCategory, etTitle, etAmount, tvFriend, etNote;
    TextView tvDate;
    Button btnCancel, btnSave;

    String selectedGroup = "";

    String startDate = "";
    String endDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupEditTextColor();
        setupEvents();
    }

    private void saveTransaction() {
        String group = etCategory.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String amountInput = etAmount.getText().toString().trim();
        String dateDisplay = tvDate.getText().toString().trim();
        String wallet = "Ví của tôi";

        if (group.isEmpty() || title.isEmpty() || amountInput.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse amount từ String input → double
        double amount;
        try {
            amount = TransactionFormatter.parseAmount(amountInput);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert date từ display format (dd/MM/yyyy) → ISO format
        String occurredAt;
        try {
            if (dateDisplay.isEmpty() || dateDisplay.equals("Đến hạn")) {
                // Nếu chưa chọn date, dùng ngày hiện tại
                Calendar c = Calendar.getInstance();
                occurredAt = TransactionFormatter.calendarToISO(
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH)
                );
            } else {
                // Parse từ format hiển thị (dd/MM/yyyy) sang ISO
                occurredAt = TransactionFormatter.parseDateToISO(dateDisplay);
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Ngày không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        TransactionEntity transaction = new TransactionEntity(title, group, amount, wallet, occurredAt);

        if (NetworkUtils.isOnline(this)) {
            // Online: Try to create on backend
            remoteRepository.createFromLocal(transaction, new ApiCallback<TransactionResponse>() {
                @Override
                public void onSuccess(TransactionResponse data) {
                    // BE success: save the synced version locally
                    String titleFromBE = (data.getNote() != null && !data.getNote().isEmpty()) ? data.getNote() : data.getCategoryName();
                    TransactionEntity syncedEntity = new TransactionEntity(
                            data.getId(),
                            titleFromBE,
                            data.getCategoryName(),
                            data.getAmount(),  // amount đã là double
                            data.getWalletName(),
                            data.getOccurredAt()  // occurredAt là ISO string
                    );
                    localRepository.insert(syncedEntity);
                    Toast.makeText(AddTransactionActivity.this, "Đã lưu và đồng bộ!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message, @Nullable Integer code) {
                    if (code != null && code >= 400 && code < 500) {
                        // Business error (4xx): Show error, do not save locally
                        Toast.makeText(AddTransactionActivity.this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
                    } else {
                        // Network/Server error (5xx, IOException): Save locally as pending
                        localRepository.insert(transaction);
                        Toast.makeText(AddTransactionActivity.this, "Lưu tạm (offline)!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } else {
            // Offline: Save locally as pending
            localRepository.insert(transaction);
            Toast.makeText(this, "Lưu tạm (offline)!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ... (the rest of the methods are unchanged)

    private void initViews() {
        etCategory = findViewById(R.id.etCategory);
        etTitle    = findViewById(R.id.etTitle);
        etAmount   = findViewById(R.id.etAmount);
        tvFriend   = findViewById(R.id.tvFriend);
        tvDate     = findViewById(R.id.tvDate);
        etNote     = findViewById(R.id.etNote);

        btnCancel  = findViewById(R.id.btnCancel);
        btnSave    = findViewById(R.id.btnSave);
    }

    private void setupEditTextColor() {
        applyWatcher(etCategory);
        applyWatcher(etTitle);
        applyWatcher(etAmount);
        applyWatcher(tvFriend);
        applyWatcher(etNote);
    }

    private void applyWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0)
                    editText.setTextColor(Color.WHITE);
                else
                    editText.setTextColor(Color.BLACK);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupEvents() {

        etCategory.setOnClickListener(v -> showGroupBottomSheet());

        etTitle.setOnClickListener(v -> {
            if (selectedGroup.isEmpty()) {
                Toast.makeText(this, "Hãy chọn nhóm giao dịch trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            showCategoryItemsBottomSheet();
        });

        tvFriend.setOnClickListener(v -> showFriendBottomSheet());

        tvDate.setOnClickListener(v -> showDueDateBottomSheet());

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void showGroupBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_group, null);

        TextView tvIncome  = view.findViewById(R.id.tvIncomeGroup);
        TextView tvExpense = view.findViewById(R.id.tvExpenseGroup);

        tvIncome.setOnClickListener(v -> {
            selectedGroup = "Thu nhập";
            etCategory.setText(selectedGroup);
            etTitle.setText("");
            dialog.dismiss();
        });

        tvExpense.setOnClickListener(v -> {
            selectedGroup = "Chi tiêu";
            etCategory.setText(selectedGroup);
            etTitle.setText("");
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showCategoryItemsBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_category_items, null);

        LinearLayout container = view.findViewById(R.id.layoutCategoryItems);
        container.removeAllViews();

        String[] items;

        if (selectedGroup.equals("Thu nhập")) {
            items = new String[]{"Lương", "Thưởng", "Phụ cấp", "Bán hàng", "Khác"};
        } else {
            items = new String[]{"Ăn uống", "Mua sắm", "Đi lại", "Hóa đơn", "Khác"};
        }

        for (String item : items) {
            TextView tv = new TextView(this);
            tv.setText(item);
            tv.setTextSize(17);
            tv.setPadding(32, 32, 32, 32);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(android.R.drawable.list_selector_background);

            tv.setOnClickListener(v -> {
                etTitle.setText(item);
                dialog.dismiss();
            });

            container.addView(tv);
        }

        dialog.setContentView(view);
        dialog.show();
    }

    private void showFriendBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_friends, null);

        LinearLayout container = view.findViewById(R.id.layoutFriendItems);
        TextView tvNone = view.findViewById(R.id.tvFriendNone);

        container.removeAllViews();

        String[] friends = new String[]{
                "Nguyễn Văn A",
                "Trần Minh Hiếu",
                "Lê Thị B",
                "Ngô Quốc C",
                "Phạm Hoàng D"
        };

        for (String f : friends) {
            TextView tv = new TextView(this);
            tv.setText(f);
            tv.setTextSize(17);
            tv.setPadding(32, 32, 32, 32);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(android.R.drawable.list_selector_background);

            tv.setOnClickListener(v -> {
                tvFriend.setText(f);
                dialog.dismiss();
            });

            container.addView(tv);
        }

        tvNone.setOnClickListener(v -> {
            tvFriend.setText("");
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void showDueDateBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_due_date, null);

        TextView tvToday  = view.findViewById(R.id.tvDueToday);
        TextView tvSingle = view.findViewById(R.id.tvDueSingle);
        TextView tvRange  = view.findViewById(R.id.tvDueRange);
        TextView tvClear  = view.findViewById(R.id.tvDueClear);

        tvToday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int y = c.get(Calendar.YEAR);
            int m = c.get(Calendar.MONTH) + 1;
            int d = c.get(Calendar.DAY_OF_MONTH);

            tvDate.setText(d + "/" + m + "/" + y);
            tvDate.setTextColor(Color.BLACK);
            dialog.dismiss();
        });

        tvSingle.setOnClickListener(v -> {
            dialog.dismiss();
            showSingleDatePicker();
        });

        tvRange.setOnClickListener(v -> {
            dialog.dismiss();
            showStartDatePicker();
        });

        tvClear.setOnClickListener(v -> {
            tvDate.setText("Đến hạn");
            tvDate.setTextColor(Color.GRAY);
            startDate = "";
            endDate = "";
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

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
}
