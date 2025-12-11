package com.finmate.data.repository;

import com.finmate.data.local.database.entity.SharedTransactionEntity;
import com.finmate.data.remote.api.ApiCallback;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Stub để ghép API thật sau này.
 */
@Singleton
public class SharedExpenseRemoteRepository {

    @Inject
    public SharedExpenseRemoteRepository() {}

    public void listShared(ApiCallback<List<SharedTransactionEntity>> callback) {
        // TODO: call real API; for now return empty
        callback.onSuccess(new ArrayList<>());
    }

    public void createShared(SharedTransactionEntity entity, ApiCallback<SharedTransactionEntity> callback) {
        // TODO: call real API; for now echo back
        callback.onSuccess(entity);
    }
}

