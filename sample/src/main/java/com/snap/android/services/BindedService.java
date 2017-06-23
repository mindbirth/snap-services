package com.snap.android.services;

import android.content.Intent;

import com.android.snap.snapservices.binder.ISnapBinder;
import com.android.snap.snapservices.binder.SnapBinder;
import com.snap.android.SnapApplication;

public class BindedService extends BaseSnapService {

    public BindedService() {
        super("BindedService");
    }

    @Override
    protected String getLogTag() {
        return "BindedService";
    }

    public void doSomething() {
        SnapApplication.log("Hello! I'm a binded service and I'm doing something!");
    }

    private final ISnapBinder mBinder = new LocalBinder();

    public class LocalBinder extends SnapBinder {
        public BindedService getService() {
            return BindedService.this;
        }
    }

    @Override
    public ISnapBinder onBind(Intent intent) {
        return mBinder;
    }
}
