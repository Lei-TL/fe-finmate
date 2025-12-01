package com.finmate.UI.activities;

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

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    EditText etCategory, etTitle, etAmount, tvFriend, etNote;
    TextView tvDate;
    Button btnCancel, btnSave;

    String selectedGroup = "";

    // Ngày
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

    // ============================================
    // MÀU TEXT: Trống = trắng / Có chữ = đen
    // ============================================
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

        // Chọn ngày
        tvDate.setOnClickListener(v -> showDueDateBottomSheet());

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    // ================================
    // 1) BottomSheet: Chọn nhóm
    // ================================
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

    // ================================
    // 2) BottomSheet: Mục con
    // ================================
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

    // ================================
    // 3) BottomSheet: Danh sách bạn
    // ================================
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

    // ================================
    // 4) BottomSheet: Chọn ngày
    // ================================
    private void showDueDateBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
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
        });

        // 2) Chọn 1 ngày
        tvSingle.setOnClickListener(v -> {
            dialog.dismiss();
            showSingleDatePicker();
        });

        // 3) Chọn từ → đến
        tvRange.setOnClickListener(v -> {
            dialog.dismiss();
            showStartDatePicker();
        });

        // 4) Xóa
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
    private void saveTransaction() {

        String group  = etCategory.getText().toString().trim();
        String title  = etTitle.getText().toString().trim();
        String amount = etAmount.getText().toString().trim();

        if (group.isEmpty() || title.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
