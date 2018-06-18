package com.turtle.hsun.jumptube.Utils;

import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public abstract class DoubleClickListener implements View.OnClickListener {

    private static final long DOUBLE_CLICK_TIME_DELTA = 300; //milliseconds
    private long lastClickTime = 0;
    private Timer timer;
    private Boolean isSingleClick = false;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            isSingleClick = false;
            onDoubleClick();
        } else {
            if (null != timer) timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isSingleClick) onSingleClick();
                }
            }, DOUBLE_CLICK_TIME_DELTA);
            isSingleClick = true;
        }
        lastClickTime = clickTime;
    }

    public abstract void onSingleClick();

    public abstract void onDoubleClick();
}