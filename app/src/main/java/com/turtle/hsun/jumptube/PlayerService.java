package com.turtle.hsun.jumptube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.android.volley.toolbox.Volley;
import com.turtle.hsun.jumptube.Activity.FullscreenWebPlayer;
import com.turtle.hsun.jumptube.Config.API;
import com.turtle.hsun.jumptube.Config.Config;
import com.turtle.hsun.jumptube.Config.ConstantStrings;
import com.turtle.hsun.jumptube.Custom.Components.CustomImageHeader;
import com.turtle.hsun.jumptube.Custom.Components.WebPlayer;
import com.turtle.hsun.jumptube.Custom.Components.CustomNotificationManager;
import com.turtle.hsun.jumptube.Custom.Components.CustomSeekbar;
import com.turtle.hsun.jumptube.Utils.HandleMessage;
import com.turtle.hsun.jumptube.Utils.LogUtil;
import com.turtle.hsun.jumptube.Utils.ServicePlayerLayoutParams;

import static com.turtle.hsun.jumptube.Config.Config.sharedPreferences;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hsun on 18/4/12.
 */
public class PlayerService extends Service implements View.OnClickListener {

    //UI Component
    public static WebPlayer webPlayer;
    private Button bt_pause_play;
    private ImageView img_head_icon, img_youtube, img_close, img_repeat_type, img_entire_width, img_fullscreen; //player
    private CustomSeekbar seekBar_player;
    private RelativeLayout windows_player, layout_webView;
    private LinearLayout layout_player, windows_close, windows_close_background, windows_head;

    //Parameter
    private Context context;
    private WindowManager windowManager;
    public static Handler handler;
    private String videoID, playListID;
    private Boolean isVideoPlaying = true, isReplay = false,
            isLastPlayItem = false, isEntireWidth = false, isVideoShow = true; //Next Video to check whether ic_next video is played or not
    private WindowManager.LayoutParams windowsHeadParams, windowsCloseParams,
            windowsCloseBackParams, componentPlayerParams,
            param_player, param_service, param_close, param_close_back;
    private CustomNotificationManager customNotificationManager;
    private CustomImageHeader customImageHeader;
    private Timer updateCurrentTimeTimer;

    private int scrnWidth, scrnHeight, defaultPlayerWidth, defaultPlayerHeight, playerHeadSize, xAtHiding, yAtHiding,
            xOnAppear, yOnAppear = 0, playerAsideRatio = 7, currentTime = 0;
    public static int OVER_LAPPING_HEIGHT = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    private void isPlaylistEnded() {
        webPlayer.loadScript(JavaScript.isPlaylistEnded());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Handler();

        switch (intent.getAction()) {
            case Config.ACTION.STARTFOREGROUND_WEB_ACTION:
                //Service 啟動
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Config.repeatType = sharedPreferences.getInt(getString(R.string.repeat_type), 0);
                initialPlay(intent);
                isLastPlayItem = false;
                break;

            case Config.ACTION.STOPFOREGROUND_WEB_ACTION:
                //Service 關閉
                stopForeground(true);
                stopSelf();
                stopService(new Intent(this, PlayerService.class));
                break;

            case Config.ACTION.PAUSE_PLAY_ACTION:
                //Service 播放
                pausePlay();
                break;

            case Config.ACTION.NEXT_ACTION:
                //Service 下一首
                if (Config.linkType == 0) {
                    webPlayer.loadScript(JavaScript.seekToZero());
                } else {
                    webPlayer.loadScript(JavaScript.playNextVideo());
                }
                break;

            case Config.ACTION.PREV_ACTION:
                //Service 上一首
                if (Config.linkType == 0) {
                    webPlayer.loadScript(JavaScript.seekToZero());
                } else {
                    webPlayer.loadScript(JavaScript.playPreviousVideo());
                }
                break;
        }

        return START_NOT_STICKY;
    }

