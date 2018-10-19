package com.turtle.hsun.jumptube.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.turtle.hsun.jumptube.Custom.Components.CustomSeekbar;
import com.turtle.hsun.jumptube.JavaScript;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.DoubleClickListener;
import com.turtle.hsun.jumptube.Utils.HandleMessage;

import java.util.Timer;
import java.util.TimerTask;

public class FullscreenWebPlayer extends Activity implements View.OnClickListener {

    //Parameter
    public static Handler handler;
    private Boolean isShow = true, isPlaying = true;
    private Timer timer;
    private int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    //Components
    private ViewGroup parent;
    private WebView player;
    private LinearLayout layout_control;
    private ImageButton imgbt_play_pause_video;
    private Button bt_backward, bt_forward;
    public static CustomSeekbar seekBar_player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_web_player);
        Handler();

        getWindow().getDecorView().setSystemUiVisibility(flags);

        layout_control = (LinearLayout) findViewById(R.id.layout_control);
        imgbt_play_pause_video = (ImageButton) findViewById(R.id.imgbt_play_pause_video);
        imgbt_play_pause_video.setOnClickListener(this);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_fullscreen);
        player = PlayerService.webPlayer.getPlayer();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        );
        parent = (ViewGroup) player.getParent();
        parent.removeView(player);
        linearLayout.addView(player, params);

        seekBar_player = (CustomSeekbar) findViewById(R.id.seekBar_player);
        seekBar_player.setOnClickListener(this);
        seekBar_player.setSeekbarListener(new CustomSeekbar.CustomSeekbarListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlayerService.webPlayer.loadScript(JavaScript.seekTo(seekBar.getProgress()));
                HandleMessage.set(handler, "showControl");
            }
        });

        bt_forward = (Button) findViewById(R.id.bt_forward);
        bt_forward.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick() {
                //setVideoPlayPause();
                HandleMessage.set(handler, "showControl");
            }

            @Override
            public void onDoubleClick() {
                HandleMessage.set(PlayerService.handler, "videoForward");
                Toast.makeText(FullscreenWebPlayer.this,
                        getString(R.string.video_forward_10), Toast.LENGTH_SHORT).show();
            }
        });
        bt_backward = (Button) findViewById(R.id.bt_backward);
        bt_backward.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick() {
                //setVideoPlayPause();
                HandleMessage.set(handler, "showControl");
            }

            @Override
            public void onDoubleClick() {
                HandleMessage.set(PlayerService.handler, "videoBackward");
                Toast.makeText(FullscreenWebPlayer.this,
                        getString(R.string.video_backward_10), Toast.LENGTH_SHORT).show();
            }
        });

        PlayerService.webPlayer.loadScript(JavaScript.playVideo());
        HandleMessage.set(handler, "hideControl");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leaveFullScreen();
    }

    private void setVideoPlayPause() {
        if (null == PlayerService.handler) return;
        isPlaying = !isPlaying;
        if (isPlaying) {
            HandleMessage.set(PlayerService.handler, "videoPlay");
        } else {
            HandleMessage.set(PlayerService.handler, "videoPause");
        }
    }

    private void leaveFullScreen() {
        ((ViewGroup) player.getParent()).removeView(player);
        parent.addView(player);
        HandleMessage.set(PlayerService.handler, "startAgain");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbt_play_pause_video:
                if (isShow) setVideoPlayPause();
                HandleMessage.set(handler, "showControl");
                break;
            case R.id.seekBar_player:
                HandleMessage.set(handler, "showControl");
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private void Handler() {
        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void handleMessage(Message msg) {
                switch (msg.getData().getString("title", "")) {
                    case "hideControl":
                        isShow = false;
                        layout_control.animate().alpha(0).setDuration(500).setStartDelay(1000).start();
                        break;
                    case "showControl":
                        isShow = true;
                        layout_control.setAlpha(1);
                        setHideControlTimer();
                        break;
                    case "playStatus_ended":
                        isPlaying = false;
                        imgbt_play_pause_video.setImageResource(R.drawable.play);
                        break;
                    case "playStatus_playing":
                        isPlaying = true;
                        imgbt_play_pause_video.setImageResource(R.drawable.pause);
                        break;
                    case "playStatus_paused":
                        isPlaying = false;
                        imgbt_play_pause_video.setImageResource(R.drawable.play);
                        break;
                    //設定總播放時間
                    case "setDurationTime":
                        Integer durationTime = Integer.parseInt(msg.getData().getString("message", "0"));
                        seekBar_player.setMax(durationTime);
                        break;
                    //設定目前播放進度
                    case "setCurrentTime":
                        Integer currentTime = Integer.parseInt(msg.getData().getString("message", "0"));
                        seekBar_player.setProgress(currentTime);
                        break;

                }
            }
        };
    }

    private void setHideControlTimer() {
        if (null != timer) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isPlaying) HandleMessage.set(handler, "hideControl");
            }
        }, 2000);
    }


//    @Override
//    protected void onPause() {
//        super.onPause();
//        onBackPressed();
//    }

}
