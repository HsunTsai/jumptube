package com.turtle.hsun.jumptube.Custom.Components;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Vibrator;
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
import com.turtle.hsun.jumptube.Utils.LogUtil;

import static com.turtle.hsun.jumptube.PlayerService.OVER_LAPPING_HEIGHT;

public class CustomImageHeader implements View.OnTouchListener {

    //Components
    private LinearLayout windows_head, windows_close;
    private RelativeLayout windows_player, layout_close;
    private ImageHeaderActionListener listener;
    private WindowManager windowManager;
    private ImageView img_close;

    //Parameters
    private PlayerService playerService;
    private Vibrator vibrator;
    private int initialX, initialY, closeMinX, closeMinY, closeMaxX, scrnWidth, scrnHeight,
            playerWidth, playerAsideRatio, playerHeadSize, playerHeight, closeImageLayoutSize,
            playerHeadCenterX, playerHeadCenterY;
    private float initialTouchX, initialTouchY, finalTouchX, finalTouchY;
    private Boolean closeShow = false, isInsideClosePre = false, isEntireWidth = false,
            isPlayerVisible = true, isNeedShowUp = false;

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
        this.vibrator = (Vibrator) playerService.getApplication().getSystemService(Service.VIBRATOR_SERVICE);

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

    public CustomImageHeader setPlayerHeadSize(Integer playerHeadSize) {
        this.playerHeadSize = playerHeadSize;
        return this;
    }

    public interface ImageHeaderActionListener {
        public void onPlayerShow(Boolean needShowUp);

        public void onPlayerHide();

        public void onScreenChange(int scrnHeight, int scrnWidth);
    }

    public void setOnActionListener(ImageHeaderActionListener listener) {
        this.listener = listener;
    }

    public void setEntireWidth(Boolean isEntireWidth) {
        this.isEntireWidth = isEntireWidth;
    }

    public CustomImageHeader setPlayerVisible(Boolean isPlayerVisible) {
        this.isPlayerVisible = isPlayerVisible;
        return this;
    }

    public void setPlayerSize(Integer playerWidth, Integer playerHeight) {
        this.playerWidth = playerWidth;
        this.playerHeight = playerHeight;
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
                    if (this.isPlayerVisible) {
                        this.listener.onPlayerHide();
                        this.isNeedShowUp = false;
                    } else {
                        this.listener.onPlayerShow(this.isNeedShowUp);
                    }
                    this.isPlayerVisible = !this.isPlayerVisible;
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
                        //貼齊左邊
                        param_player.x = 0;
                        params.x = 0;
                    } else if (playerWidth + newX > scrnWidth) {
                        //限制不超出右邊
                        param_player.x = scrnWidth - playerWidth;
                        params.x = scrnWidth - playerWidth;
                    } else {
                        //自由移動
                        param_player.x = newX;
                        params.x = newX;
                    }

                    if (newY < 0) {
                        //貼齊最上
                        param_player.y = playerHeadSize - OVER_LAPPING_HEIGHT;
                        params.y = 0;
                    } else if (playerHeight + newY + playerHeadSize - 80 > scrnHeight) {
                        params.y = newY;
                        if (this.isPlayerVisible) {
                            this.isPlayerVisible = false;
                            this.isNeedShowUp = true;
                            this.listener.onPlayerHide();
                        }
                    } else {
                        //自由移動
                        param_player.y = newY + playerHeadSize - OVER_LAPPING_HEIGHT;
                        params.y = newY;
                    }
                    windowManager.updateViewLayout(windows_head, params);
                    if (this.isPlayerVisible)
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
                        int[] imgClosePosition = new int[2];
                        img_close.getLocationOnScreen(imgClosePosition);
                        params.x = imgClosePosition[0];
                        params.y = imgClosePosition[1] - getStatusBarHeight() + 3;
                        params.width = (int) (img_close.getMeasuredWidth() * 1.4);
                        params.height = (int) (img_close.getMeasuredHeight() * 1.4f);
                        if (isInsideClose != isInsideClosePre) {
                            vibrator.vibrate(50);
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

    public void changeScreenDirection(Configuration configuration) {
        int scrnWidthTemp = scrnWidth, scrnHeightTemp = scrnHeight;
        switch (configuration.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                if (scrnWidth < scrnHeight) {
                    scrnWidth = scrnHeightTemp;
                    scrnHeight = scrnWidthTemp;
                }
                break;

            case Configuration.ORIENTATION_PORTRAIT:
                if (scrnWidth > scrnHeight) {
                    scrnWidth = scrnHeightTemp;
                    scrnHeight = scrnWidthTemp;
                }
                break;
        }
        listener.onScreenChange(scrnHeight, scrnWidth);
        LogUtil.show("Screen Width => ", String.valueOf(scrnWidth));
        LogUtil.show("Screen Height => ", String.valueOf(scrnHeight));
    }
}
