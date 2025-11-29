package com.finmate.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;      // tên category: ăn uống, mua sắm...
    public String type;      // income / expense
    public int iconRes;      // icon R.drawable.xxx

    public CategoryEntity(String name, String type, int iconRes) {
        this.name = name;
        this.type = type;
        this.iconRes = iconRes;
    }
}
