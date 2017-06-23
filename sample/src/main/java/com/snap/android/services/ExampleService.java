package com.snap.android.services;

import android.content.Intent;

import com.android.snap.snapservices.SnapService;

/**
 * Created by pedro on 23/06/2017.
 */

public class ExampleService extends SnapService {

    public ExampleService() {
        super("ExampleService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
