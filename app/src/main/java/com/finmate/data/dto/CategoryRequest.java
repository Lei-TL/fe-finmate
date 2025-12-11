package com.finmate.data.dto;

/**
 * DTO matching backend CategoryRequest
 */
public class CategoryRequest {

    private String name;
    private String type; // INCOME / EXPENSE / TRANSFER
    private String parentId; // optional
    private String icon; // optional

    public CategoryRequest() {
    }

    public CategoryRequest(String name, String type, String icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public CategoryRequest(String name, String type, String parentId, String icon) {
        this.name = name;
        this.type = type;
        this.parentId = parentId;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
