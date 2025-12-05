package com.finmate.ui.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.finmate.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NotificationBottomSheet extends BottomSheetDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Inflate (thổi phồng) layout của bạn
        View view = View.inflate(getContext(), R.layout.bottom_sheet_notification_actions, null);

        // Đặt layout đã thổi phồng làm nội dung cho dialog
        dialog.setContentView(view);

        // Xử lý sự kiện nhấn cho các nút
        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Xóa", Toast.LENGTH_SHORT).show();
            dismiss(); // Đóng bottom sheet
        });

        view.findViewById(R.id.btnReadAll).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đánh dấu tất cả đã đọc", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        view.findViewById(R.id.btnShare).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chia sẻ", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        view.findViewById(R.id.btnHelp).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Trợ giúp & phản hồi", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return dialog;
    }
}
