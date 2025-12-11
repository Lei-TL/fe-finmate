package com.finmate.ui.transaction;

public class ChartPoint {

    private final String label;
    private final float value;

    public ChartPoint(String label, double value) {
        this.label = label;
        this.value = (float) value;
    }

    public String getLabel() {
        return label;
    }

    public float getValue() {
        return value;
    }
}

