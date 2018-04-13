package com.turtle.hsun.jumptube.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.turtle.hsun.jumptube.Config;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.LogUtil;

public class Settings extends AppCompatActivity implements View.OnClickListener{

    LinearLayout videoQuality, playerType, about;
    Button increaseCount, decreaseCount;
    CheckBox fullscreenOnRotate, stopNotPlaying;
    LinearLayout layout_settings;
    TextView quality;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();

        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.sharedpreference_name) ,Context.MODE_PRIVATE);

        Config.playbackQuality = sharedPref.getInt(getString(R.string.videoQuality), 3);

        stopNotPlaying.setChecked(sharedPref.getBoolean(getString(R.string.finishOnEnd), false));
        quality.setText(Config.getPlaybackQuality());

        videoQuality.setOnClickListener(this);
        playerType.setOnClickListener(this);
        about.setOnClickListener(this);
        increaseCount.setOnClickListener(this);
        decreaseCount.setOnClickListener(this);
        fullscreenOnRotate.setOnClickListener(this);

        stopNotPlaying.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true || isChecked == false) {
                    Config.finishOnEnd = isChecked;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(getString(R.string.finishOnEnd), isChecked);
                    editor.commit();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_quality:
                final int[] checked = new int[1];
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setTitle("Video Quality");
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Config.playbackQuality = checked[0];
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(getString(R.string.videoQuality), checked[0]);
                        editor.commit();
                        quality.setText(Config.getPlaybackQuality());
                        LogUtil.show("New Video Quality => ", Config.getPlaybackQuality());
                    }
                });
                String[] items = {"Auto", "1080p", "720p", "480p", "360p", "240p", "144p"};
                checked[0] = sharedPref.getInt(getString(R.string.videoQuality), 3);
                LogUtil.show("Now Video Quality => ", Config.getPlaybackQuality());
                builder.setSingleChoiceItems(items, checked[0], new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int ith) {
                        checked[0] = ith;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.about:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://imshyam.github.io/mintube"));
                startActivity(browserIntent);
                break;
            default:
                Snackbar snackbar = Snackbar
                        .make(layout_settings, "Action Coming soon", Snackbar.LENGTH_SHORT);
                //Changing Text Color
                View snkBar = snackbar.getView();
                TextView tv = (TextView) snkBar.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.parseColor("#e52d27"));
                snackbar.show();
        }
    }

    private void initViews() {
        quality = (TextView) findViewById(R.id.text_view_quality);
        layout_settings = (LinearLayout) findViewById(R.id.layout_settings);
        videoQuality = (LinearLayout) findViewById(R.id.video_quality);
        playerType = (LinearLayout) findViewById(R.id.player_type);
        about = (LinearLayout) findViewById(R.id.about);
        increaseCount = (Button) findViewById(R.id.increase_repeat_count);
        decreaseCount = (Button) findViewById(R.id.decrease_repeat_count);
        fullscreenOnRotate = (CheckBox) findViewById(R.id.fullscreen_on_rotate);
        stopNotPlaying = (CheckBox) findViewById(R.id.stop_not_playing);
    }
}
