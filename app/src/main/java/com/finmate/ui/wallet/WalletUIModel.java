package com.finmate.ui.wallet;

public class WalletUIModel {

    private final int iconRes;   // R.drawable
    private final String name;   // Tên ví
    private final String balance; // Số dư (đã format UI)

    public WalletUIModel(int iconRes, String name, String balance) {
        this.iconRes = iconRes;
        this.name = name;
        this.balance = balance;
    }

    public int getIconRes() { return iconRes; }
    public String getName() { return name; }
    public String getBalance() { return balance; }
}
