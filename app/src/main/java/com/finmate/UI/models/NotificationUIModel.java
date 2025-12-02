package com.finmate.UI.models;

public class NotificationUIModel {

    private final int iconRes;
    private final String title;
    private final String message;
    private final String time;

    public NotificationUIModel(int iconRes, String title, String message, String time) {
        this.iconRes = iconRes;
        this.title = title;
        this.message = message;
        this.time = time;
    }

    public int getIconRes() { return iconRes; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
}
