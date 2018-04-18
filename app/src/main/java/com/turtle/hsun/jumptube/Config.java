package com.turtle.hsun.jumptube;

public class Config {
    public static Boolean develop_mode = true;

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
    //if repeatType = 2  --> repeatType single
    public static int repeatType = 0;

    //Finish service on end video
    public static boolean finishOnEnd = false;

    //Playback Quality
    public static int playbackQuality = 3;

    //Float window (Only for 1 ~ 5)
    public static int windowsScaleType = 6;

    public static String getPlaybackQuality() {
        //0 = auto, 1 = hd1080, 2 = hd720, 3 = large(480p), 4 = medium(360p), 5 = small(240p), 6 = tiny(144p),
        String[] strPlaybackQuality = {"auto", "hd1080", "hd720", "large", "medium", "small", "tiny"};
        return strPlaybackQuality[playbackQuality];
    }

    //Actions
    public interface ACTION {
        String PREV_ACTION = "com.turtle.hsun.jumptube.action.prev";
        String PAUSE_PLAY_ACTION = "com.turtle.hsun.jumptube.action.play";
        String NEXT_ACTION = "com.turtle.hsun.jumptube.action.ic_next";
        String STARTFOREGROUND_WEB_ACTION = "com.turtle.hsun.jumptube.action.playingweb";
        String STOPFOREGROUND_WEB_ACTION = "com.turtle.hsun.jumptube.action.stopplayingweb";
    }
}
