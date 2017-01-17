package com.iamnotafondrik.notesmanager;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by iamnotafondrik on 04.11.2016.
 */

public class SPHelper {

    public static final String PREFS_NOTE_MANAGER = "PrefsNoteManager";
    // KEYS
    public static final String PREFS_NOTE_MANAGER_LAST_SORT = "PrefsNoteManagerLastSort";
    public static final String PREFS_NOTE_MANAGER_FIRST_LAUNCH = "PrefsNoteManagerFirstLaunch";
    public static final String PREFS_NOTE_MANAGER_USER_SINGED = "PrefsNoteManagerUserSinged";
    public static final String PREFS_NOTE_MANAGER_USER_NAME = "PrefsNoteManagerUserName";
    public static final String PREFS_NOTE_MANAGER_USER_EMAIL = "PrefsNoteManagerUserEmail";
    public static final String PREFS_NOTE_MANAGER_DO_BACKUP = "PrefsNoteManagerDoBackup";
    // OTHER KEYS NOT REALISED

    private static SharedPreferences sharedPreferences = null;
    private static SharedPreferences.Editor editor = null;
    private static Context context = null;

    public static void sharedPreferenceInit () {
        sharedPreferences = context.getSharedPreferences(PREFS_NOTE_MANAGER, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static void sharedPreferenceInit (Context cont) {
        context = cont;
    }

    public static void setStringPreference (String key, String value) {
        if (sharedPreferences == null) {
            sharedPreferenceInit();
        }
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringPreference (String key) {
        if (sharedPreferences == null) {
            sharedPreferenceInit();
        }
        if (key.equals(PREFS_NOTE_MANAGER_LAST_SORT)) {
            return sharedPreferences.getString(key, DBHelper.KEY_ID);
        } else {
            return sharedPreferences.getString(key, "");
        }
    }

    public void setIntPreference (int i) {

    }

    public int getIntPreference () {
        return 0;
    }

    public static void setBoolPreference (String key, Boolean value) {
        if (sharedPreferences == null) {
            sharedPreferenceInit();
        }
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBoolPreference (String key) {
        if (sharedPreferences == null) {
            sharedPreferenceInit();
        }
        return sharedPreferences.getBoolean(key, false);
    }
}
