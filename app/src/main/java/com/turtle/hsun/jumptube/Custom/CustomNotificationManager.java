package com.turtle.hsun.jumptube.Custom;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.turtle.hsun.jumptube.AsyncTask.ImageLoadTask;
import com.turtle.hsun.jumptube.AsyncTask.LoadDetailsTask;
import com.turtle.hsun.jumptube.Config;
import com.turtle.hsun.jumptube.PlayerService;
import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class CustomNotificationManager {

    //Parameter
    private String videoId;
    private NotificationListener listener;

    //Components
    private Notification notification;
    private NotificationManager notificationManager;
    private RemoteViews notification_large, notification_small;

    @SuppressLint("ServiceCast")
    public CustomNotificationManager(Context context, String videoId) {
        this.videoId = videoId;
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        //Notification view Intitial
        notification_large = new RemoteViews(context.getPackageName(), R.layout.notification_large);
        notification_small = new RemoteViews(context.getPackageName(), R.layout.notification_small);

        //Notification inti
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_status_bar)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContent(notification_small)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(false);
        notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            notification.bigContentView = notification_large;

        //Intent to do things
        Intent intentAction = new Intent(context, PlayerService.class);

        //stop Service using initViews Intent
        notification_small.setOnClickPendingIntent(R.id.imgbt_stop_video,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.STOPFOREGROUND_WEB_ACTION), 0));

        notification_large.setOnClickPendingIntent(R.id.imgbt_stop_video,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.STOPFOREGROUND_WEB_ACTION), 0));

        notification_large.setOnClickPendingIntent(R.id.imgbt_close,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.STOPFOREGROUND_WEB_ACTION), 0));

        //Pause, Play Video using initViews Intent
        notification_small.setOnClickPendingIntent(R.id.imgbt_play_pause_video,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.PAUSE_PLAY_ACTION), 0));

        notification_large.setOnClickPendingIntent(R.id.imgbt_play_pause_video,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.PAUSE_PLAY_ACTION), 0));

        //Next Video using initViews Intent
        notification_small.setOnClickPendingIntent(R.id.imgbt_next_video,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.NEXT_ACTION), 0));

        notification_large.setOnClickPendingIntent(R.id.imgbt_next_video,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.NEXT_ACTION), 0));

        //Previous Video using initViews Intent
        notification_large.setOnClickPendingIntent(R.id.imgbt_previous_video,
                PendingIntent.getService(context.getApplicationContext(), 0,
                        intentAction.setAction(Config.ACTION.PREV_ACTION), 0));
    }

    //Interface
    public interface NotificationListener {
        public void onFinish(Notification notification);
        public void onBitmapChange(Bitmap bitmap);
    }

    public void setOnFinish(NotificationListener listener) {
        this.listener = listener;
        setAuthor(videoId);
        this.listener.onFinish(notification);
    }

    public void setAuthor(String videoId) {
        //設定Notificaiton 影片作者名稱
        String title, author;
        Bitmap bitmap;
        try {
            bitmap = new ImageLoadTask("https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg").execute().get();
            String details = new LoadDetailsTask(
                    "https://www.youtube.com/oembed?url=http://www.youtu.be/watch?v=" + videoId + "&format=json")
                    .execute().get();
            JSONObject detailsJson = new JSONObject(details);
            title = detailsJson.getString("title");
            author = detailsJson.getString("author_name");

            notification_large.setImageViewBitmap(R.id.img_preview, bitmap);
            notification_large.setTextViewText(R.id.txt_title, title);
            notification_large.setTextViewText(R.id.txt_author, author);

            notification_small.setTextViewText(R.id.txt_title, title);
            notification_small.setImageViewBitmap(R.id.img_preview, bitmap);

            notificationManager.notify(Config.NOTIFICATION_ID, notification);

            //painting shadow
//            Paint shadow = new Paint();
//            shadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF005500);
//            new Canvas().drawBitmap(bitmap, 0.0f, 0.0f, shadow);
            this.listener.onBitmapChange(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setPlayOver() {
        LogUtil.show("PlayNotification", "status => over");
        notification_large.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.replay);
        notification_small.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.replay);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    public void setPause() {
        LogUtil.show("PlayNotification", "status => pause");
        notification_large.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.pause);
        notification_small.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.pause);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    public void setPlay() {
        LogUtil.show("PlayNotification", "status => play");
        notification_large.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.play);
        notification_small.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.play);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }

    public void setReplay() {
        LogUtil.show("PlayNotification", "status => relay");
        notification_large.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.replay);
        notification_small.setImageViewResource(R.id.imgbt_play_pause_video, R.drawable.replay);
        notificationManager.notify(Config.NOTIFICATION_ID, notification);
    }
}
