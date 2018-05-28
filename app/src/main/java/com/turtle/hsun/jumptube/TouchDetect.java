package com.turtle.hsun.jumptube;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

public class TouchDetect implements View.OnTouchListener {
    private double distance_ori = 0;
    private Boolean isScaleAction = false;
    private Float initialTouchX = 0f, initialTouchY = 0f;
    private TouchListener listener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Integer finger_count = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchX = event.getX();
                initialTouchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                isScaleAction = false;
                distance_ori = 0;
                if (isClicked(initialTouchX, event.getX(), initialTouchY, event.getY())) {
                    this.listener.onCick();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (finger_count == 2 && distance_ori == 0) {
                    distance_ori = Math.sqrt(Math.pow(event.getX(0) -
                            event.getX(1), 2) + Math.pow(event.getY(0) - event.getY(1), 2));
                }
                if (distance_ori == 0) break;
                if (finger_count == 1) {
                    return true;
                } else if (finger_count == 2) {
                    float finger1_x = event.getX(0),
                            finger1_y = event.getY(0),
                            finger2_x = event.getX(1),
                            finger2_y = event.getY(1);

                    final double distanceGap = Math.sqrt(Math.pow(finger1_x - finger2_x, 2) + Math.pow(finger1_y - finger2_y, 2)) - distance_ori;
                    if (Math.abs(distanceGap) > 50 && !isScaleAction) {
                        isScaleAction = true;
                        this.listener.onScale(distanceGap > 0);
                    }
                }
                break;
        }
        return true;
    }

    private boolean isClicked(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX >= 5 || differenceY >= 5) {
            return false;
        }
        return true;
    }

    public void setTouchListener(TouchListener listener) {
        this.listener = listener;
    }

    public interface TouchListener {
        public void onCick();
        public void onScale(Boolean isScaleUp);
    }
}

