package com.turtle.hsun.jumptube.Config;

/**
 * Created by Hsun on 18/4/12.
 */
public class ConstantStrings {

    public static String getVideoHTML(String videoID) {
        return "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <script src=\"https://www.youtube.com/iframe_api\"></script>\n" +
                "    <style type=\"text/css\">\n" +
                "        html, body {\n" +
                "            margin: 0px;\n" +
                "            padding: 0px;\n" +
                "            border: 0px;\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "    </style>" +
                "  </head>\n" +
                "\n" +
                "  <body>\n" +
                "    <iframe style=\"display: block;\" id=\"player\" frameborder=\"0\"  width=\"100%\" height=\"100%\" " +
                "       src=\"https://www.youtube.com/embed/" + videoID +
                "?enablejsapi=1&autoplay=1&iv_load_policy=3&fs=0&rel=0&autohide=1&controls=0&showinfo=0\">" +
                "    </iframe>\n" +
                "    <script type=\"text/javascript\">\n" +
                "      var tag = document.createElement('script');\n" +
                "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
                "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
                "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
                "      var player;\n" +
                "      function onYouTubeIframeAPIReady() {\n" +
                "          player = new YT.Player('player', {\n" +
                "              events: {\n" +
                "                  'onReady': onPlayerReady\n" +
                "              }\n" +
                "          });\n" +
                "      }\n" +
                "      function onPlayerReady(event) {\n" +
                "          player.setPlaybackQuality(\"" + Config.getPlaybackQuality() + "\");\n" +
                "      }\n" +
                "    </script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getPlayListHTML(String playListID) {
        return "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <script src=\"https://www.youtube.com/iframe_api\"></script>\n" +
                "    <style type=\"text/css\">\n" +
                "        html, body {\n" +
                "            margin: 0px;\n" +
                "            padding: 0px;\n" +
                "            border: 0px;\n" +
                "            width: 100%;\n" +
                "            height: 100%;\n" +
                "        }\n" +
                "    </style>" +
                "  </head>\n" +
                "\n" +
                "  <body>\n" +
                "    <iframe style=\"display: block;\" id=\"player\" frameborder=\"0\" width=\"100%\" height=\"100%\" " +
                "       src=\"https://www.youtube.com/embed/" +
                "?list=" + playListID +
                "&enablejsapi=1&autoplay=1&iv_load_policy=3&fs=0&rel=0&autohide=1&controls=0&showinfo=0\">" +
                "    </iframe>\n" +
                "    <script type=\"text/javascript\">\n" +
                "      var tag = document.createElement('script');\n" +
                "\n" +
                "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
                "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
                "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
                "      var player;\n" +
                "      function onYouTubeIframeAPIReady() {\n" +
                "          player = new YT.Player('player', {\n" +
                "              events: {\n" +
                "                  'onReady': onPlayerReady\n" +
                "              }\n" +
                "          });\n" +
                "      }\n" +
                "      function onPlayerReady(event) {\n" +
                "          player.setPlaybackQuality(\"" + Config.getPlaybackQuality() + "\");\n" +
                "      }\n" +
                "    </script>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
}
