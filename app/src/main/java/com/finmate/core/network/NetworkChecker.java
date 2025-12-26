package com.finmate.core.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class NetworkChecker {

    private final Context context;

    @Inject
    public NetworkChecker(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Kiểm tra xem device có kết nối mạng không
     * @return true nếu có mạng, false nếu không có mạng
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ (API 23+)
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }
            
            NetworkCapabilities capabilities = 
                    connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return false;
            }
            
            return true;
        } else {
            // Android cũ hơn
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
}



