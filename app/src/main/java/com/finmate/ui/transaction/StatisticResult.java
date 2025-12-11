package com.finmate.ui.transaction;

import java.util.List;

public class StatisticResult {

    private final double totalAmount;
    private final List<ChartPoint> timeline;
    private final List<ChartPoint> byCategory;
    private final List<ChartPoint> byWallet;
    private final String dateLabel;
    private final String granularity;

    public StatisticResult(double totalAmount,
                           List<ChartPoint> timeline,
                           List<ChartPoint> byCategory,
                           List<ChartPoint> byWallet,
                           String dateLabel,
                           String granularity) {
        this.totalAmount = totalAmount;
        this.timeline = timeline;
        this.byCategory = byCategory;
        this.byWallet = byWallet;
        this.dateLabel = dateLabel;
        this.granularity = granularity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<ChartPoint> getTimeline() {
        return timeline;
    }

    public List<ChartPoint> getByCategory() {
        return byCategory;
    }

    public List<ChartPoint> getByWallet() {
        return byWallet;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public String getGranularity() {
        return granularity;
    }
}

