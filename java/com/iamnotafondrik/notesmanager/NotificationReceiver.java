package com.iamnotafondrik.notesmanager;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by iamnotafondrik on 31.12.2016.
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent actionIntent = new Intent(context, NotesListActivity.class);
        actionIntent.putExtra("noteId", intent.getStringExtra("noteId"));
        actionIntent.putExtra("request", 1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, actionIntent, 0);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_create)
                .setContentTitle(intent.getStringExtra("title"))
                .setContentText(intent.getStringExtra("message"))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
        Log.d("NTFC_", "Remind triggered");
    }
}
