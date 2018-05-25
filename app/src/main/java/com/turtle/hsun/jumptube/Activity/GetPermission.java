package com.turtle.hsun.jumptube.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.turtle.hsun.jumptube.Config.Config;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;

/**
 * Created by Hsun on 18/4/12.
 */
public class GetPermission extends AppCompatActivity {

    private String videoID, playListID;
    private Integer permissionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_permission);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            videoID = extras.getString("VIDEO_ID");
            playListID = extras.getString("PLAYLIST_ID");
            permissionCode = Config.OVERLAY_PERMISSION_REQ_CODE;
        }
        else{
            permissionCode = Config.OVERLAY_PERMISSION_REQ_BACKTO_ACT_CODE;
        }

        Button getPermission = (Button) findViewById(R.id.bt_get_permission);
        getPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.canDrawOverlays(GetPermission.this)) {
                        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(i, permissionCode);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    needPermissionDialog(requestCode);
                } else {
                    Intent i = new Intent(this, PlayerService.class);
                    i.putExtra("VIDEO_ID", videoID);
                    i.putExtra("PLAYLIST_ID", playListID);
                    i.setAction(Config.ACTION.STARTFOREGROUND_WEB_ACTION);
                    startService(i);
                    finish();
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    needPermissionDialog(requestCode);
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }
        }
    }

    private void needPermissionDialog(Integer requestCode) {
        if(requestCode == Config.OVERLAY_PERMISSION_REQ_CODE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.need_permission));
            builder.setPositiveButton(getString(R.string.sure),
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(i, Config.OVERLAY_PERMISSION_REQ_CODE);
                        }
                    });
            builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }
}