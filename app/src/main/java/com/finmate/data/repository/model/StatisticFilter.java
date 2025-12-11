package com.finmate.data.repository.model;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Represents a local-only filter for statistic queries.
 */
public class StatisticFilter {

    private final String startDateIso;
    private final String endDateIso;
    private final StatisticGranularity granularity;
    @Nullable
    private final String walletName;
    private final String displayLabel;

    public StatisticFilter(String startDateIso,
                           String endDateIso,
                           StatisticGranularity granularity,
                           @Nullable String walletName,
                           String displayLabel) {
        this.startDateIso = startDateIso;
        this.endDateIso = endDateIso;
        this.granularity = granularity;
        this.walletName = walletName;
        this.displayLabel = displayLabel;
    }

    public String getStartDateIso() {
        return startDateIso;
    }

    public String getEndDateIso() {
        return endDateIso;
    }

    public StatisticGranularity getGranularity() {
        return granularity;
    }

    @Nullable
    public String getWalletName() {
        return walletName;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public StatisticFilter withWallet(@Nullable String wallet) {
        return new StatisticFilter(startDateIso, endDateIso, granularity, wallet, displayLabel);
    }

    public StatisticFilter withDisplayLabel(String label) {
        return new StatisticFilter(startDateIso, endDateIso, granularity, walletName, label);
    }

    public static StatisticFilter lastDays(int days) {
        Calendar end = Calendar.getInstance();
        Calendar start = (Calendar) end.clone();
        start.add(Calendar.DAY_OF_YEAR, -Math.max(days - 1, 0));
        StatisticGranularity granularity = pickGranularity(days);
        return new StatisticFilter(
                toIsoStartOfDay(start),
                toIsoEndOfDay(end),
                granularity,
                null,
                days + " ngày gần nhất"
        );
    }

    public static StatisticFilter currentMonth() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        Calendar end = (Calendar) start.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        StatisticGranularity granularity = StatisticGranularity.DAY;
        return new StatisticFilter(
                toIsoStartOfDay(start),
                toIsoEndOfDay(end),
                granularity,
                null,
                "Tháng này"
        );
    }

    public static StatisticFilter yearToDate() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.MONTH, Calendar.JANUARY);
        start.set(Calendar.DAY_OF_MONTH, 1);
        Calendar end = Calendar.getInstance();
        long days = daysBetween(start, end);
        return new StatisticFilter(
                toIsoStartOfDay(start),
                toIsoEndOfDay(end),
                pickGranularity((int) days),
                null,
                "Năm nay"
        );
    }

    public static StatisticFilter custom(Calendar start, Calendar end, String label) {
        Calendar safeStart = (Calendar) start.clone();
        Calendar safeEnd = (Calendar) end.clone();
        if (safeEnd.before(safeStart)) {
            Calendar tmp = (Calendar) safeStart.clone();
            safeStart = safeEnd;
            safeEnd = tmp;
        }
        long days = daysBetween(safeStart, safeEnd);
        return new StatisticFilter(
                toIsoStartOfDay(safeStart),
                toIsoEndOfDay(safeEnd),
                pickGranularity((int) days),
                null,
                label
        );
    }

    private static String toIsoStartOfDay(Calendar calendar) {
        Calendar clone = (Calendar) calendar.clone();
        clone.set(Calendar.HOUR_OF_DAY, 0);
        clone.set(Calendar.MINUTE, 0);
        clone.set(Calendar.SECOND, 0);
        clone.set(Calendar.MILLISECOND, 0);
        return iso().format(clone.getTime());
    }

    private static String toIsoEndOfDay(Calendar calendar) {
        Calendar clone = (Calendar) calendar.clone();
        clone.set(Calendar.HOUR_OF_DAY, 23);
        clone.set(Calendar.MINUTE, 59);
        clone.set(Calendar.SECOND, 59);
        clone.set(Calendar.MILLISECOND, 999);
        return iso().format(clone.getTime());
    }

    private static StatisticGranularity pickGranularity(int days) {
        if (days <= 14) {
            return StatisticGranularity.DAY;
        }
        if (days <= 90) {
            return StatisticGranularity.WEEK;
        }
        return StatisticGranularity.MONTH;
    }

    private static long daysBetween(Calendar start, Calendar end) {
        long diff = end.getTimeInMillis() - start.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.max(diff, 0)) + 1;
    }

    private static SimpleDateFormat iso() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    }
}

