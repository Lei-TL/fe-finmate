package com.finmate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    private ImageView btnBack, btnCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initViews();
        handleEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCamera = findViewById(R.id.btnCamera);
    }

    private void handleEvents() {

        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v ->
                Toast.makeText(this, "Đổi avatar", Toast.LENGTH_SHORT).show()
        );
    }
}
