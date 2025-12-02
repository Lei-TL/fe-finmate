package com.finmate.dto.response;

public class CategoryResponse {

    private int id;
    private String name;
    private String iconUrl;  // Icon trả về dạng link (backend thường trả URL)

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
