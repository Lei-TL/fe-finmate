package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncDao;
import com.finmate.data.local.database.entity.BudgetEntity;

import java.util.List;

@Dao
public interface BudgetDao extends SyncDao<BudgetEntity> {

    @Insert
    void insert(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);

    @Delete
    void delete(BudgetEntity budget);

    @Query("SELECT * FROM budgets ORDER BY id DESC")
    List<BudgetEntity> getAll();

    @Query("SELECT * FROM budgets WHERE pendingAction != :noneAction ORDER BY updatedAt ASC")
    List<BudgetEntity> getPendingForSyncInternal(String noneAction);

    @Override
    default List<BudgetEntity> getPendingForSync() {
        return getPendingForSyncInternal(PendingAction.NONE);
    }
}
