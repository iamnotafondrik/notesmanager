package com.iamnotafondrik.notesmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by iamnotafondrik on 29.10.2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NoteManager";
    public static final String TABLE_NOTES = "Notes";

    public  static final  String KEY_ID = "_id";
    public  static final  String KEY_DESCRIPTION = "Description";
    public  static final  String KEY_DATE= "Date";
    public  static final  String KEY_GROUP= "Groups";
    public  static final  String KEY_PINNED= "Pinned"; // YES or NO

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NOTES + "(" + KEY_ID + " integer primary key,"
                + KEY_DESCRIPTION + " text," + KEY_DATE + " text," + KEY_GROUP + " integer," + KEY_PINNED + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NOTES);
        onCreate(db);
    }
}
