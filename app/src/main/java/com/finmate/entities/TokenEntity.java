package com.finmate.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tokens")
public class TokenEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String accessToken;
    public String refreshToken;

    public TokenEntity(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
