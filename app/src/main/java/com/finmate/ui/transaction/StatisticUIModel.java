package com.finmate.ui.transaction;

public class StatisticUIModel {

    private final String label;  // Ví dụ: "Tháng 1", "Chi tiêu"
    private final float value;   // Giá trị cho biểu đồ

    public StatisticUIModel(String label, float value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public float getValue() { return value; }
}
