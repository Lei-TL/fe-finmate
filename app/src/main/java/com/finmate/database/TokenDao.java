package com.finmate.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface TokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertToken(TokenEntity token);

    @Query("SELECT * FROM tokens LIMIT 1")
    TokenEntity getToken();

    @Query("DELETE FROM tokens")
    void clearTokens();
}
