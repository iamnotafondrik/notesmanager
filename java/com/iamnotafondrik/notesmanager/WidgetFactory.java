package com.iamnotafondrik.notesmanager;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by iamnotafondrik on 06.01.2017.
 */

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    ArrayList<String> data;
    ArrayList<String> noteId;
    Context context;
    int widgetID;

    WidgetFactory(Context context, Intent intent) {
        this.context = context;
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        data = new ArrayList<String>();
        noteId = new ArrayList<String>();
    }

    @Override
    public void onDataSetChanged() {
        data.clear();
        noteId.clear();

        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase database;
        database = dbHelper.getReadableDatabase();

        Cursor cursor = database.query(DBHelper.TABLE_NOTES, null, DBHelper.KEY_PINNED + " = ?", new String[]{"YES"}, null, null, DBHelper.KEY_ID + " DESC");
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                data.add(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION)));
                noteId.add(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_ID)));
            } while (cursor.moveToNext());
        } else {
            data.add(context.getString(R.string.widget_no_one_note));
            //TODO
        }

        cursor.close();
        database.close();
    }

    @Override
    public void onDestroy() {
        try {
            data.clear();
            noteId.clear();
        } catch (Exception e) {
            Log.d("Widget_", e.getMessage());
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        remoteViews.setTextViewText(R.id.widget_item_text, data.get(position));
        remoteViews.setTextColor(R.id.widget_item_text, Color.BLACK);
        Intent clickIntent = new Intent();
        clickIntent.putExtra(WidgetNotes.ITEM_POSITION, position);
        if (noteId.size() != 0) {
            clickIntent.putExtra("request", 1);
            clickIntent.putExtra("noteId", noteId.get(position));
            //TODO
        }
        remoteViews.setOnClickFillInIntent(R.id.widget_item_text, clickIntent);
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
