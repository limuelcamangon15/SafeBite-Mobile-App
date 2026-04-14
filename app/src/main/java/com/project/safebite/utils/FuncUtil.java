package com.project.safebite.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FuncUtil {

    public static boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm != null){
            Network network = cm.getActiveNetwork();
            if(network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) return false;

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        }
        return false;
    }

    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < DateUtils.MINUTE_IN_MILLIS) {
            return "Just now";
        } else if (diff < DateUtils.HOUR_IN_MILLIS) {
            return (diff / DateUtils.MINUTE_IN_MILLIS) + "m ago";
        } else if (diff < DateUtils.DAY_IN_MILLIS) {
            return (diff / DateUtils.HOUR_IN_MILLIS) + "h ago";
        } else if (diff < DateUtils.DAY_IN_MILLIS * 7) {
            return (diff / DateUtils.DAY_IN_MILLIS) + "d ago";
        } else {
            // Older than a week, show "23 April"
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
