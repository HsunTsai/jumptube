package com.turtle.hsun.jumptube.Utils;

import android.graphics.PixelFormat;
import android.view.WindowManager;

public class ServicePlayerLayoutParams {
    public static WindowManager.LayoutParams init(int w, int h) {
        return new WindowManager.LayoutParams(
                w, h,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
    }
}
