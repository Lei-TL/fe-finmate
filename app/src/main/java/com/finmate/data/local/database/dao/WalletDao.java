package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.finmate.entities.WalletEntity;
import java.util.List;

@Dao
public interface WalletDao {

    @Insert
    void insert(WalletEntity wallet);

    @Update
    void update(WalletEntity wallet);

    @Delete
    void delete(WalletEntity wallet);

    @Query("SELECT * FROM wallets ORDER BY id DESC")
    List<WalletEntity> getAll();

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    WalletEntity getById(int id);
}
