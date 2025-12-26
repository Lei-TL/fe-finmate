package com.finmate.ui.transaction;

public class TransactionUIModel {
    public final long localId;
    public final String name;
    public final String category;
    public final String amount;
    public final String wallet;
    public final String date;
    public final String type;

    public TransactionUIModel(long localId, String name, String category, String amount, String wallet, String date, String type) {
        this.localId = localId;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
        this.type = type;
    }
}
