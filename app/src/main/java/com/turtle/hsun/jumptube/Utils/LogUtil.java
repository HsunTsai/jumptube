package com.turtle.hsun.jumptube.Utils;

import android.util.Log;

import com.turtle.hsun.jumptube.Config;

/**
 * Created by hsun on 2017/10/14.
 */

public class LogUtil {
    public static void show(String log_name, String log_message) {
        if (null != log_message && Config.develop_mode)
            Log.e(log_name, log_message);
    }

    public static void show(String log_name, Exception log_exception) {
        if (null != log_exception && Config.develop_mode)
            Log.e(log_name, log_exception.toString());
    }
}
