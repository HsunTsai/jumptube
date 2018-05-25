package com.turtle.hsun.jumptube.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.turtle.hsun.jumptube.Config.Config;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.LogUtil;

import static com.turtle.hsun.jumptube.Config.Config.sharedPreferences;

public class Settings extends AppCompatActivity implements View.OnClickListener {

    //Components
    private LinearLayout layout_video_quality, layout_about;
    private TextView txt_video_quality;

    //Parameters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        layout_video_quality = (LinearLayout) findViewById(R.id.layout_video_quality);
        layout_about = (LinearLayout) findViewById(R.id.layout_about);
        txt_video_quality = (TextView) findViewById(R.id.txt_video_quality);

        layout_about.setOnClickListener(this);
        layout_video_quality.setOnClickListener(this);
        txt_video_quality.setText(Config.getPlaybackQuality());

        Config.playbackQuality = sharedPreferences.getInt(getString(R.string.videoQuality), 3);
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
        switch (v.getId()) {
            case R.id.layout_video_quality:
                final int[] checked = new int[1];
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setTitle(getString(R.string.video_quality));
                builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Config.playbackQuality = checked[0];
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(getString(R.string.videoQuality), checked[0]);
                        editor.commit();
                        txt_video_quality.setText(Config.getPlaybackQuality());
                        LogUtil.show("New Video Quality => ", Config.getPlaybackQuality());
                    }
                });
                String[] items = {"Auto", "1080p", "720p", "480p", "360p", "240p", "144p"};
                checked[0] = sharedPreferences.getInt(getString(R.string.videoQuality), 3);
                LogUtil.show("Now Video Quality => ", Config.getPlaybackQuality());
                builder.setSingleChoiceItems(items, checked[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int ith) {
                        checked[0] = ith;
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.layout_about:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/HsunTsai/jumptube"));
                startActivity(browserIntent);
                break;
        }
    }
}
