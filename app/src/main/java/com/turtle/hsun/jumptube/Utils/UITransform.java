package com.turtle.hsun.jumptube.Utils;

import android.app.Activity;

public class UITransform {
    public static int dp2px(Activity activity, float dp) {
        float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dp(Activity activity, float px) {
        float scale = activity.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
