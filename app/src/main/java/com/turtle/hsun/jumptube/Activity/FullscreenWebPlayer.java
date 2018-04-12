package com.turtle.hsun.jumptube.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.turtle.hsun.jumptube.JavaScript;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.HandleMessage;

public class FullscreenWebPlayer extends Activity {

    //Parameter
    public static Activity activity;
    public static Boolean active = false;

    //Components
    private ViewGroup parent;
    private WebView player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_web_player);

        active = true;
        activity = this;

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_fullscreen);
        player = PlayerService.webPlayer.getPlayer();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        );
        parent = (ViewGroup) player.getParent();
        parent.removeView(player);
        linearLayout.addView(player, params);

        PlayerService.webPlayer.loadScript(JavaScript.playVideo());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (active) {
            ((ViewGroup) player.getParent()).removeView(player);
            parent.addView(player);
            HandleMessage.set(PlayerService.handler, "startAgain");
        }
        active = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (active) activity.onBackPressed();
        active = false;
    }
}
