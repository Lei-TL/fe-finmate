package com.finmate.UI.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.finmate.entities.TransactionEntity;
import com.finmate.repository.TransactionRepository;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText etCategory, etTitle, etAmount, etNote;
    private TextView tvFriend, tvDate;
    private Button btnSave, btnCancel;

    private TransactionRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        repository = new TransactionRepository(this);

        mapViews();
        setupEvents();
    }

    private void mapViews() {
        etCategory = findViewById(R.id.etCategory);
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);

        tvFriend = findViewById(R.id.tvFriend);
        tvDate = findViewById(R.id.tvDate);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupEvents() {

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String category = etCategory.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String amount = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String friend = tvFriend.getText().toString().trim();
        String date = tvDate.getText().toString().trim();

        // Kiểm tra dữ liệu
        if (title.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo entity để lưu database
        TransactionEntity entity = new TransactionEntity(
                title,
                category,
                amount,
                friend,     // dùng friend như "wallet" (tùy bạn đổi)
                date
        );

        // Lưu database qua Repository (chạy background thread)
        repository.insert(entity);

        Toast.makeText(this, "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);
        finish();
    }
}
