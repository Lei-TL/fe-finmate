package com.finmate.data.repository;

import android.content.Context;

import com.finmate.core.offline.PendingAction;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.dao.WalletDao;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.local.database.model.StatisticPoint;
import com.finmate.data.repository.model.StatisticFilter;
import com.finmate.data.repository.model.StatisticGranularity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class StatisticRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final TransactionDao transactionDao;
    private final WalletDao walletDao;

    @Inject
    public StatisticRepository(@ApplicationContext Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        this.transactionDao = database.transactionDao();
        this.walletDao = database.walletDao();
    }

    public void loadWallets(Callback<List<WalletEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(walletDao.getAll()));
    }

    public void loadStatistic(String type, StatisticFilter filter, Callback<StatisticData> callback) {
        EXECUTOR.execute(() -> {
            String start = filter.getStartDateIso();
            String end = filter.getEndDateIso();
            String wallet = filter.getWalletName();
            String ignoredAction = PendingAction.DELETE;

            double total = transactionDao.sumAmount(type, start, end, wallet, ignoredAction);
            List<StatisticPoint> timeline = chooseTimelineQuery(type, filter.getGranularity(), start, end, wallet, ignoredAction);
            List<StatisticPoint> byCategory = transactionDao.sumByCategory(type, start, end, wallet, ignoredAction);
            List<StatisticPoint> byWallet = transactionDao.sumByWallet(type, start, end, wallet, ignoredAction);

            callback.onResult(new StatisticData(total, timeline, byCategory, byWallet));
        });
    }

    private List<StatisticPoint> chooseTimelineQuery(String type,
                                                     StatisticGranularity granularity,
                                                     String start,
                                                     String end,
                                                     String wallet,
                                                     String ignoredAction) {
        if (granularity == StatisticGranularity.WEEK) {
            return transactionDao.sumByWeek(type, start, end, wallet, ignoredAction);
        }
        if (granularity == StatisticGranularity.MONTH) {
            return transactionDao.sumByMonth(type, start, end, wallet, ignoredAction);
        }
        return transactionDao.sumByDay(type, start, end, wallet, ignoredAction);
    }

    public interface Callback<T> {
        void onResult(T data);
    }
}

