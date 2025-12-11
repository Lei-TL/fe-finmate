package com.finmate.data.local.database.model;

import androidx.room.ColumnInfo;

/**
 * Simple projection model for aggregation queries in Room.
 */
public class StatisticPoint {

    @ColumnInfo(name = "label")
    public String label;

    @ColumnInfo(name = "total")
    public double total;
}

