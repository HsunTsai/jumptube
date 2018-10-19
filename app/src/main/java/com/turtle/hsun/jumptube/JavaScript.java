package com.turtle.hsun.jumptube;

/**
 * Created by Hsun on 18/4/12.
 */
public class JavaScript {

    public static String loadVideoScript(String vId) {
        return "javascript:player.loadVideoById(\"" + vId + "\");";
    }

    public static String playVideo() {
        return "javascript:player.playVideo();";
    }

    public static String playNextVideo() {
        return "javascript:player.nextVideo()";
    }

    public static String playPreviousVideo() {
        return "javascript:player.previousVideo()";
    }

    public static String pauseVideo() {
        return "javascript:player.pauseVideo();";
    }

    public static String seekTo(Integer second) {
        return "javascript:player.seekTo(" + second + ");";
    }

    public static String getCurrentTime() {
        return "javascript:window.Interface.showCurrentTime(player.getCurrentTime());";
    }

    public static String getDuration() {
        return "javascript:window.Interface.showDurationTime(player.getDuration());";
    }

    public static String onPlayerStateChangeListener() {
        return "javascript:" +
                "player.addEventListener(\"onStateChange\", \"onPlayerStateChange\");" +
                "function onPlayerStateChange(event) {\n" +
                "      window.Interface.showPlayerState(player.getPlayerState());\n" +
                "  }";
    }

    public static String loadPlaylistScript(String pId) {
        return "javascript:player.loadPlaylist({list:\"" + pId + "\"});";
    }

    public static String getVidUpdateNotiContent() {
        return "javascript:window.Interface.showVID(player.getVideoData()['video_id']);";
    }

    public static String getPlaybackQuality() {
        return "javascript:window.Interface.showPlaybackQuality(player.getPlaybackQuality());";
    }

//    public static String getAvailableQualityLevels() {
//        return "javascript:window.Interface.showAvailableQualityLevels(player.getAvailableQualityLevels());";
//    }

    public static String seekToZero() {
        return "javascript:player.seekTo(0)";
    }

    public static String setLoopPlaylist() {
        return "javascript:player.setLoop(true)";
    }

    public static String unsetLoopPlaylist() {
        return "javascript:player.setLoop(false)";
    }

    public static String replayPlayList() {
        return "javascript:player.playVideoAt(0)";
    }

    public static String isPlaylistEnded() {
        return "javascript:window.Interface.getPlaylistItems(player.getPlaylist());";
        //player.getPlaylistIndex() //取得目前播放的index
    }

    public static String resetPlaybackQuality(String quality) {
        return "javascript:player.setPlaybackQuality(\"" + quality + "\");";
    }

    public static String getVideosInPlaylist() {
        return "javascript:window.Interface.videosInPlaylist(player.getPlaylist());";
    }
}
