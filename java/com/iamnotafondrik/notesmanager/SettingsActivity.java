/**/
package com.iamnotafondrik.notesmanager;

import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

import java.io.File;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    //AUTH
    private static final String TAG = "GoogleActivity_Settings";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    FirebaseUser user;

    TextView nameText;
    TextView emailText;
    TextView authText;

    //FirebaseAuthHelper firebaseAuthHelper;
    //FirebaseStorageHelper firebaseStorageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        nameText = (TextView) findViewById(R.id.settings_account_name);
        emailText = (TextView) findViewById(R.id.settings_account_email);
        authText = (TextView) findViewById(R.id.settings_auth);

        if (authText != null) {
            authText.setOnClickListener(this);
        }

        SPHelper.sharedPreferenceInit(this);

        if (user == null) {
            GoogleAuth();
        }
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.alpha_out);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_auth:
                if (user == null) {
                    signIn();
                } else {
                    signOut();
                }
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
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, R.string.gps_error, Toast.LENGTH_SHORT).show();
    }

    //AUTH
    public void GoogleAuth() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
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
                        loadBackUp();
                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //signIn();
                }
                updateInformarion();
            }
        };
    }


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
                        loadBackUp();

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SettingsActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.log_out);
        builder.setMessage(R.string.delete_note_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                user.unlink(user.getUid());
                try {
                    FirebaseAuth.getInstance().signOut();
                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    }
                } catch (Exception e) {
                    //
                }
                user = null;
                updateInformarion();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void loadBackUp() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.setMaxDownloadRetryTimeMillis(10000);
        storage.setMaxUploadRetryTimeMillis(10000);
        String filePath = String.format("%s", getDatabasePath(DBHelper.DATABASE_NAME).getAbsolutePath());
        Log.d(TAG, "DBPath - " + filePath);
        Uri file = Uri.fromFile(new File(filePath));
        StorageReference storageReference = storage.getReference().child("user").child(user.getUid()).child(file.getLastPathSegment());

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
                progressDialog.cancel();
                backUpToast(R.string.backup_download_succsses);
                WidgetUpdater.updateWidget(getApplicationContext());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "BACKUP DOWNLOAD FAILURE. " + exception.getMessage());
                progressDialog.cancel();
                backUpToast(R.string.backup_download_failed);
            }
        });
        SPHelper.setBoolPreference(SPHelper.PREFS_NOTE_MANAGER_DO_BACKUP, true);
    }

    private void backUpToast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //@Override
    public void updateInformarion() {
        if (user != null) {
            nameText.setText(user.getDisplayName());
            emailText.setText(user.getEmail());
            authText.setText(R.string.log_out);
        } else {
            nameText.setText(R.string.n_name);
            emailText.setText(R.string.n_email);
            authText.setText(R.string.log_in);
        }
    }

    /*@Override
    public void onBackDataRecoverySuccess() {
        WidgetUpdater.updateWidget(this);
    }*/
}
