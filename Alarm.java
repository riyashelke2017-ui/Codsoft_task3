package com.example.alarmtech;

public class Alarm {

    private int id;
    private int hour;
    private int minute;
    private boolean enabled;
    private String toneUri;

    public Alarm(int id, int hour, int minute, boolean enabled, String toneUri) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
        this.toneUri = toneUri;
    }

    public int getId() {
        return id;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getToneUri() {
        return toneUri;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
