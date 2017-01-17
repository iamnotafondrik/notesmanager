package com.iamnotafondrik.notesmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

/**
 * Created by iamnotafondrik on 17.01.2017.
 */

public class FirebaseStorageHelper {

    Context context;
    FirebaseStorageListener firebaseStorageListener;
    FirebaseUser user;
    StorageReference storageReference;
    FirebaseStorage storage;
    Uri file;
    private static final String TAG = "GoogleActivity";

    public FirebaseStorageHelper(Activity activity) {
        this.context = activity.getApplicationContext();
        firebaseStorageListener = (FirebaseStorageListener) activity;
        storage = FirebaseStorage.getInstance();
        storage.setMaxDownloadRetryTimeMillis(10000);
        storage.setMaxUploadRetryTimeMillis(10000);
        String filePath = String.format("%s", context.getDatabasePath (DBHelper.DATABASE_NAME).getAbsolutePath());
        Log.d(TAG, "DBPath - " + filePath);
        file = Uri.fromFile(new File(filePath));
    }

    public void setUser (FirebaseUser user) {
        this.user = user;
    }

    private void storageInit () {
        storageReference = storage.getReference().child("user").child(this.user.getUid()).child(file.getLastPathSegment());
    }

    public void uploadDatabase () {
        storageInit ();
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

    public void downloadDatabase () {
        storageInit ();
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getString(R.string.wait));
        progressDialog.setMessage(context.getString(R.string.wait_message));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Log.d(TAG, "EXISTING - " + storageReference.getFile(file).getSnapshot());
        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "BACKUP DOWNLOAD SUCCESS");
                firebaseStorageListener.onBackDataRecoverySuccess();
                //TODO
                //loadFormDB(SPHelper.getStringPreference(SPHelper.PREFS_NOTE_MANAGER_LAST_SORT));
                //updateWidget();
                Toast.makeText(context, R.string.backup_download_succsses, Toast.LENGTH_LONG).show();
                progressDialog.cancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "BACKUP DOWNLOAD FAILURE. " + exception.getMessage());
                progressDialog.cancel();
                Toast.makeText(context, R.string.backup_download_failed, Toast.LENGTH_LONG).show();
            }
        });
        SPHelper.setBoolPreference(SPHelper.PREFS_NOTE_MANAGER_DO_BACKUP, true);
    }

    interface FirebaseStorageListener {
        public void onBackDataRecoverySuccess ();
    }
}
