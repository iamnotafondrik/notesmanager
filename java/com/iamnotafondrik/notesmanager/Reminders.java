package com.iamnotafondrik.notesmanager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by iamnotafondrik on 27.12.2016.
 */

public class Reminders {

    ReminderListener reminderListener;
    private Context context;
    private Spinner spinner;

    long remindTime;

    public Reminders (Spinner spinner, Activity activity) {
        this.spinner = spinner;
        this.context = activity.getApplicationContext();
        reminderListener = (ReminderListener) activity;
    }

    public void configSpinner () {
        String[] items = {context.getString(R.string.remind_dont), context.getString(R.string.remind_half),
                context.getString(R.string.remind_one), context.getString(R.string.remind_two),
                context.getString(R.string.remind_three), context.getString(R.string.remind_eight)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    remindTime = 0;
                }
                if (position == 1) {
                    //when = 1800000;+ System.currentTimeMillis(
                    remindTime = 1800000;
                }
                if (position == 2) {
                    remindTime = 3600000;
                }
                if (position == 3) {
                    remindTime = 7200000;
                }
                if (position == 4) {
                    remindTime = 10800000;
                }
                if (position == 5) {
                    remindTime = 28800000;
                }
                /*if (position == 6) {
                    remindTime = 60000;
                }*/
                String noteId = reminderListener.getNoteId();
                String message = reminderListener.getDescription().getText().toString();
                createRemind (noteId, context.getString(R.string.reminder), message, remindTime);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
    }

    private void createRemind (String id, String title, String message, long delay) {
        if (remindTime > 0) {
            Intent alarmIntent =  new Intent(context, NotificationReceiver.class);
            alarmIntent.putExtra("noteId", id);
            alarmIntent.putExtra("title", title);
            alarmIntent.putExtra("message", message);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            }
            else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
            }
        }
    }

    interface ReminderListener {
        public EditText getDescription ();
        public String getNoteId ();
    }
}
