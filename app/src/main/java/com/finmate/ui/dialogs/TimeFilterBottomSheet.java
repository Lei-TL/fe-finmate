package com.finmate.ui.dialogs;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.finmate.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeFilterBottomSheet extends BottomSheetDialogFragment {

    public interface TimeFilterListener {
        void onTodaySelected();
        void onSingleDaySelected(Date date);
        void onDateRangeSelected(Date startDate, Date endDate);
        void onClear();
    }

    private TimeFilterListener listener;
    private Date selectedSingleDay;
    private Date selectedStartDate;
    private Date selectedEndDate;
    private String currentFilterType = "none"; // "today", "single", "range", "none"

    private ImageView ivTodayCheck, ivSingleDayCheck, ivDateRangeCheck;
    private TextView tvSingleDayDate, tvStartDate, tvEndDate;

    public static TimeFilterBottomSheet newInstance() {
        return new TimeFilterBottomSheet();
    }

    public void setListener(TimeFilterListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_time_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivTodayCheck = view.findViewById(R.id.ivTodayCheck);
        ivSingleDayCheck = view.findViewById(R.id.ivSingleDayCheck);
        ivDateRangeCheck = view.findViewById(R.id.ivDateRangeCheck);
        tvSingleDayDate = view.findViewById(R.id.tvSingleDayDate);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);

        LinearLayout btnToday = view.findViewById(R.id.btnToday);
        LinearLayout btnSingleDay = view.findViewById(R.id.btnSingleDay);
        LinearLayout btnDateRange = view.findViewById(R.id.btnDateRange);
        LinearLayout btnClear = view.findViewById(R.id.btnClear);

        btnToday.setOnClickListener(v -> {
            currentFilterType = "today";
            updateCheckIcons();
            if (listener != null) {
                listener.onTodaySelected();
            }
            dismiss();
        });

        btnSingleDay.setOnClickListener(v -> showSingleDayPicker());

        tvSingleDayDate.setOnClickListener(v -> showSingleDayPicker());

        btnDateRange.setOnClickListener(v -> {
            currentFilterType = "range";
            updateCheckIcons();
        });

        tvStartDate.setOnClickListener(v -> showStartDatePicker());
        tvEndDate.setOnClickListener(v -> showEndDatePicker());

        btnClear.setOnClickListener(v -> {
            currentFilterType = "none";
            selectedSingleDay = null;
            selectedStartDate = null;
            selectedEndDate = null;
            updateCheckIcons();
            updateDateTexts();
            if (listener != null) {
                listener.onClear();
            }
            dismiss();
        });

        updateCheckIcons();
        updateDateTexts();
    }

    private void showSingleDayPicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedSingleDay != null) {
            calendar.setTime(selectedSingleDay);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    selectedSingleDay = cal.getTime();
                    currentFilterType = "single";
                    updateCheckIcons();
                    updateDateTexts();
                    if (listener != null) {
                        listener.onSingleDaySelected(selectedSingleDay);
                    }
                    dismiss();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showStartDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedStartDate != null) {
            calendar.setTime(selectedStartDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    selectedStartDate = cal.getTime();
                    updateDateTexts();
                    if (selectedStartDate != null && selectedEndDate != null) {
                        if (listener != null) {
                            listener.onDateRangeSelected(selectedStartDate, selectedEndDate);
                        }
                        dismiss();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showEndDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedEndDate != null) {
            calendar.setTime(selectedEndDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    selectedEndDate = cal.getTime();
                    updateDateTexts();
                    if (selectedStartDate != null && selectedEndDate != null) {
                        if (listener != null) {
                            listener.onDateRangeSelected(selectedStartDate, selectedEndDate);
                        }
                        dismiss();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateCheckIcons() {
        ivTodayCheck.setVisibility("today".equals(currentFilterType) ? View.VISIBLE : View.GONE);
        ivSingleDayCheck.setVisibility("single".equals(currentFilterType) ? View.VISIBLE : View.GONE);
        ivDateRangeCheck.setVisibility("range".equals(currentFilterType) ? View.VISIBLE : View.GONE);
    }

    private void updateDateTexts() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (selectedSingleDay != null) {
            tvSingleDayDate.setText(sdf.format(selectedSingleDay));
        } else {
            tvSingleDayDate.setText("");
        }

        if (selectedStartDate != null) {
            tvStartDate.setText(sdf.format(selectedStartDate));
        } else {
            tvStartDate.setText(getString(R.string.select_start_date));
        }

        if (selectedEndDate != null) {
            tvEndDate.setText(sdf.format(selectedEndDate));
        } else {
            tvEndDate.setText(getString(R.string.select_end_date));
        }
    }
}



