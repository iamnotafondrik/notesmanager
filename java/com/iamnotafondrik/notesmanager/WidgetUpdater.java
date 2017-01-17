package com.iamnotafondrik.notesmanager;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * Created by iamnotafondrik on 17.01.2017.
 */

public class WidgetUpdater {

    public static void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetNotes.class));
        if (appWidgetIds.length > 0) {
            new WidgetNotes().onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
}