    //初始化所有整個播放器的畫面
    @SuppressLint("ClickableViewAccessibility")
    private void initialPlay(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            videoID = bundle.getString("VIDEO_ID");
            playListID = bundle.getString("PLAYLIST_ID");
        }
        //初始化參數
        initParams();
        //初始化畫面
        initViews();
        //初始化通知
        initNotification();
    }

    //Layout Params Initialized
    private void initParams() {
        //getting Screen Width and Height
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrnWidth = size.x;
        scrnHeight = size.y;

        //Service Head Params
        playerHeadSize = sharedPreferences.getInt("stickerSize", 150);
        param_service = ServicePlayerLayoutParams.init(playerHeadSize, playerHeadSize);

        //Player View Params
        Double playerHeight = new Double(scrnWidth / 1.49);
        param_player = ServicePlayerLayoutParams.init(scrnWidth, playerHeight.intValue());

        //Close Backgroung Params
        param_close_back = ServicePlayerLayoutParams.init(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        //Close Image Params
        param_close = ServicePlayerLayoutParams.init(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //大頭貼 畫面
        LayoutInflater inflater = (LayoutInflater) this.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        windows_head = (LinearLayout) inflater.inflate(R.layout.windows_head, null, false);
        img_head_icon = (ImageView) windows_head.findViewById(R.id.img_head_icon);

        param_service.gravity = Gravity.TOP | Gravity.LEFT;
        param_service.x = 0;
        param_service.y = 0;
        windowManager.addView(windows_head, param_service);

        //播放器 畫面
        windows_player = (RelativeLayout) inflater.inflate(R.layout.windows_player, null, false);
        //Player components
        bt_pause_play = (Button) windows_player.findViewById(R.id.bt_pause_play);
        bt_pause_play.setOnClickListener(this);
        TouchDetect touchDetect = new TouchDetect();
        touchDetect.setTouchListener(new TouchDetect.TouchListener() {
            @Override
            public void onCick() {
                pausePlay();
            }

            @Override
            public void onScale(Boolean isScaleUp) {
                scaleWindow(isScaleUp);
            }
        });
        bt_pause_play.setOnTouchListener(touchDetect);
        img_youtube = (ImageView) windows_player.findViewById(R.id.img_youtube);
        seekBar_player = (CustomSeekbar) windows_player.findViewById(R.id.seekBar_player);
        seekBar_player.setSeekbarListener(new CustomSeekbar.CustomSeekbarListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                webPlayer.loadScript(JavaScript.seekTo(seekBar.getProgress()));
            }
        });
        //Player Layouts
        layout_webView = (RelativeLayout) windows_player.findViewById(R.id.layout_webView);
        layout_player = (LinearLayout) windows_player.findViewById(R.id.layout_player);
        //Player Controls
        img_repeat_type = (ImageView) windows_player.findViewById(R.id.img_repeat_type);
        img_entire_width = (ImageView) windows_player.findViewById(R.id.img_entire_width);
        img_fullscreen = (ImageView) windows_player.findViewById(R.id.img_fullscreen);
        img_repeat_type.setOnClickListener(this);
        img_entire_width.setOnClickListener(this);
        img_fullscreen.setOnClickListener(this);
        updateRepeatTypeImage(); //變更重播次數的icon

        webPlayer = new WebPlayer(this);
        webPlayer.setupPlayer();
        layout_webView.addView(webPlayer.getPlayer(), new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
        //------------------------------Got Player Id--------------------------------------------------------
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                if (Config.linkType == 1) {
                    LogUtil.show("Service Start => ", "Playlist");
                    webPlayer.loadDataWithUrl("https://www.youtube.com/player_api", ConstantStrings.getPlayListHTML(playListID),
                            "text/html", null, null);

                } else {
                    LogUtil.show("Service Start => ", "Single Video");
                    webPlayer.loadDataWithUrl("https://www.youtube.com/player_api", ConstantStrings.getVideoHTML(videoID),
                            "text/html", null, null);
                }
            }
        });

        param_player.gravity = Gravity.TOP | Gravity.LEFT;
        param_player.x = 0;
        //param_player.y = playerHeadSize - OVER_LAPPING_HEIGHT;
        windowManager.addView(windows_player, param_player);

        //大頭貼 畫面參數監聽註冊
        ViewTreeObserver vto = windows_head.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                windows_head.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                playerHeadSize = windows_head.getMeasuredHeight();
                param_player.y = playerHeadSize - OVER_LAPPING_HEIGHT;
                xOnAppear = -playerHeadSize / playerAsideRatio;
                windowManager.updateViewLayout(windows_player, param_player);
            }
        });

        //播放器 畫面參數監聽註冊
        vto = windows_player.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                windows_player.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                defaultPlayerWidth = windows_player.getMeasuredWidth();
                defaultPlayerHeight = windows_player.getMeasuredHeight();
                param_player.width = defaultPlayerWidth;
                param_player.height = defaultPlayerHeight;

                //scale popup window to last time save
                scaleWindow(null);
            }
        });


        //關閉背景 畫面
        windows_close_background = (LinearLayout) inflater.inflate(R.layout.windows_close_background, null, false);
        param_close_back.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        windows_close_background.setVisibility(View.GONE);
        windowManager.addView(windows_close_background, param_close_back);

        //關閉icon 畫面
        windows_close = (LinearLayout) inflater.inflate(R.layout.windows_close, null, false);
        param_close.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        windows_close.setVisibility(View.GONE);
        windowManager.addView(windows_close, param_close);
        img_close = (ImageView) windows_close.findViewById(R.id.img_close);

        customImageHeader = new CustomImageHeader(this, windowManager, windows_head, windows_player, windows_close, scrnWidth, scrnHeight, playerAsideRatio);
        customImageHeader.setOnActionListener(new CustomImageHeader.ImageHeaderActionListener() {
            @Override
            public void onPlayerHide() {
                hidePlayer(true);
            }

            @Override
            public void onPlayerShow(Boolean needShowUp) {
                showPlayer(needShowUp);
            }

            @Override
            public void onScreenChange(int scrnHeight_, int scrnWidth_) {
                scrnHeight = scrnHeight_;
                scrnWidth = scrnWidth_;
            }
        });


        img_head_icon.setOnTouchListener(customImageHeader);

    }

    private void initNotification() {
        //Notification Init
        customNotificationManager = new CustomNotificationManager(context, videoID);
        customNotificationManager.setOnFinish(new CustomNotificationManager.NotificationListener() {
            @Override
            public void onFinish(Notification notification) {
                startForeground(Config.NOTIFICATION_ID, notification);
            }

            @Override
            public void onBitmapChange(Bitmap bitmap) {
                img_head_icon.setImageBitmap(bitmap);
            }
        });
    }

    private void scaleWindow(Boolean isScaleUp) {
        //when isScaleUp == null view update scale last time (sharedphference)
        int partScrnWidth = scrnWidth / 6, newWindowsScaleType;
        float paramPlayerProportion = (float) defaultPlayerHeight / (float) defaultPlayerWidth;
        if (null == isScaleUp) {
            newWindowsScaleType = Config.windowsScaleType;
        } else if (isScaleUp) {
            newWindowsScaleType = Config.windowsScaleType + 1;
        } else {
            newWindowsScaleType = Config.windowsScaleType - 1;
        }
        if (newWindowsScaleType >= 3 && newWindowsScaleType <= 6) {
            //only accept type from 3 to 6
            //....
            HandleMessage.set(PlayerService.handler, "setWindowsScaleType", String.valueOf(newWindowsScaleType));
        }
    }

    //Update Image of Repeat Type Button
    private void updateRepeatTypeImage() {
        if (Config.repeatType == 0) {
            img_repeat_type.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_none));
        } else if (Config.repeatType == 1) {
            img_repeat_type.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat));
        } else if (Config.repeatType == 2) {
            img_repeat_type.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one));
        }
    }


    private void pausePlay() {
        webPlayer.loadScript(JavaScript.getPlaybackQuality());
        if (isVideoPlaying) {
            if (isReplay) {
                if (Config.linkType == 1) {
                    LogUtil.show("PlayStatus", "Replay Playlist");
                    webPlayer.loadScript(JavaScript.replayPlayList());
                } else {
                    LogUtil.show("PlayStatus", "Replay Video");
                    HandleMessage.set(handler, "videoPlay");
                }
                isReplay = false;
            } else {
                LogUtil.show("PlayStatus", "Pause Video");
                HandleMessage.set(handler, "videoPause");
            }
        } else {
            LogUtil.show("PlayStatus", "Play Video");
            HandleMessage.set(handler, "videoPlay");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_pause_play:
                pausePlay();
                break;
            //Handle Full Screen
            case R.id.img_fullscreen:
                HandleMessage.set(handler, "videoPause");
                Intent fullScreenIntent = new Intent(context, FullscreenWebPlayer.class);
                fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //remove Views
                windowManager.removeView(windows_head);
                windowsHeadParams = (WindowManager.LayoutParams) windows_head.getLayoutParams();
                windowManager.removeView(windows_close);
                windowsCloseParams = (WindowManager.LayoutParams) windows_close.getLayoutParams();
                windowManager.removeView(windows_close_background);
                windowsCloseBackParams = (WindowManager.LayoutParams) windows_close_background.getLayoutParams();
                windowManager.removeView(windows_player);
                componentPlayerParams = (WindowManager.LayoutParams) windows_player.getLayoutParams();
                //start full Screen Player
                context.startActivity(fullScreenIntent);
                break;
            //Handle Entire Width
            case R.id.img_entire_width:
                if (isEntireWidth) {
                    //Exit Entire Width
                    param_player.width = defaultPlayerWidth;
                    windowManager.updateViewLayout(windows_player, param_player);
                    img_entire_width.setImageDrawable(getResources().getDrawable(R.drawable.ic_entire_width));
                    customImageHeader.setEntireWidth(false);
                } else {
                    //Enter Entire Width
                    param_player.width = WindowManager.LayoutParams.MATCH_PARENT;
                    windowManager.updateViewLayout(windows_player, param_player);
                    img_entire_width.setImageDrawable(getResources().getDrawable(R.drawable.ic_entire_width_exit));
                    customImageHeader.setEntireWidth(true);
                }
                isEntireWidth = !isEntireWidth;
                break;
            //Handle Repeat Settings
            case R.id.img_repeat_type:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (Config.repeatType == 0) {
                    Config.repeatType = 1;
                    if (Config.linkType == 1) {
                        webPlayer.loadScript(JavaScript.setLoopPlaylist());
                    }
                } else {
                    Config.repeatType = 0;
                    if (Config.linkType == 1) {
                        webPlayer.loadScript(JavaScript.unsetLoopPlaylist());
                    }
                }
                editor.putInt(getString(R.string.repeat_type), Config.repeatType).apply();
                updateRepeatTypeImage();
                break;
            default:
                break;
        }
    }

    private void showPlayer(Boolean needShowUp) {
        layout_player.setVisibility(View.VISIBLE);
        //Store current to again hidden icon will come here
        if (param_service.x > 0) {
            xOnAppear = scrnWidth - playerHeadSize + playerHeadSize / playerAsideRatio;
        } else {
            xOnAppear = -playerHeadSize / playerAsideRatio;
        }
        yOnAppear = param_service.y;
        //Update the icon and player to player's hidden position
        param_service.x = xAtHiding;
        param_player.x = xAtHiding;
        if (needShowUp) {
            param_service.y = yAtHiding - 80;
            param_player.y = yAtHiding + playerHeadSize - 80 - OVER_LAPPING_HEIGHT;
        } else {
            param_service.y = yAtHiding;
            param_player.y = yAtHiding + playerHeadSize - OVER_LAPPING_HEIGHT;
        }
        windowManager.updateViewLayout(windows_player, param_player);
        windowManager.updateViewLayout(windows_head, param_service);
        isVideoShow = true;
    }

    private void hidePlayer(Boolean updateHead) {
        xAtHiding = param_service.x;
        yAtHiding = param_service.y;
        //To hide the Player View
        WindowManager.LayoutParams playerParams = ServicePlayerLayoutParams.init(0, 0);
        playerParams.x = scrnWidth;
        playerParams.y = scrnHeight;
        layout_player.setVisibility(View.GONE);
        windowManager.updateViewLayout(windows_player, playerParams);
        if (updateHead) {
            param_service.x = xOnAppear;
            param_service.y = yOnAppear;
            windowManager.updateViewLayout(windows_head, param_service);
        }
        isVideoShow = false;
    }

    @SuppressLint("HandlerLeak")
    private void Handler() {
        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void handleMessage(Message msg) {
                switch (msg.getData().getString("title", "")) {
                    case "setCloseShow":
                        windows_close_background.setVisibility(View.VISIBLE);
                        windows_close.setVisibility(View.VISIBLE);
                        windows_close.animate().alpha(1f).setDuration(100).start();
                        img_close.animate().translationY(100).start();
                        img_close.animate().translationY(0).setDuration(100).start();
                        break;
                    case "setCloseDismiss":
                        windows_close_background.setVisibility(View.GONE);
                        windows_close.animate().alpha(0).setDuration(100).start();
                        img_close.animate().translationY(100).setDuration(100).start();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                HandleMessage.set(handler, "setCloseWindowGONE");
                            }
                        }, 200);
                        break;
                    case "setCloseWindowGONE":
                        windows_close.setVisibility(View.GONE);
                        break;
                    case "startAgain":
                        //Play video again on exit full screen
                        windowManager.addView(windows_head, windowsHeadParams);
                        windowManager.addView(windows_close, windowsCloseParams);
                        windowManager.addView(windows_close_background, windowsCloseBackParams);
                        windowManager.addView(windows_player, componentPlayerParams);
                        HandleMessage.set(handler, "videoPlay");
                        break;
                    case "startVideo":
                        String video_ID = msg.getData().getString("VIDEO_ID"),
                                playListID = msg.getData().getString("PLAYLIST_ID");
                        currentTime = 0;
                        if (playListID == null) {
                            LogUtil.show("start video", video_ID);
                            customNotificationManager.setAuthor(videoID);
                            webPlayer.loadScript(JavaScript.loadVideoScript(video_ID));
                        } else {
                            LogUtil.show("start playlist", playListID);
                            webPlayer.loadScript(JavaScript.loadPlaylistScript(playListID));
                            customNotificationManager.setAuthor(videoID);
                        }
                        break;
                    case "addStateChangeListener":
                        webPlayer.loadScript(JavaScript.onPlayerStateChangeListener());
                        break;
                    case "setImageTitleAuthor":
                        String videoID = msg.getData().getString("message", "");
                        customNotificationManager.setAuthor(videoID);
                        API.USER_LOG_KEYWORD(Volley.newRequestQueue(context), videoID, "watch_video_id");
                        break;

                    case "isEndofPlayList":
                        isLastPlayItem = Boolean.parseBoolean(msg.getData().getString("message"));
                        break;
                    case "playStatus_unstarted":
                        //無法解讀 play 失敗
                        break;
                    //結束播放
                    case "playStatus_ended":
                        LogUtil.show("Repeat Type => ", String.valueOf(Config.repeatType));
                        img_youtube.setVisibility(View.VISIBLE);
                        if (Config.linkType == 1) {
                            //Play List
                            if (Config.repeatType == 0) isPlaylistEnded();
                            if (Config.repeatType == 1)
                                HandleMessage.set(handler, "videoPlay");

                            //if this video is last item, then replay this list
                            if (isLastPlayItem) {
                                webPlayer.loadScript(JavaScript.replayPlayList());
                            }
//                            if (Config.repeatType == 2)
//                                webPlayer.loadScript(JavaScript.playPreviousVideo());
                        } else {
                            //Single Video
                            if (Config.repeatType > 0) {
                                HandleMessage.set(handler, "videoPlay");
                            } else {
                                customNotificationManager.setReplay();
                                isReplay = true;
                            }
                        }
                        if (null != FullscreenWebPlayer.handler)
                            HandleMessage.set(FullscreenWebPlayer.handler, "playStatus_ended");
                        break;
                    //播放中
                    case "playStatus_playing":
                        img_youtube.setVisibility(View.GONE);
                        isVideoPlaying = true;
                        customNotificationManager.setPause();
                        webPlayer.loadScript(JavaScript.getVidUpdateNotiContent());
                        if (null != FullscreenWebPlayer.handler)
                            HandleMessage.set(FullscreenWebPlayer.handler, "playStatus_playing");
                        break;
                    //停止播放
                    case "playStatus_paused":
                        img_youtube.setVisibility(View.VISIBLE);
                        isVideoPlaying = false;
                        customNotificationManager.setPlay();
                        if (null != FullscreenWebPlayer.handler)
                            HandleMessage.set(FullscreenWebPlayer.handler, "playStatus_paused");
                        break;
                    //緩存中
                    case "playStatus_buffering":
                        String quality = Config.getPlaybackQuality();
