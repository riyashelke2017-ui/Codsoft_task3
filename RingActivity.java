package com.example.alarmtech;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class RingActivity extends AppCompatActivity {

    private Ringtone ringtone;

    private int alarmId;
    private String toneUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ring);

        // Get alarm information
        alarmId = getIntent().getIntExtra("alarmId", -1);

        toneUri = getIntent().getStringExtra("toneUri");

        // Find views
        TextView txtTime = findViewById(R.id.txtRingTime);

        Button snooze = findViewById(R.id.btnSnooze);

        Button dismiss = findViewById(R.id.btnDismiss);

        // Get current time
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);

        int minute = calendar.get(Calendar.MINUTE);

        if (hour == 0) {
            hour = 12;
        }

        String amPm;

        if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
            amPm = "AM";
        } else {
            amPm = "PM";
        }

        // Display current time
        txtTime.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d %s",
                        hour,
                        minute,
                        amPm
                )
        );

        // Start alarm sound
        startRingtone();

        // Snooze button
        snooze.setOnClickListener(v -> snoozeAlarm());

        // Dismiss button
        dismiss.setOnClickListener(v -> dismissAlarm());
    }

    // -----------------------------
    // START ALARM SOUND
    // -----------------------------

    private void startRingtone() {

        try {

            Uri uri;

            if (toneUri != null && !toneUri.isEmpty()) {

                uri = Uri.parse(toneUri);

            } else {

                uri = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM
                );
            }

            ringtone = RingtoneManager.getRingtone(
                    getApplicationContext(),
                    uri
            );

            if (ringtone != null) {
                ringtone.play();
            }

        } catch (Exception e) {

            // Use default alarm sound if selected tone fails

            try {

                Uri defaultTone =
                        RingtoneManager.getDefaultUri(
                                RingtoneManager.TYPE_ALARM
                        );

                ringtone = RingtoneManager.getRingtone(
                        getApplicationContext(),
                        defaultTone
                );

                if (ringtone != null) {
                    ringtone.play();
                }

            } catch (Exception ignored) {
            }
        }
    }

    // -----------------------------
    // STOP ALARM SOUND
    // -----------------------------

    private void stopRingtone() {

        if (ringtone != null && ringtone.isPlaying()) {

            ringtone.stop();
        }
    }

    // -----------------------------
    // DISMISS ALARM
    // -----------------------------

    private void dismissAlarm() {

        // Stop sound
        stopRingtone();

        // Remove notification
        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager != null) {
            manager.cancel(alarmId);
        }

        // Close ringing screen
        finish();
    }

    // -----------------------------
    // SNOOZE ALARM FOR 5 MINUTES
    // -----------------------------

    private void snoozeAlarm() {

        // Stop current alarm sound
        stopRingtone();

        // Remove notification
        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (manager != null) {
            manager.cancel(alarmId);
        }

        // Get AlarmManager
        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                Context.ALARM_SERVICE
                        );

        // Create intent for AlarmReceiver
        Intent intent =
                new Intent(
                        this,
                        AlarmReceiver.class
                );

        intent.putExtra(
                "alarmId",
                alarmId
        );

        intent.putExtra(
                "toneUri",
                toneUri
        );

        // Create PendingIntent
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        alarmId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        // Snooze time = 5 minutes
        long snoozeTime =
                System.currentTimeMillis()
                        + (5 * 60 * 1000);

        // Schedule snoozed alarm
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
            );

        } else {

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
            );
        }

        // Close ringing screen
        finish();
    }

    // -----------------------------
    // STOP SOUND WHEN ACTIVITY CLOSES
    // -----------------------------

    @Override
    protected void onDestroy() {

        stopRingtone();

        super.onDestroy();
    }
}
