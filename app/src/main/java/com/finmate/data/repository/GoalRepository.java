package com.finmate.data.repository;

import android.content.Context;

import com.finmate.core.offline.PendingAction;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.entity.GoalEntity;
import com.finmate.data.repository.model.GoalProgress;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class GoalRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final GoalLocalRepository localRepository;
    private final TransactionDao transactionDao;

    @Inject
    public GoalRepository(@ApplicationContext Context context, GoalLocalRepository localRepository) {
        this.localRepository = localRepository;
        this.transactionDao = AppDatabase.getDatabase(context).transactionDao();
    }

    public void listGoals(Callback<List<GoalEntity>> callback) {
        localRepository.getAll(callback::onResult);
    }

    public void saveGoal(GoalEntity entity) {
        if (entity.id == 0) {
            localRepository.insert(entity);
        } else {
            localRepository.update(entity);
        }
    }

    public void deleteGoal(GoalEntity entity) {
        localRepository.delete(entity);
    }

    /**
        Simple progress: sum of INCOME transactions into linked wallet (if provided) within all time.
        This can be refined once BE rules are available.
     */
    public void calculateProgress(GoalEntity goal, ProgressCallback callback) {
        EXECUTOR.execute(() -> {
            String wallet = goal.linkedWalletName;
            double saved = transactionDao.sumAmount("INCOME", null, null, wallet, PendingAction.DELETE);
            callback.onProgress(new GoalProgress(goal.targetAmount, saved));
        });
    }

    public interface Callback<T> {
        void onResult(T data);
    }

    public interface ProgressCallback {
        void onProgress(GoalProgress progress);
    }
}
