package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.finmate.data.local.database.entity.FriendEntity;

import java.util.List;

@Dao
public interface FriendDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FriendEntity friend);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FriendEntity> friends);

    @Query("SELECT * FROM friends")
    List<FriendEntity> getAll();

    @Query("SELECT * FROM friends WHERE status = :status")
    List<FriendEntity> getByStatus(String status);

    @Query("SELECT * FROM friends WHERE incoming = :incoming")
    List<FriendEntity> getByIncoming(boolean incoming);

    @Delete
    void delete(FriendEntity friend);

    @Query("DELETE FROM friends")
    void deleteAll();
}

