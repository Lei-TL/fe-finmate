package com.finmate.ui.models;

public class LanguageUIModel {

    private int flagResId;       // R.drawable lá cờ
    private String name;         // "Vietnamese", "English"
    private String code;         // "vi", "en"
    private boolean selected;    // trạng thái đã chọn hay chưa

    public LanguageUIModel(int flagResId, String name, String code, boolean selected) {
        this.flagResId = flagResId;
        this.name = name;
        this.code = code;
        this.selected = selected;
    }

    // Adapter dùng các hàm này ↓↓↓↓

    public int getFlagResId() {
        return flagResId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
