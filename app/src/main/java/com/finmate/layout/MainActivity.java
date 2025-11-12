package com.finmate.layout;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;

import com.finmate.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LineChart chart = findViewById(R.id.lineChart);
        chart.setBackgroundColor(Color.parseColor("#44464E"));
        chart.setNoDataText("Không có dữ liệu hiển thị");
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setExtraOffsets(10, 10, 10, 10);

        // === Dữ liệu Thu nhập ===
        List<Entry> thuNhap = new ArrayList<>();
        thuNhap.add(new Entry(1, 5));
        thuNhap.add(new Entry(2, 10));
        thuNhap.add(new Entry(3, 60));
        thuNhap.add(new Entry(4, 55));
        thuNhap.add(new Entry(5, 15));
        thuNhap.add(new Entry(6, 90));

        LineDataSet dsThuNhap = new LineDataSet(thuNhap, "Thu nhập");
        dsThuNhap.setColor(Color.MAGENTA);
        dsThuNhap.setLineWidth(2.5f);
        dsThuNhap.setCircleColor(Color.MAGENTA);
        dsThuNhap.setCircleRadius(4f);
        dsThuNhap.setValueTextColor(Color.WHITE);
        dsThuNhap.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // === Dữ liệu Chi tiêu ===
        List<Entry> chiTieu = new ArrayList<>();
        chiTieu.add(new Entry(1, 30));
        chiTieu.add(new Entry(2, 10));
        chiTieu.add(new Entry(3, 8));
        chiTieu.add(new Entry(4, 15));
        chiTieu.add(new Entry(5, 35));
        chiTieu.add(new Entry(6, 12));

        LineDataSet dsChiTieu = new LineDataSet(chiTieu, "Chi tiêu");
        dsChiTieu.setColor(Color.GREEN);
        dsChiTieu.setLineWidth(2.5f);
        dsChiTieu.setCircleColor(Color.GREEN);
        dsChiTieu.setCircleRadius(4f);
        dsChiTieu.setValueTextColor(Color.WHITE);
        dsChiTieu.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // === Kết hợp dữ liệu ===
        LineData lineData = new LineData(dsThuNhap, dsChiTieu);
        chart.setData(lineData);

        // === Tùy chỉnh trục X ===
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return "Tháng " + (int) value;
            }
        });

        // === Tùy chỉnh trục Y ===
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.LTGRAY);
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);

        // === Chú thích (Legend) ===
        Legend legend = chart.getLegend();
        legend.setTextColor(Color.LTGRAY);
        legend.setForm(Legend.LegendForm.CIRCLE);

        chart.animateX(1500);
        chart.invalidate();
    }
}
