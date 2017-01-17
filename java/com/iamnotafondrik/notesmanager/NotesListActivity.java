package com.iamnotafondrik.notesmanager;

import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//TODO LINKS http://a-student.github.io/SvgToVectorDrawableConverter.Web/ http://editor.method.ac/

public class NotesListActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Note> noteArrayList;
    DBHelper dbHelper;
    String[] noteId, description, date, pinned;
    int[] group;
    TextView noNoteText;

    //AUTH
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    FirebaseUser user;
    //END AUTH

    //FirebaseAuthHelper firebaseAuthHelper;
    //FirebaseStorageHelper firebaseStorageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(this);
        }

        noNoteText = (TextView) findViewById(R.id.noNoteText);

        SPHelper.sharedPreferenceInit(this);

        if (user == null) {
            GoogleAuth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFormDB(SPHelper.getStringPreference(SPHelper.PREFS_NOTE_MANAGER_LAST_SORT));
    }

    private  void loadFormDB (String column) {
        dbHelper = new DBHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor;
        if (column == null) {
            column = DBHelper.KEY_ID;
        }
        /*cursor = database.rawQuery("SELECT * FROM " + DBHelper.TABLE_NOTES + " ORDER BY "
                + column + " DESC", null);*/
        cursor = database.query(DBHelper.TABLE_NOTES, null, null, null, null, null, column + " DESC");
        noteArrayList = new ArrayList<Note>();
        noteId = new String[cursor.getCount()];
        //title = new String[cursor.getCount()];
        description = new String[cursor.getCount()];
        date = new String[cursor.getCount()];
        group = new int[cursor.getCount()];
        pinned = new String[cursor.getCount()];

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int descriptionIndex = cursor.getColumnIndex(DBHelper.KEY_DESCRIPTION);
            int dateIndex = cursor.getColumnIndex(DBHelper.KEY_DATE);
            int groupIndex = cursor.getColumnIndex(DBHelper.KEY_GROUP);
            int pinnedIndex = cursor.getColumnIndex(DBHelper.KEY_PINNED);
            do {
                noteId[cursor.getPosition()] = cursor.getString(idIndex);
                description[cursor.getPosition()] = cursor.getString(descriptionIndex);
                date[cursor.getPosition()] = cursor.getString(dateIndex);
                group[cursor.getPosition()] = cursor.getInt(groupIndex);
                pinned[cursor.getPosition()] = cursor.getString(pinnedIndex);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        int count = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm, dd MMM yyyy", Locale.getDefault());
        for (String tmpDescription : description) {
            String dateFormated = simpleDateFormat.format(new Date(Long.parseLong(date[count])));
            Note note = new Note(noteId[count], tmpDescription, dateFormated, group[count], pinned[count]);
            note.getNoteFullInformationToLog();
            count++;
            noteArrayList.add(note);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        adapter = new NoteAdapter(noteArrayList, this);
        recyclerView.setAdapter(adapter);
        //registerForContextMenu(recyclerView);

        if (noteArrayList.isEmpty()) {
            noNoteText.setVisibility(View.VISIBLE);
        } else {
            noNoteText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notes_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        //SORTING
        if (id == R.id.sorting_note_by_date) {
            loadFormDB(DBHelper.KEY_DATE);
            SPHelper.setStringPreference(SPHelper.PREFS_NOTE_MANAGER_LAST_SORT, DBHelper.KEY_DATE);
        }
        if (id == R.id.sorting_note_by_group) {
            loadFormDB(DBHelper.KEY_GROUP);
            SPHelper.setStringPreference(SPHelper.PREFS_NOTE_MANAGER_LAST_SORT, DBHelper.KEY_GROUP);
        }
        if (id == R.id.sorting_note_by_order) {
            loadFormDB(DBHelper.KEY_ID);
            SPHelper.setStringPreference(SPHelper.PREFS_NOTE_MANAGER_LAST_SORT, DBHelper.KEY_ID);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.fab:
                addNewNote ();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (user != null) {
            dataBase(1);
        }
        mAuth.removeAuthStateListener(mAuthListener);
    }

    //AUTH
    public void GoogleAuth () {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (!SPHelper.getBoolPreference(SPHelper.PREFS_NOTE_MANAGER_DO_BACKUP)) {
                        dataBase(0);
                    }
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    if (!SPHelper.getBoolPreference(SPHelper.PREFS_NOTE_MANAGER_FIRST_LAUNCH)) {
                        signIn();
                    }
                }
            }
        };
    }

    // start stop

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        SPHelper.setBoolPreference(SPHelper.PREFS_NOTE_MANAGER_USER_SINGED, true);

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(NotesListActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        SPHelper.setBoolPreference(SPHelper.PREFS_NOTE_MANAGER_FIRST_LAUNCH, true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, R.string.gps_error, Toast.LENGTH_SHORT).show();
    }
    //END AUTH

    // //data/data/com.iamnotafondrik.notesmanager/databases/NoteManager (.db)
    private void dataBase (int request) {
        // CHECK USER BEFORE USE THIS METHOD
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.setMaxDownloadRetryTimeMillis(10000);
        storage.setMaxUploadRetryTimeMillis(10000);
        String filePath = String.format("%s", getDatabasePath (DBHelper.DATABASE_NAME).getAbsolutePath());
        Log.d(TAG, "DBPath - " + filePath);
        Uri file = Uri.fromFile(new File(filePath));
        StorageReference storageReference = storage.getReference().child("user").child(user.getUid()).child(file.getLastPathSegment());

        // 1-save 0-read
        if (request == 0) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.wait));
            progressDialog.setMessage(getString(R.string.wait_message));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();

            Log.d(TAG, "EXISTING - " + storageReference.getFile(file).getSnapshot());
            storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "BACKUP DOWNLOAD SUCCESS");
                    loadFormDB(SPHelper.getStringPreference(SPHelper.PREFS_NOTE_MANAGER_LAST_SORT));
                    progressDialog.cancel();
                    backUpToast(R.string.backup_download_succsses);
                    WidgetUpdater.updateWidget(getApplicationContext());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "BACKUP DOWNLOAD FAILURE. " + exception.getMessage());
                    progressDialog.cancel();
                    backUpToast (R.string.backup_download_failed);
                }
            });
            SPHelper.setBoolPreference(SPHelper.PREFS_NOTE_MANAGER_DO_BACKUP, true);
        }
        if (request == 1) {
            storageReference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "BACKUP UPLOAD SUCCESS");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "BACKUP UPLOAD FAILURE. " + e.getMessage());
                }
            });
        }
    }

    private void addNewNote () {
        Intent intentNewNote = new Intent(this, FullNoteActivity.class);
        intentNewNote.putExtra("request", 0);
        startActivity(intentNewNote);
        overridePendingTransition(R.anim.slide_in_right, R.anim.alpha_out);
    }

    private void backUpToast (int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void backUpToast (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /*@Override
    public void updateInformarion() {

    }

    @Override
    public void onBackDataRecoverySuccess() {
        loadFormDB(SPHelper.getStringPreference(SPHelper.PREFS_NOTE_MANAGER_LAST_SORT));
        WidgetUpdater.updateWidget(this);
    }*/
}
