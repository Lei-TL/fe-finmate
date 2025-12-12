package com.finmate.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.entity.PendingSyncEntity;

import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

/**
 * Worker để sync pending changes lên server trong background
 */
@HiltWorker
public class SyncWorker extends Worker {

    @AssistedInject
    public SyncWorker(@Assisted @NonNull Context context,
                     @Assisted @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    @NonNull
    public Result doWork() {
        // TODO: Implement sync logic
        // 1. Lấy tất cả pending sync
        // 2. Với mỗi pending sync, gọi API tương ứng
        // 3. Nếu thành công, xóa pending sync
        // 4. Nếu thất bại, tăng retry count và retry sau
        
        // Tạm thời return success
        return Result.success();
    }
}

