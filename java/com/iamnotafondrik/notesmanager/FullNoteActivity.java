package com.iamnotafondrik.notesmanager;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.actions.NoteIntents;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FullNoteActivity extends AppCompatActivity implements View.OnClickListener, GroupPicker.GroupPickerListener,
        DeleteNoteDialog.DeleteDialogListener, Reminders.ReminderListener {

    EditText description;
    ImageView groups;
    Spinner remindSpinner;
    Switch switcher;

    String noteId, oldDescription, pinned, oldPinned;
    // REQUEST 0 - new note, 1 - edit note
    int groupId, oldGroupId, request;

    SQLiteDatabase database;
    ContentValues contentValues;
    Reminders reminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarFullNote);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        description = (EditText) findViewById(R.id.fullNoteDescriptionEditText);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        groups = (ImageView) findViewById(R.id.fullNoteIcon);
        remindSpinner = (Spinner) findViewById(R.id.remind_spinner);
        switcher = (Switch) findViewById(R.id.switcher);
        groups.setOnClickListener(this);
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pinned = "YES";
                } else {
                    pinned = "NO";
                }

                if (!description.getText().toString().equals("")) {
                    saveNote();
                    updateWidget();
                }
            }
        });

        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getReadableDatabase();
        contentValues = new ContentValues();

        request = getIntent().getIntExtra("request", 0);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (request == 0) {
            contentValues.put(DBHelper.KEY_DESCRIPTION, "");
            contentValues.put(DBHelper.KEY_DATE, "");
            contentValues.put(DBHelper.KEY_GROUP, 0);
            contentValues.put(DBHelper.KEY_PINNED, "NO");
            database.insert(DBHelper.TABLE_NOTES, null, contentValues);

            Cursor cursor = database.query(DBHelper.TABLE_NOTES, null, null, null, null, null, null);
            cursor.moveToLast();
            noteId = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_ID));
            cursor.close();

            setGroup(0);
            pinned = "NO";
            oldPinned = "NO";
            switcher.setChecked(false);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
        }

        if (request == 1) {
            noteId = getIntent().getStringExtra("noteId");
            Cursor cursor = database.query(DBHelper.TABLE_NOTES, null, "_id = ?", new String[] {noteId}, null, null, null);
            cursor.moveToFirst();

            description.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION)));
            groupId = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_GROUP));
            pinned = cursor.getString(cursor.getColumnIndex(DBHelper.KEY_PINNED));
            cursor.close();
            oldDescription = description.getText().toString();
            oldGroupId = groupId;
            oldPinned = pinned;
            setGroup(groupId);

            if (pinned.equals("YES")) {
                switcher.setChecked(true);
            }
            if (pinned.equals("NO")) {
                switcher.setChecked(false);
            }

            imm.hideSoftInputFromWindow(description.getWindowToken(), 0);

        }

        reminders = new Reminders(remindSpinner, this);
        reminders.configSpinner();

        //GET INTENT
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) || NoteIntents.ACTION_CREATE_NOTE.equals(action)) {
            if (type != null) {
                if ("text/plain".equals(type)) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (sharedText != null) {
                        description.setText(sharedText);
                    }
                }
            }
        }

        //Log.d ("Note_", "NOTE INFO: " + noteId + ", " + description.getText().toString() + ", " + groupId);
    }



    @Override
    protected void onPause() {
        saveNote();
        updateWidget ();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.alpha_out);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_full_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share_note) {
            if (!description.getText().toString().equals("")) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s", description.getText().toString()));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            } else {
                Toast.makeText(this, R.string.cant_share_empty_note, Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.action_delete_note) {
            DeleteNoteDialog deleteNoteDialog = new DeleteNoteDialog(this);
            deleteNoteDialog.createDeleteDialog ();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote () {
        if (!description.getText().toString().equals(oldDescription) || groupId != oldGroupId || !pinned.equals(oldPinned)) {
            if (description.getText().toString().equals("")) {
                database.delete(DBHelper.TABLE_NOTES, "_id = " + noteId, null);
            } else {

                contentValues.put(DBHelper.KEY_DESCRIPTION, description.getText().toString());

                contentValues.put(DBHelper.KEY_DATE, String.valueOf(System.currentTimeMillis()));

                contentValues.put(DBHelper.KEY_GROUP, groupId);
                contentValues.put(DBHelper.KEY_PINNED, pinned);
                database.update(DBHelper.TABLE_NOTES, contentValues, "_id = ?", new String[] { noteId });
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fullNoteIcon:
                GroupPicker groupPicker = new GroupPicker(this);
                groupPicker.show();
                break;
        }
    }

    @Override
    public void setGroup (int id) {
        groupId = id;
        switch (id) {
            case 0:
                groups.setImageResource(R.drawable.ic_group_regular);
                break;
            case 1:
                groups.setImageResource(R.drawable.ic_group_home);
                break;
            case 2:
                groups.setImageResource(R.drawable.ic_group_work);
                break;
            case 3:
                groups.setImageResource(R.drawable.ic_group_important);
                break;
        }
    }

    @Override
    public void deleteNote () {
        database.delete(DBHelper.TABLE_NOTES, "_id = " + noteId, null);
        updateWidget ();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.alpha_out);
    }

    @Override
    public EditText getDescription () {
        return description;
    }

    @Override
    public String getNoteId () {
        return noteId;
    }

    void updateWidget () {
        if (!description.getText().toString().equals("")) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetNotes.class));
            if (appWidgetIds.length > 0) {
                new WidgetNotes().onUpdate(this, appWidgetManager, appWidgetIds);
            }
        }
    }
}
