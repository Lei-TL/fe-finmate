package com.finmate.util;

import android.content.Context;

import javax.inject.Inject;
import dagger.hilt.android.qualifiers.ApplicationContext;

public class TransactionUtil {

    private final Context context;

    @Inject
    public TransactionUtil(@ApplicationContext Context context) {
        this.context = context;
    }

    // Thêm các phương thức tiện ích của bạn vào đây
}
