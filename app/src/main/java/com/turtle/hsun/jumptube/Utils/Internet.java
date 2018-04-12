package com.turtle.hsun.jumptube.Utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet {
    public static Boolean isAvailable(Activity activity) {
        NetworkInfo info = ((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info == null) {
            //LogUtil.show("Network Test", "no internet connection");
            return false;
        } else {
            if (info.isConnected()) {
                //LogUtil.show("Network Test", " internet connection available...");
                return true;
            } else {
                //LogUtil.show("Network Test", " internet connection");
                return true;
            }
        }
    }
}
