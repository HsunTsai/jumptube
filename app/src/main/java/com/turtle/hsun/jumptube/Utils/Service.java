package com.turtle.hsun.jumptube.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.turtle.hsun.jumptube.PlayerService;

public class Service {
    public static Boolean isRunning(Activity activity, Class<PlayerService> playerServiceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (playerServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
