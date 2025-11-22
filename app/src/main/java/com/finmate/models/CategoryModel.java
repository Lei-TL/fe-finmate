package com.finmate.models;

/**
 * Lớp mô hình (model) cho một Nhóm phân loại.
 * Đây là một đối tượng bất biến (immutable) để đảm bảo an toàn dữ liệu.
 */
public class CategoryModel {
    private final int icon;       // id icon (R.drawable.xxx)
    private final String name;    // tên nhóm phân loại

    public CategoryModel(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
