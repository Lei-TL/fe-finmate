package com.finmate.data.repository.model;

public class BudgetProgress {
    public final double limit;
    public final double spent;
    public final double percent;

    public BudgetProgress(double limit, double spent) {
        this.limit = limit;
        this.spent = spent;
        this.percent = limit > 0 ? (spent / limit) * 100.0 : 0;
    }
}
