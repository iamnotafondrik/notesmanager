package com.iamnotafondrik.notesmanager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;


public class WidgetNotes extends AppWidgetProvider {

    final String ACTION_NOTES_LIST_ACTIVITY = "com.iamnotafondrik.notesmanager.noteslistactivity";
    final String ACTION_FULL_NOTE_ACTIVITY = "com.iamnotafondrik.notesmanager.fullnoteactivity";
    final static String ITEM_POSITION = "item_position";

    void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notes_widget);

        setList(remoteViews, context, appWidgetId);

        setTitleClick(remoteViews, context, appWidgetId);
        setAddClick(remoteViews, context, appWidgetId);
        setListClick(remoteViews, context, appWidgetId);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    void setList(RemoteViews remoteViews, Context context, int appWidgetId) {
        Intent adapter = new Intent(context, WidgetService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME));
        adapter.setData(data);
        remoteViews.setRemoteAdapter(R.id.widget_list_view, adapter);
    }

    void setTitleClick(RemoteViews remoteViews, Context context, int appWidgetId) {
        Intent listClickIntent = new Intent(context, NotesListActivity.class);
        listClickIntent.setAction(ACTION_NOTES_LIST_ACTIVITY);
        listClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent listClickPIntent = PendingIntent.getActivity(context, 0, listClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_edit, listClickPIntent);
    }

    void setAddClick(RemoteViews remoteViews, Context context, int appWidgetId) {
        Intent listClickIntent = new Intent(context, FullNoteActivity.class);
        listClickIntent.setAction(ACTION_FULL_NOTE_ACTIVITY);
        listClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent listClickPIntent = PendingIntent.getActivity(context, 0, listClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_add, listClickPIntent);
    }

    void setListClick(RemoteViews remoteViews, Context context, int appWidgetId) {
        Intent listClickIntent = new Intent(context, FullNoteActivity.class);
        listClickIntent.setAction(ACTION_FULL_NOTE_ACTIVITY);
        listClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent listClickPIntent = PendingIntent.getActivity(context, 0, listClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_list_view, listClickPIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equalsIgnoreCase(ACTION_NOTES_LIST_ACTIVITY)) {
            context.startActivity (intent);
        }
        if (intent.getAction().equalsIgnoreCase(ACTION_FULL_NOTE_ACTIVITY)) {
            context.startActivity (intent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notes_widget);
        remoteViews.setTextColor(R.id.widget_edit, Color.WHITE);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}

