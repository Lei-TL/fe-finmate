package com.finmate.data.repository;

import android.content.Context;

import com.finmate.core.offline.PendingAction;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.entity.BudgetEntity;
import com.finmate.data.repository.model.BudgetProgress;
import com.finmate.data.repository.model.StatisticFilter;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class BudgetRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final BudgetLocalRepository localRepository;
    private final TransactionDao transactionDao;

    @Inject
    public BudgetRepository(@ApplicationContext Context context, BudgetLocalRepository localRepository) {
        this.localRepository = localRepository;
        this.transactionDao = AppDatabase.getDatabase(context).transactionDao();
    }

    public void listBudgets(Callback<List<BudgetEntity>> callback) {
        localRepository.getAll(callback::onResult);
    }

    public void saveBudget(BudgetEntity entity) {
        if (entity.id == 0) {
            localRepository.insert(entity);
        } else {
            localRepository.update(entity);
        }
    }

    public void deleteBudget(BudgetEntity entity) {
        localRepository.delete(entity);
    }

    public void calculateProgress(BudgetEntity budget, StatisticFilter filter, ProgressCallback callback) {
        EXECUTOR.execute(() -> {
            String start = filter.getStartDateIso();
            String end = filter.getEndDateIso();
            String wallet = budget.walletName;
            String category = budget.categoryName;
            double spent = transactionDao.sumAmountForBudget("EXPENSE", start, end, wallet, category, PendingAction.DELETE);
            callback.onProgress(new BudgetProgress(budget.amountLimit, spent));
        });
    }

    public interface Callback<T> {
        void onResult(T data);
    }

    public interface ProgressCallback {
        void onProgress(BudgetProgress progress);
    }
}
