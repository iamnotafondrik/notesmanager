package com.iamnotafondrik.notesmanager;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by iamnotafondrik on 06.01.2017.
 */

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetFactory(getApplicationContext(), intent);
    }
}
