package com.finmate.data.repository;

import com.finmate.data.local.database.model.StatisticPoint;

import java.util.List;

public class StatisticData {

    private final double totalAmount;
    private final List<StatisticPoint> timeline;
    private final List<StatisticPoint> byCategory;
    private final List<StatisticPoint> byWallet;

    public StatisticData(double totalAmount,
                         List<StatisticPoint> timeline,
                         List<StatisticPoint> byCategory,
                         List<StatisticPoint> byWallet) {
        this.totalAmount = totalAmount;
        this.timeline = timeline;
        this.byCategory = byCategory;
        this.byWallet = byWallet;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<StatisticPoint> getTimeline() {
        return timeline;
    }

    public List<StatisticPoint> getByCategory() {
        return byCategory;
    }

    public List<StatisticPoint> getByWallet() {
        return byWallet;
    }
}

