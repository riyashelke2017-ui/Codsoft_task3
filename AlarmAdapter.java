package com.example.alarmtech;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormatSymbols;
import java.util.List;
import java.util.Locale;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;
    private OnAlarmActionListener listener;

    public interface OnAlarmActionListener {
        void onToggle(Alarm alarm, boolean enabled);
        void onDelete(Alarm alarm);
    }

    public AlarmAdapter(List<Alarm> alarmList, OnAlarmActionListener listener) {
        this.alarmList = alarmList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);

        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {

        Alarm alarm = alarmList.get(position);

        String amPm = alarm.getHour() >= 12 ? "PM" : "AM";

        int displayHour = alarm.getHour() % 12;

        if (displayHour == 0) {
            displayHour = 12;
        }

        String time = String.format(
                Locale.getDefault(),
                "%02d:%02d %s",
                displayHour,
                alarm.getMinute(),
                amPm
        );

        holder.txtTime.setText(time);

        holder.txtTone.setText("🔔 Alarm tone");

        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(alarm.isEnabled());

        holder.switchAlarm.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        listener.onToggle(alarm, isChecked)
        );

        holder.btnDelete.setOnClickListener(
                v -> listener.onDelete(alarm)
        );
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {

        TextView txtTime;
        TextView txtTone;
        Switch switchAlarm;
        Button btnDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTime = itemView.findViewById(R.id.txtAlarmTime);
            txtTone = itemView.findViewById(R.id.txtAlarmTone);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
