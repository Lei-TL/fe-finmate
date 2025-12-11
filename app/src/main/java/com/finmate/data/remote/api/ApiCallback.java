package com.finmate.data.remote.api;

import androidx.annotation.Nullable;

public interface ApiCallback<T> {
    void onSuccess(T data);
    void onError(String message, @Nullable Integer code);
}
