package com.turtle.hsun.jumptube.Custom.Components;

import android.content.Context;
import android.os.Build;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.turtle.hsun.jumptube.JavaScriptInterface;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.Utils.HandleMessage;

/**
 * Created by Hsun on 18/4/12.
 */
public class WebPlayer {

    private Context context;
    private WebView player;

    public WebPlayer(Context context) {
        this.player = new WebView(context);
        this.context = context;
    }

    public void setupPlayer() {
        player.getSettings().setJavaScriptEnabled(true);
        //For debugging using chrome on PC
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            player.setWebContentsDebuggingEnabled(true);
        }
        player.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            player.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        player.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");
        player.addJavascriptInterface(new JavaScriptInterface((PlayerService) context), "Interface");
        player.setWebViewClient(new webViewClient());
    }

    private class webViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            HandleMessage.set(PlayerService.handler, "addStateChangeListener");
        }
    }

    public void loadScript(final String s) {
        player.post(new Runnable() {
            @Override
            public void run() {
                player.loadUrl(s);
            }
        });
    }

    public WebView getPlayer() {
        return player;
    }

    public void destroy() {
        player.destroy();
    }

    public void loadDataWithUrl(String baseUrl, String videoHTML, String mimeType, String encoding, String historyUrl) {
        player.loadDataWithBaseURL(baseUrl, videoHTML, mimeType, encoding, historyUrl);
    }
}