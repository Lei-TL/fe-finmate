package com.finmate.ui.transaction;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.local.database.model.StatisticPoint;
import com.finmate.data.repository.StatisticData;
import com.finmate.data.repository.StatisticRepository;
import com.finmate.data.repository.model.StatisticFilter;
import com.finmate.data.repository.model.StatisticGranularity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatisticViewModel extends ViewModel {

    private final StatisticRepository repository;

    private StatisticFilter currentFilter = StatisticFilter.currentMonth();

    private final MutableLiveData<StatisticResult> _statistic = new MutableLiveData<>();
    public LiveData<StatisticResult> statistic = _statistic;

    private final MutableLiveData<List<WalletEntity>> _wallets = new MutableLiveData<>();
    public LiveData<List<WalletEntity>> wallets = _wallets;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public StatisticViewModel(StatisticRepository repository) {
        this.repository = repository;
    }

    public void loadWallets() {
        repository.loadWallets(_wallets::postValue);
    }

    public void loadStatistics(String type) {
        _isLoading.postValue(true);
        repository.loadStatistic(type, currentFilter, data -> {
            _statistic.postValue(mapToUi(data));
            _isLoading.postValue(false);
        });
    }

    public void applyFilter(String type, StatisticFilter filter) {
        this.currentFilter = filter;
        loadStatistics(type);
    }

    public void applyWallet(String type, @Nullable String walletName) {
        this.currentFilter = currentFilter.withWallet(walletName);
        loadStatistics(type);
    }

    public StatisticFilter getCurrentFilter() {
        return currentFilter;
    }

    private StatisticResult mapToUi(StatisticData data) {
        List<ChartPoint> timeline = mapPoints(data.getTimeline(), currentFilter.getGranularity());
        List<ChartPoint> byCategory = mapPoints(data.getByCategory(), StatisticGranularity.DAY);
        List<ChartPoint> byWallet = mapPoints(data.getByWallet(), StatisticGranularity.DAY);

        return new StatisticResult(
                data.getTotalAmount(),
                timeline,
                byCategory,
                byWallet,
                currentFilter.getDisplayLabel(),
                currentFilter.getGranularity().name()
        );
    }

    private List<ChartPoint> mapPoints(List<StatisticPoint> points, StatisticGranularity granularity) {
        List<ChartPoint> result = new ArrayList<>();
        if (points == null) {
            return result;
        }
        for (StatisticPoint p : points) {
            String label = formatLabel(p.label, granularity);
            result.add(new ChartPoint(label, p.total));
        }
        return result;
    }

    private String formatLabel(String raw, StatisticGranularity granularity) {
        if (raw == null) return "";
        if (granularity == StatisticGranularity.WEEK) {
            String[] parts = raw.split("-");
            if (parts.length == 2) {
                return "Tuần " + parts[1] + "/" + parts[0];
            }
        } else if (granularity == StatisticGranularity.MONTH) {
            String[] parts = raw.split("-");
            if (parts.length == 2) {
                return parts[1] + "/" + parts[0];
            }
        } else {
            String[] parts = raw.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1];
            }
        }
        return raw;
    }
}

