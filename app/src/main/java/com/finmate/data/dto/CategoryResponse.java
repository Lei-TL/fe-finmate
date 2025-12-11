package com.finmate.data.dto;

public class CategoryResponse {

    private String id;
    private String name;
    private String type;
    private String icon;
    private Integer displayOrder;

    public CategoryResponse() {
    }

    public String getId() {
        return id;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