//                        webPlayer.loadScript(JavaScript.resetPlaybackQuality(quality));
                        break;
                    case "setPlaybackQuality":
                        Integer qualityIndex = Integer.parseInt(msg.getData().getString("message", "0"));
                        String str_quality = Config.getPlaybackQuality(qualityIndex);
                        webPlayer.loadScript(JavaScript.resetPlaybackQuality(str_quality));
                        break;
                    case "setWindowsScaleType":
                        int partScrnWidth = scrnWidth / 6, windowsScaleType = Integer.parseInt(msg.getData().getString("message", "6"));
                        float paramPlayerProportion = (float) defaultPlayerHeight / (float) defaultPlayerWidth;
                        param_player.width = partScrnWidth * windowsScaleType;
                        param_player.height = Math.round(param_player.width * paramPlayerProportion);
                        customImageHeader.setPlayerSize(param_player.width, param_player.height);
                        if (isVideoShow)
                            windowManager.updateViewLayout(windows_player, param_player);
                        Config.windowsScaleType = windowsScaleType;
                        Config.sharedPreferences.edit().putInt("windowsScaleType", Config.windowsScaleType).apply();
                        break;
                    case "setStickerSize":
                        playerHeadSize = Integer.parseInt(msg.getData().getString("message", "150"));
                        param_service.width = playerHeadSize;
                        param_service.height = playerHeadSize;
                        param_player.y = param_service.y + playerHeadSize - OVER_LAPPING_HEIGHT;
                        customImageHeader.setPlayerHeadSize(playerHeadSize);
                        windowManager.updateViewLayout(windows_head, param_service);
                        if (isVideoShow)
                            windowManager.updateViewLayout(windows_player, param_player);
                        break;
                    //定時更新播放時間
                    case "startCurrentTimeUpdate":
                        if (null != updateCurrentTimeTimer) updateCurrentTimeTimer.cancel();
                        updateCurrentTimeTimer = new Timer();
                        updateCurrentTimeTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                webPlayer.loadScript(JavaScript.getCurrentTime());
                            }
                        }, 1000, 2000);
                        break;
                    //設定總播放時間
                    case "setDurationTime":
                        Integer durationTime = Integer.parseInt(msg.getData().getString("message", "0"));
                        seekBar_player.setMax(durationTime);
                        if (null != FullscreenWebPlayer.handler)
                            HandleMessage.set(FullscreenWebPlayer.handler, "setDurationTime", String.valueOf(durationTime));
                        break;
                    //設定目前播放進度
                    case "setCurrentTime":
                        currentTime = Integer.parseInt(msg.getData().getString("message", "0"));
                        seekBar_player.setProgress(currentTime);
                        if (null != FullscreenWebPlayer.handler)
                            HandleMessage.set(FullscreenWebPlayer.handler, "setCurrentTime", String.valueOf(currentTime));
                        break;
                    case "imageEntireClick":
                        img_entire_width.performClick();
                        break;
                    case "videoForward":
                        webPlayer.loadScript(JavaScript.seekTo(currentTime - 10));
                        break;
                    case "videoBackward":
                        webPlayer.loadScript(JavaScript.seekTo(currentTime + 10));
                        break;
                    case "videoPause":
                        webPlayer.loadScript(JavaScript.pauseVideo());
                        break;
                    case "videoPlay":
                        webPlayer.loadScript(JavaScript.playVideo());
                        break;
//                    case "setPlayOver":
//                        customNotificationManager.setPlayOver();
//                        isReplay = true;
//                        break;
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isVideoPlaying = true;
        Config.linkType = 0;
        if (windows_player != null) {
            windowManager.removeView(windows_player);
            windowManager.removeView(windows_head);
            windowManager.removeView(windows_close);
            webPlayer.destroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        customImageHeader.changeScreenDirection(configuration);
    }
}