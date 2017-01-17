package com.iamnotafondrik.notesmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

/**
 * Created by iamnotafondrik on 04.12.2016.
 */

public class GroupPicker extends Dialog implements View.OnClickListener {

    private GroupPickerListener groupPickerListener;

    public GroupPicker(Activity activity) {
        super(activity);
        groupPickerListener = (GroupPickerListener) activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.group_pick_dialog);
        CardView regular = (CardView) findViewById(R.id.group_regular_card);
        CardView home = (CardView) findViewById(R.id.group_home_card);
        CardView work = (CardView) findViewById(R.id.group_work_card);
        CardView important = (CardView) findViewById(R.id.group_important_card);
        regular.setOnClickListener(this);
        home.setOnClickListener(this);
        work.setOnClickListener(this);
        important.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.group_regular_card:
                groupPickerListener.setGroup(0);
                break;

            case R.id.group_home_card:
                groupPickerListener.setGroup(1);
                break;
            case R.id.group_work_card:
                groupPickerListener.setGroup(2);
                break;
            case R.id.group_important_card:
                groupPickerListener.setGroup(3);
                break;
        }
        dismiss();
    }

    interface GroupPickerListener {
        public void setGroup(int id);
    }
}
