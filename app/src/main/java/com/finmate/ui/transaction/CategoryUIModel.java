package com.finmate.ui.transaction;

// Model này dành riêng cho tầng UI, tuân thủ Clean Architecture
public class CategoryUIModel {
    private int icon;
    private String name;

    public CategoryUIModel(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    // Thêm các getter để Adapter có thể truy cập
    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
