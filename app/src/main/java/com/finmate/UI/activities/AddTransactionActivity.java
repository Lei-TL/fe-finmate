package com.finmate.UI.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.finmate.R;

public class AddTransactionActivity extends BaseActivity {

    EditText etCategory, etTitle, etAmount, etNote;
    TextView tvFriend, tvDate;
    Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Ánh xạ
        etCategory = findViewById(R.id.etCategory);
        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);

        tvFriend = findViewById(R.id.tvFriend);
        tvDate = findViewById(R.id.tvDate);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Xử lý nút
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            // TODO: lưu vào database
        });
    }
}
