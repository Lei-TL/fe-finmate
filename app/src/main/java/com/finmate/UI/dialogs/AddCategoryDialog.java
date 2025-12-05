package com.finmate.UI.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.finmate.R;

public class AddCategoryDialog extends Dialog {

    public interface OnCategoryAddedListener {
        void onCategoryAdded(String name, String type); // type = "income" | "expense"
    }

    private OnCategoryAddedListener listener;
    private final String categoryType; // truyền "income" hoặc "expense"

    public AddCategoryDialog(Context context, String categoryType, OnCategoryAddedListener listener) {
        super(context);
        this.categoryType = categoryType;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_category);

        EditText edtName = findViewById(R.id.edtName);
        EditText edtNote = findViewById(R.id.edtNote);

        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnSave = findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Tên danh mục không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onCategoryAdded(name, categoryType);
            }

            dismiss();
        });
    }
}
