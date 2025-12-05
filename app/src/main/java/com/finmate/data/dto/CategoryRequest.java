package com.finmate.data.dto;

public class CategoryRequest {

    private String name;
    private String type; // "INCOME" / "EXPENSE"
    private String icon;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, String type, String icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getIcon() {
        return icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
