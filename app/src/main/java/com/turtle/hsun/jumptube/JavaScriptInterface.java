package com.turtle.hsun.jumptube;

import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.turtle.hsun.jumptube.Custom.WebPlayer;
import com.turtle.hsun.jumptube.Utils.HandleMessage;
import com.turtle.hsun.jumptube.Utils.LogUtil;

/**
 * Created by Hsun on 18/4/12.
 */
public class JavaScriptInterface {

    //Parameter
    private Handler handler;
    private WebPlayer webPlayer;
    private Integer currentIndex = 0, playlistCount = 0;

    public JavaScriptInterface(PlayerService playerService) {
        this.handler = PlayerService.handler;
        this.webPlayer = PlayerService.webPlayer;
    }

    //播放狀態
    //-1 – unstarted
    // 0 – ended
    // 1 – playing
    // 2 – paused
    // 3 – buffering
    // 5 – video cued
    @JavascriptInterface
    public void showPlayerState(int status) {
        LogUtil.show("Player Status ", String.valueOf(status));
        switch (status) {
            case -1:
                HandleMessage.set(handler, "playStatus_unstarted");
                break;
            case 0:
                HandleMessage.set(handler, "playStatus_ended");
                break;
            case 1:
                HandleMessage.set(handler, "playStatus_playing");
                HandleMessage.set(handler, "startCurrentTimeUpdate");
                webPlayer.loadScript(JavaScript.getDuration());
                break;
            case 2:
                HandleMessage.set(handler, "playStatus_paused");
                break;
            case 3:
                HandleMessage.set(handler, "playStatus_buffering");
                break;
        }
    }

    //目前播放的時間
    @JavascriptInterface
    public void showCurrentTime(int time) {
//        LogUtil.show("showCurrentTime", String.valueOf(time));
        HandleMessage.set(handler, "setCurrentTime", String.valueOf(time));
    }

    //播放總時間
    @JavascriptInterface
    public void showDurationTime(int time) {
//        LogUtil.show("showDuration", String.valueOf(time));
        HandleMessage.set(handler, "setDurationTime", String.valueOf(time));
    }

    @JavascriptInterface
    public void showVID(String videoID) {
        LogUtil.show("New Video Id ", videoID);
        HandleMessage.set(handler, "setImageTitleAuthor", String.valueOf(videoID));
    }

    @JavascriptInterface
    public void playlistItems(final String[] items) {
        LogUtil.show("Playlist Items", String.valueOf(items.length));
        playlistCount = items.length;
        if (currentIndex == playlistCount - 1)
            HandleMessage.set(handler, "setPlayOver");
    }

    @JavascriptInterface
    public void currVidIndex(final int index) {
        LogUtil.show("Current Video Index ", String.valueOf(index));
        currentIndex = index;
        if (currentIndex == playlistCount - 1)
            HandleMessage.set(handler, "setPlayOver");
    }
}