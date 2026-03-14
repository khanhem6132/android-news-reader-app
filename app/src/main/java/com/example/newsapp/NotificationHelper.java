package com.example.newsapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

public class NotificationHelper {

    private static final String CHANNEL_ID = "news_channel";

    public static void showRandomNewsNotification(Context context, News randomNews) {
        // Tạo kênh thông báo (chỉ cần làm 1 lần)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo tin tức",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo bài viết mới");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Intent mở NewsDetailActivity khi click
        Intent intent = new Intent(context, NewsDetailActivity.class);
        intent.putExtra("title", randomNews.getTitle());
        intent.putExtra("url", randomNews.getLink());
        intent.putExtra("image", randomNews.getImageUrl());
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(NewsDetailActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // 🔔 icon trong mipmap/drawable
                .setContentTitle("📰 Bài viết mới!")
                .setContentText(randomNews.getTitle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
