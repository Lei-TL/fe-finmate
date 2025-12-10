package com.finmate.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String type;
    public int iconRes;

    public CategoryEntity(String name, String type, int iconRes) {
        this.name = name;
        this.type = type;
        this.iconRes = iconRes;
    }
}
