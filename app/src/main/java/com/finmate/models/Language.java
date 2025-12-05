package com.finmate.models;

public class Language {
    private String name;
    private String code;
    private int flagResId;
    private boolean isSelected;

    public Language(String name, String code, int flagResId) {
        this.name = name;
        this.code = code;
        this.flagResId = flagResId;
        this.isSelected = false;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getFlagResId() {
        return flagResId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
