package com.finmate.ui.models;

public class LanguageUIModel {

    private final int flagRes;       // R.drawable (cờ)
    private final String languageName;
    private final String languageCode; // "en", "vi", ...

    public LanguageUIModel(int flagRes, String languageName, String languageCode) {
        this.flagRes = flagRes;
        this.languageName = languageName;
        this.languageCode = languageCode;
    }

    public int getFlagRes() { return flagRes; }
    public String getLanguageName() { return languageName; }
    public String getLanguageCode() { return languageCode; }
}
