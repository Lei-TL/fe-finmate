package com.finmate.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;     // Tên category
    private String type;     // income / expense
    private String icon;     // Icon dạng string từ server, ví dụ "ic_food"

    public CategoryEntity(String name, String type, String icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    // Getters
    public int getId() {
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

    // Setters
    public void setId(int id) {
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
}
