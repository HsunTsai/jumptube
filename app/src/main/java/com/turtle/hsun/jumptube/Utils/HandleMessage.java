package com.turtle.hsun.jumptube.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by hsun on 2017/6/13.
 */

public class HandleMessage {

    public static void set(Handler handler, String title) {
        Message register_success = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        register_success.setData(bundle);
        handler.sendMessage(register_success);
    }

    public static void set(Handler handler, String title, String message) {
        Message register_success = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        if (null != message) {
            bundle.putString("message", message);
        }
        register_success.setData(bundle);
        handler.sendMessage(register_success);
    }

    public static void set(Handler handler, String title, Bundle bundle) {
        Message register_success = new Message();
        if (null == bundle) {
            bundle = new Bundle();
        }
        bundle.putString("title", title);
        register_success.setData(bundle);
        handler.sendMessage(register_success);
    }
}
