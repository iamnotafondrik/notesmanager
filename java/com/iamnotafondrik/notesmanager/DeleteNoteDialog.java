package com.iamnotafondrik.notesmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

/**
 * Created by iamnotafondrik on 10.12.2016.
 */

public class DeleteNoteDialog {

    private DeleteDialogListener deleteDialogListener;
    private AlertDialog.Builder builder;

    public DeleteNoteDialog(Activity activity) {
        deleteDialogListener = (DeleteDialogListener) activity;
        builder = new AlertDialog.Builder(activity);
    }

    public void createDeleteDialog () {
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.delete_note_message);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteDialogListener.deleteNote();
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

    interface DeleteDialogListener {
        public void deleteNote ();
    }
}
