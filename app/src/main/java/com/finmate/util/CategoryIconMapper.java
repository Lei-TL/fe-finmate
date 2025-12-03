package com.finmate.util;

import com.finmate.R;

public class CategoryIconMapper {

    public static int getIconRes(String iconKey) {
        switch (iconKey) {
            case "food": return R.drawable.ic_food;
            case "shopping": return R.drawable.ic_shopping;
            case "health": return R.drawable.ic_health;
            default: return R.drawable.ic_default_category;
        }
    }
}
