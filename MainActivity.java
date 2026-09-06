package com.example.alarmtech;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements AlarmAdapter.OnAlarmActionListener {

    private TextView txtTime;
    private TextView txtDate;
    private TextView txtCount;

    private ArrayList<Alarm> alarmList;

    private AlarmAdapter adapter;

    private static final int TONE_REQUEST = 100;

    private Uri selectedTone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        txtTime = findViewById(R.id.txtTime);
        txtDate = findViewById(R.id.txtDate);
        txtCount = findViewById(R.id.txtCount);

        Button addAlarm =
                findViewById(R.id.btnAddAlarm);

        RecyclerView recycler =
                findViewById(R.id.recyclerAlarms);

        alarmList = new ArrayList<>();

        loadAlarms();

        adapter =
                new AlarmAdapter(
                        alarmList,
                        this
                );

        recycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recycler.setAdapter(adapter);

        updateClock();

        addAlarm.setOnClickListener(
                v -> showTimePicker()
        );

        requestNotificationPermission();

        createNotificationChannel();
    }

    private void updateClock() {

        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "hh:mm:ss a",
                        Locale.getDefault()
                );

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "EEEE, dd MMMM yyyy",
                        Locale.getDefault()
                );

        txtTime.setText(
                timeFormat.format(new Date())
        );

        txtDate.setText(
                dateFormat.format(new Date())
        );

        txtTime.postDelayed(
                this::updateClock,
                1000
        );
    }

    private void showTimePicker() {

        Calendar calendar =
                Calendar.getInstance();

        int hour =
                calendar.get(Calendar.HOUR_OF_DAY);

        int minute =
                calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog =
                new TimePickerDialog(
                        this,
                        (view, selectedHour, selectedMinute) -> {

                            chooseAlarmTone(
                                    selectedHour,
                                    selectedMinute
                            );

                        },
                        hour,
                        minute,
                        false
                );

        dialog.setTitle("Set Alarm Time");

        dialog.show();
    }

    private void chooseAlarmTone(
            int hour,
            int minute
    ) {

        Intent intent =
                new Intent(
                        RingtoneManager.ACTION_RINGTONE_PICKER
                );

        intent.putExtra(
                RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_ALARM
        );

        intent.putExtra(
                RingtoneManager.EXTRA_RINGTONE_TITLE,
                "Choose Alarm Tone"
        );

        intent.putExtra(
                RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                false
        );

        startActivityForResult(
                intent,
                TONE_REQUEST
        );

        getPreferences(
                MODE_PRIVATE
        ).edit()
                .putInt("pendingHour", hour)
                .putInt("pendingMinute", minute)
                .apply();
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == TONE_REQUEST &&
                resultCode == RESULT_OK &&
                data != null) {

            selectedTone =
                    data.getParcelableExtra(
                            RingtoneManager.EXTRA_RINGTONE_PICKED_URI
                    );

            if (selectedTone == null) {

                selectedTone =
                        RingtoneManager.getDefaultUri(
                                RingtoneManager.TYPE_ALARM
                        );
            }

            int hour =
                    getPreferences(MODE_PRIVATE)
                            .getInt("pendingHour", 0);

            int minute =
                    getPreferences(MODE_PRIVATE)
                            .getInt("pendingMinute", 0);

            createAlarm(
                    hour,
                    minute,
                    selectedTone.toString()
            );
        }
    }

    private void createAlarm(
            int hour,
            int minute,
            String toneUri
    ) {

        int id =
                (int) System.currentTimeMillis();

        Alarm alarm =
                new Alarm(
                        id,
                        hour,
                        minute,
                        true,
                        toneUri
                );

        alarmList.add(alarm);

        saveAlarms();

        scheduleAlarm(alarm);

        adapter.notifyDataSetChanged();

        updateCount();

        Toast.makeText(
                this,
                "⏰ Alarm set successfully!",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void scheduleAlarm(Alarm alarm) {

        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                Context.ALARM_SERVICE
                        );

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                alarm.getHour()
        );

        calendar.set(
                Calendar.MINUTE,
                alarm.getMinute()
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        if (calendar.getTimeInMillis()
                <= System.currentTimeMillis()) {

            calendar.add(
                    Calendar.DAY_OF_YEAR,
                    1
            );
        }

        Intent intent =
                new Intent(
                        this,
                        AlarmReceiver.class
                );

        intent.putExtra(
                "alarmId",
                alarm.getId()
        );

        intent.putExtra(
                "toneUri",
                alarm.getToneUri()
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        alarm.getId(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (alarmManager.canScheduleExactAlarms()) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );

            } else {

                Intent settingsIntent =
                        new Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        );

                try {
                    startActivity(settingsIntent);
                } catch (Exception ignored) {
                }

                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

        } else {

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private void cancelAlarm(Alarm alarm) {

        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                Context.ALARM_SERVICE
                        );

        Intent intent =
                new Intent(
                        this,
                        AlarmReceiver.class
                );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        alarm.getId(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onToggle(
            Alarm alarm,
            boolean enabled
    ) {

        alarm.setEnabled(enabled);

        if (enabled) {
            scheduleAlarm(alarm);

            Toast.makeText(
                    this,
                    "Alarm turned ON",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            cancelAlarm(alarm);

            Toast.makeText(
                    this,
                    "Alarm turned OFF",
                    Toast.LENGTH_SHORT
            ).show();
        }

        saveAlarms();
    }

    @Override
    public void onDelete(Alarm alarm) {

        cancelAlarm(alarm);

        alarmList.remove(alarm);

        saveAlarms();

        adapter.notifyDataSetChanged();

        updateCount();

        Toast.makeText(
                this,
                "Alarm deleted",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateCount() {

        int count = alarmList.size();

        txtCount.setText(
                count + (count == 1 ? " alarm" : " alarms")
        );
    }

    private void saveAlarms() {

        try {

            JSONArray array =
                    new JSONArray();

            for (Alarm alarm : alarmList) {

                JSONObject object =
                        new JSONObject();

                object.put(
                        "id",
                        alarm.getId()
                );

                object.put(
                        "hour",
                        alarm.getHour()
                );

                object.put(
                        "minute",
                        alarm.getMinute()
                );

                object.put(
                        "enabled",
                        alarm.isEnabled()
                );

                object.put(
                        "tone",
                        alarm.getToneUri()
                );

                array.put(object);
            }

            getPreferences(MODE_PRIVATE)
                    .edit()
                    .putString(
                            "alarms",
                            array.toString()
                    )
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAlarms() {

        String data =
                getPreferences(MODE_PRIVATE)
                        .getString(
                                "alarms",
                                "[]"
                        );

        try {

            JSONArray array =
                    new JSONArray(data);

            for (int i = 0;
                 i < array.length();
                 i++) {

                JSONObject object =
                        array.getJSONObject(i);

                Alarm alarm =
                        new Alarm(
                                object.getInt("id"),
                                object.getInt("hour"),
                                object.getInt("minute"),
                                object.getBoolean("enabled"),
                                object.getString("tone")
                        );

                alarmList.add(alarm);

                if (alarm.isEnabled()) {
                    scheduleAlarm(alarm);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        updateCount();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            AlarmReceiver.CHANNEL_ID,
                            "Alarm Notifications",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Alarm Clock Notifications"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            requestPermissions(
                    new String[]{
                            "android.permission.POST_NOTIFICATIONS"
                    },
                    200
            );
        }
    }
}
