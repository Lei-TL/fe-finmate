package com.finmate.data.repository.model;

public class GoalProgress {
    public final double targetAmount;
    public final double savedAmount;
    public final double percent;

    public GoalProgress(double targetAmount, double savedAmount) {
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.percent = targetAmount > 0 ? (savedAmount / targetAmount) * 100.0 : 0;
    }
}
