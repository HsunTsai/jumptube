package com.turtle.hsun.jumptube.Config;

import android.content.SharedPreferences;

public class Config {
    public static Boolean develop_mode = false;
    public static SharedPreferences sharedPreferences;
    public static String user_id = "";

    //For Result Activity
    public static int OVERLAY_PERMISSION_REQ_CODE = 12345,
            OVERLAY_PERMISSION_REQ_BACKTO_ACT_CODE = 23456,
            NOTIFICATION_ID = 101;

    //Type of link
    //Single song link = 0
    //Playlist link = 1
    public static int linkType = 0;

    //Repeat
    //if repeatType = 0  --> no repeatType
    //if repeatType = 1  --> repeatType complete
    //if repeatType = 2  --> repeatType single(Remove)
    public static int repeatType = 0;

    //Playback Quality
    public static int playbackQuality = 0;

    //Float window (Only for 3 ~ 6)
    public static int windowsScaleType = 6;

    public static String getPlaybackQuality() {
        //0 = auto, 1 = hd1080, 2 = hd720, 3 = large(480p), 4 = medium(360p), 5 = small(240p), 6 = tiny(144p),
        String[] strPlaybackQuality = {"auto", "hd1080", "hd720", "large", "medium", "small", "tiny"};
        return strPlaybackQuality[playbackQuality];
    }

    public static String getPlaybackQuality(Integer index) {
        //0 = auto, 1 = hd1080, 2 = hd720, 3 = large(480p), 4 = medium(360p), 5 = small(240p), 6 = tiny(144p),
        String[] strPlaybackQuality = {"auto", "hd1080", "hd720", "large", "medium", "small", "tiny"};
        return strPlaybackQuality[index];
    }

    public static String
            webHomePage_1 = "https://m.yo",
            webHomePage_2 = "utube.com/",
            webHomePage = webHomePage_1 + webHomePage_2,
            webAccountPage = webHomePage + "feed/account",
            webTrendingPage = webHomePage + "feed/trending",
            webSubscriptionPage = webHomePage + "feed/subscriptions",
            youtubeSuggestURL = "http://suggestqueries.google.com/complete/search?client=youtube&ds=yt&client=firefox&q=";

    //Actions
    public interface ACTION {
        String PREV_ACTION = "com.turtle.hsun.jumptube.action.prev";
        String PAUSE_PLAY_ACTION = "com.turtle.hsun.jumptube.action.play";
        String NEXT_ACTION = "com.turtle.hsun.jumptube.action.ic_next";
        String STARTFOREGROUND_WEB_ACTION = "com.turtle.hsun.jumptube.action.playingweb";
        String STOPFOREGROUND_WEB_ACTION = "com.turtle.hsun.jumptube.action.stopplayingweb";
    }
}
