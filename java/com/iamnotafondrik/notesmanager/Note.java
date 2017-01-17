package com.iamnotafondrik.notesmanager;

import android.util.Log;

/**
 * Created by iamnotafondrik on 29.10.2016.
 */

public class Note {

    private String noteId, description, date, pinned;
    private int group;

    public Note (String noteId, String description, String date, int group, String pinned) {
        this.setNoteId(noteId);
        this.setDescription(description);
        this.setDate(date);
        this.setGroup(group);
        this.setPinned(pinned);
    }

    public String getNoteId() { return noteId; }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public int getGroup() { return group; }

    public void setNoteId(String noteId) { this.noteId = noteId; }

    public void setDescription(String description) { this.description = description; }

    public void setDate(String date) { this.date = date; }

    public void setGroup(int group) { this.group = group; }

    public String getPinned() { return pinned; }

    public void setPinned(String pinned) { this.pinned = pinned; }

    public void getNoteFullInformationToLog() {
        String info = String.format("NOTE INFO: %s, %s, %s, %s, %s", noteId, description, date, group, pinned);
        Log.d("Note_", info);
    }
}
