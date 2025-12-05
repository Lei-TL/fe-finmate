package com.finmate.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;

public class AccountActivity extends AppCompatActivity {

    private ImageView btnBack, btnCamera, btnEdit;
    private EditText edtName, edtEmail, edtBirthday, edtNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initViews();
        handleEvents();
        
        // Mặc định không cho chỉnh sửa
        setEditingEnabled(false);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnCamera = findViewById(R.id.btnCamera);
        
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtBirthday = findViewById(R.id.edtBirthday);
        edtNote = findViewById(R.id.edtNote);
    }

    private void handleEvents() {

        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v ->
                Toast.makeText(this, "Đổi avatar", Toast.LENGTH_SHORT).show()
        );

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Chế độ chỉnh sửa", Toast.LENGTH_SHORT).show();
            setEditingEnabled(true);
        });
    }

    private void setEditingEnabled(boolean enabled) {
        edtName.setEnabled(enabled);
        edtEmail.setEnabled(enabled);
        edtBirthday.setEnabled(enabled);
        edtNote.setEnabled(enabled);
        
        // Ẩn/Hiện nút Camera avatar khi edit
        btnCamera.setVisibility(enabled ? View.VISIBLE : View.GONE);
        
        // Đổi màu text edittext để user biết trạng thái (optional)
        int color = enabled ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.darker_gray);
        // Có thể set color ở đây nếu muốn
    }
}
