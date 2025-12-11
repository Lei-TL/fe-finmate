package com.finmate.data.dto;

public class CategoryRequest {

    private String name;
    private String type;
    private String icon;

    public CategoryRequest(String name, String type, String icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getIcon() { return icon; }
}
