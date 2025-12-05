package com.finmate.core.network;

public interface ApiCallback<T> {
    void onSuccess(T body);
    void onError(String message);
}
