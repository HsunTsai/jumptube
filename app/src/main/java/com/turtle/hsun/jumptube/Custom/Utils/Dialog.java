package com.turtle.hsun.jumptube.Custom.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.turtle.hsun.jumptube.Config.Config;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.HandleMessage;
import com.turtle.hsun.jumptube.Utils.LogUtil;
import com.turtle.hsun.jumptube.Utils.Service;

import static com.turtle.hsun.jumptube.Config.Config.sharedPreferences;

public class Dialog {

    private static Integer stickerCheckedIndex = 0;

    public static void videoQuality(final Activity activity) {
        final Integer[] checkedIndex = {0};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.video_quality));
        builder.setPositiveButton(activity.getString(R.string.done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //set Playing quality
                Config.playbackQuality = checkedIndex[0];
                sharedPreferences.edit()
                        .putInt(activity.getString(R.string.videoQuality), Config.playbackQuality).apply();
                HandleMessage.set(PlayerService.handler, "setPlaybackQuality", String.valueOf(Config.playbackQuality));
                LogUtil.show("New Video Quality => ", Config.getPlaybackQuality());
            }
        });
        String[] items = {"Auto", "1080p", "720p", "480p", "360p", "240p", "144p"};
        builder.setSingleChoiceItems(items, Config.playbackQuality, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int ith) {
                checkedIndex[0] = ith;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void videoSize(final Activity activity) {
        final Integer[] checkedIndex = {0};
        String[] items = {
                activity.getString(R.string.video_size_small), //3
                activity.getString(R.string.video_size_medium), //4
                activity.getString(R.string.video_size_large), //5
                activity.getString(R.string.video_size_full)}; //6
        Integer defaultCheckIndex = Config.windowsScaleType - 3 > 0 ? Config.windowsScaleType - 3 : 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.video_size));
        builder.setPositiveButton(activity.getString(R.string.done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //set Playing windows scale
                Integer windowsScaleType = checkedIndex[0] + 3;
                if (Service.isRunning(activity, PlayerService.class)) {
                    //full=6 large=5 medium=4 small=3
                    HandleMessage.set(PlayerService.handler, "setWindowsScaleType", String.valueOf(windowsScaleType));
                } else {
                    Config.windowsScaleType = windowsScaleType;
                    Config.sharedPreferences.edit().putInt("windowsScaleType", Config.windowsScaleType).apply();
                }
                LogUtil.show("New Video Size => ", Config.getPlaybackQuality());
            }
        });
        builder.setSingleChoiceItems(items, defaultCheckIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int ith) {
                checkedIndex[0] = ith;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void stickerSize(final Activity activity) {
        String[] items = {
                activity.getString(R.string.sticker_size_large),
                activity.getString(R.string.sticker_size_medium),
                activity.getString(R.string.sticker_size_small)};
        //activity.getString(R.string.sticker_size_disappear)
        Integer stickerSize = Config.sharedPreferences.getInt("stickerSize", 150);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.sticker_size));
        builder.setPositiveButton(activity.getString(R.string.done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //set Header size
                Integer[] stickerSize = {200, 150, 100};
                if (Service.isRunning(activity, PlayerService.class)) {
                    HandleMessage.set(PlayerService.handler, "setStickerSize",
                            String.valueOf(stickerSize[stickerCheckedIndex]));
                }
                Config.sharedPreferences.edit().putInt("stickerSize", stickerSize[stickerCheckedIndex]).apply();
                LogUtil.show("New Header Size => ", String.valueOf(stickerSize[stickerCheckedIndex]));
            }
        });
        builder.setSingleChoiceItems(items,
                stickerSize == 200 ? 0 : stickerSize == 150 ? 1 : stickerSize == 100 ? 2 : 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int ith) {
                        stickerCheckedIndex = ith;
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
