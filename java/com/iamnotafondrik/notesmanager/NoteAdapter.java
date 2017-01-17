package com.iamnotafondrik.notesmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by iamnotafondrik on 29.10.2016.
 */

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private ArrayList<Note> noteArrayList = new ArrayList<Note>();
    private Context context;
    private int lastPosition = -1;

    public NoteAdapter(ArrayList<Note> noteArrayList, Context context) {
        this.noteArrayList = noteArrayList;
        this.context = context;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_card, parent, false);
        return new NoteViewHolder(view, context, noteArrayList);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = noteArrayList.get(position);
        holder.description.setText(note.getDescription());
        holder.date.setText(note.getDate());
        holder.noteId = note.getNoteId();
        holder.group = note.getGroup();
        holder.pinned = note.getPinned();

        switch (holder.group) {
            case 0:
                holder.groups.setImageResource(R.drawable.ic_group_regular);
                break;
            case 1:
                holder.groups.setImageResource(R.drawable.ic_group_home);
                break;
            case 2:
                holder.groups.setImageResource(R.drawable.ic_group_work);
                break;
            case 3:
                holder.groups.setImageResource(R.drawable.ic_group_important);
                break;
        }

        if (holder.pinned.equals("YES")) {
            holder.pin.setVisibility(View.VISIBLE);
        }
        if (holder.pinned.equals("NO")) {
            holder.pin.setVisibility(View.INVISIBLE);
        }

        //setAnimation(holder.noteCardLayout, position);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.scroll_to_up);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return noteArrayList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView description, date;
        ImageView groups, pin;
        String noteId, pinned;
        int group;
        ArrayList<Note> noteArrayList = new ArrayList<Note>();
        Context context;

        LinearLayout noteCardLayout;

        public NoteViewHolder(View view, Context context, ArrayList<Note> noteArrayList) {
            super(view);
            this.noteArrayList = noteArrayList;
            this.context = context;
            description = (TextView) view.findViewById(R.id.noteDescription);
            date = (TextView) view.findViewById(R.id.noteDate);
            groups = (ImageView) view.findViewById(R.id.noteIcon);
            pin = (ImageView) view.findViewById(R.id.notePinned);
            noteCardLayout = (LinearLayout) view.findViewById(R.id.note_card_layout);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Note note = this.noteArrayList.get(position);
            Intent intent = new Intent(this.context, FullNoteActivity.class);
            intent.putExtra("noteId", note.getNoteId());
            intent.putExtra("request", 1);
            this.context.startActivity(intent);
        }
    }
}
