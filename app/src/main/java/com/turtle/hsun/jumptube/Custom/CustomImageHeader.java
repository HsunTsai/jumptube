package com.turtle.hsun.jumptube.Custom;

import android.content.Intent;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.HandleMessage;

public class CustomImageHeader implements View.OnTouchListener {

    private PlayerService playerService;
    private LinearLayout windows_head, windows_close;
    private RelativeLayout windows_player, layout_close;
    private ImageHeaderActionListener listener;
    private WindowManager windowManager;
    private ImageView img_close;

    private int initialX, initialY, closeMinX, closeMinY, closeMaxX, scrnWidth, scrnHeight,
            playerWidth, playerAsideRatio, playerHeadSize, playerHeight, closeImageLayoutSize,
            playerHeadCenterX, playerHeadCenterY;
    private float initialTouchX, initialTouchY, finalTouchX, finalTouchY;
    private Boolean closeShow = false, isInsideClosePre = false, isEntireWidth = false, isPlayerVisible = true;

    public CustomImageHeader(PlayerService playerService, WindowManager windowManager,
                             LinearLayout windows_head_, RelativeLayout windows_player_,
                             LinearLayout windows_close,
                             Integer scrnWidth, Integer scrnHeight,
                             Integer playerAsideRatio) {
        this.playerService = playerService;
        this.windowManager = windowManager;
        this.windows_head = windows_head_;
        this.windows_player = windows_player_;
        this.windows_close = windows_close;
        this.scrnWidth = scrnWidth;
        this.scrnHeight = scrnHeight;
        this.playerAsideRatio = playerAsideRatio;
        this.layout_close = (RelativeLayout) windows_close.findViewById(R.id.layout_close);

        ViewTreeObserver vto = windows_head.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                windows_head.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerHeadSize = windows_head.getMeasuredHeight();
            }
        });

        vto = windows_player.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                windows_player.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerHeight = windows_player.getMeasuredHeight();
            }
        });

        vto = layout_close.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout_close.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                closeImageLayoutSize = layout_close.getMeasuredHeight();
            }
        });


        this.img_close = (ImageView) windows_close.findViewById(R.id.img_close);
    }

    public interface ImageHeaderActionListener {
        public void onPlayerVisible(Boolean visible);
    }

    public void setOnActionListener(ImageHeaderActionListener listener) {
        this.listener = listener;
    }

    public void setEntireWidth(Boolean isEntireWidth) {
        this.isEntireWidth = isEntireWidth;
    }

    public void setPlayerVisible(Boolean isPlayerVisible) {
        this.isPlayerVisible = isPlayerVisible;
    }

    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        if (isEntireWidth) {
            playerWidth = scrnWidth;
        } else {
            playerWidth = windows_player.getMeasuredWidth();
        }
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) windows_head.getLayoutParams();
        WindowManager.LayoutParams param_player = (WindowManager.LayoutParams) windows_player.getLayoutParams();
        final Handler handleLongTouch = new Handler();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                if (!closeShow) {
                    closeShow = true;
                    HandleMessage.set(playerService.handler, "setCloseShow");
                }
                return true;
            case MotionEvent.ACTION_UP:
                finalTouchX = event.getRawX();
                finalTouchY = event.getRawY();
                handleLongTouch.removeCallbacksAndMessages(null);
                if (closeShow) {
                    closeShow = false;
                    HandleMessage.set(playerService.handler, "setCloseDismiss");
                }
                if (isClicked(initialTouchX, finalTouchX, initialTouchY, finalTouchY)) {
                    //change player isVisible
                    changePlayerVisible();
                } else {
                    //stop if inside the close Button
                    if (isInsideClosePre) {
                        playerService.stopForeground(true);
                        playerService.stopSelf();
                        playerService.stopService(new Intent(playerService, PlayerService.class));
                    } else if (!isPlayerVisible) {
                        //靠邊站
                        if (params.x > scrnWidth / 2) {
                            params.x = scrnWidth - playerHeadSize + playerHeadSize / playerAsideRatio;
                        } else {
                            params.x = -playerHeadSize / playerAsideRatio;
                        }
                        windowManager.updateViewLayout(windows_head, params);
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                int newX, newY;
                newX = initialX + (int) (event.getRawX() - initialTouchX);
                newY = initialY + (int) (event.getRawY() - initialTouchY);
                if (isPlayerVisible) {
                    if (newX < 0) {
                        param_player.x = 0;
                        params.x = 0;
                    } else if (playerWidth + newX > scrnWidth) {
                        param_player.x = scrnWidth - playerWidth;
                        params.x = scrnWidth - playerWidth;
                    } else {
                        param_player.x = newX;
                        params.x = newX;
                    }

                    if (newY < 0) {
                        param_player.y = playerHeadSize;
                        params.y = 0;
                    } else if (playerHeight + newY + playerHeadSize > scrnHeight) {
                        //change player isVisible (Hide Player)
                        changePlayerVisible();
                        params.y = newY;
                    } else {
                        param_player.y = newY + playerHeadSize;
                        params.y = newY;
                    }
                    windowManager.updateViewLayout(windows_head, params);
                    windowManager.updateViewLayout(windows_player, param_player);
                } else {
                    if (newY + playerHeadSize > scrnHeight) {
                        params.y = scrnHeight - playerHeadSize;
                    } else {
                        params.y = newY;
                    }
                    params.x = newX;
                    int[] t = new int[2];
                    layout_close.getLocationOnScreen(t);
                    Boolean isInsideClose = updateIsInsideClose(params.x, params.y, t);
                    //判斷是否進入close image內 是的話靠近去 否的話回復原位
                    if (isInsideClose) {
                        params.x = t[0] + 46;
                        params.y = t[1] - getStatusBarHeight() + 50;
                        params.width = closeImageLayoutSize;
                        params.height = closeImageLayoutSize;
                        if (isInsideClose != isInsideClosePre) {
                            img_close.animate().scaleX(1.4f).scaleY(1.4f).setDuration(100).start();
                            isInsideClosePre = isInsideClose;
                        }
                    } else {
                        params.width = playerHeadSize;
                        params.height = playerHeadSize;
                        if (isInsideClose != isInsideClosePre) {
                            img_close.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                            isInsideClosePre = isInsideClose;
                        }
                    }
                    windowManager.updateViewLayout(windows_head, params);
                }
                return true;
        }
        return false;
    }

    private boolean isClicked(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        if (differenceX >= 5 || differenceY >= 5) {
            return false;
        }
        return true;
    }

    private void changePlayerVisible() {
        isPlayerVisible = !isPlayerVisible;
        listener.onPlayerVisible(isPlayerVisible);
    }


    private Boolean updateIsInsideClose(int x, int y, int[] t) {
        playerHeadCenterX = x + playerHeadSize / 2;
        playerHeadCenterY = y + playerHeadSize / 2;
        closeMinX = t[0] - 10;
        closeMinY = t[1] - getStatusBarHeight() - 10;
        closeMaxX = closeMinX + closeImageLayoutSize + 10;
        if (isInsideClose()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInsideClose() {
        if (playerHeadCenterX >= closeMinX && playerHeadCenterX <= closeMaxX) {
            if (playerHeadCenterY >= closeMinY) {
                return true;
            }
        }
        return false;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = (int) Math.ceil(25 * playerService.getApplicationContext().getResources().getDisplayMetrics().density);
        return statusBarHeight;
    }
}
