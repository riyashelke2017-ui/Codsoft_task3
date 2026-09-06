package com.example.alarmtech;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "alarm_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        int alarmId = intent.getIntExtra("alarmId", -1);

        String toneUri = intent.getStringExtra("toneUri");

        createNotificationChannel(context);

        Intent ringIntent = new Intent(context, RingActivity.class);

        ringIntent.putExtra("alarmId", alarmId);
        ringIntent.putExtra("toneUri", toneUri);

        ringIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent fullScreenIntent = PendingIntent.getActivity(
                context,
                alarmId,
                ringIntent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri;

        try {
            soundUri = Uri.parse(toneUri);
        } catch (Exception e) {
            soundUri = RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
            );
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle("⏰ ALARM TECH")
                        .setContentText("Your alarm is ringing!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setSound(soundUri)
                        .setFullScreenIntent(fullScreenIntent, true);

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(alarmId, builder.build());

        // Also open alarm screen
        try {
            context.startActivity(ringIntent);
        } catch (Exception ignored) {
        }

        // Schedule same alarm for next day
        scheduleNextDay(context, alarmId, toneUri);
    }

    private void scheduleNextDay(
            Context context,
            int alarmId,
            String toneUri
    ) {

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long nextTime =
                System.currentTimeMillis() +
                        24 * 60 * 60 * 1000;

        Intent intent = new Intent(context, AlarmReceiver.class);

        intent.putExtra("alarmId", alarmId);
        intent.putExtra("toneUri", toneUri);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTime,
                    pendingIntent
            );
        }
    }

    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Alarm Notifications",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription("Alarm Clock notifications");
            channel.setLockscreenVisibility(
                    android.app.Notification.VISIBILITY_PUBLIC
            );

            NotificationManager manager =
                    context.getSystemService(
                            NotificationManager.class
                    );

            manager.createNotificationChannel(channel);
        }
    }
}
