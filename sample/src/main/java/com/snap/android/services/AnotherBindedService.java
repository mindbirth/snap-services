package com.snap.android.services;

import android.content.Intent;

import com.android.snap.snapservices.binder.ISnapBinder;
import com.android.snap.snapservices.binder.SnapBinder;
import com.snap.android.SnapApplication;

public class AnotherBindedService extends BaseSnapService {

    public AnotherBindedService() {
        super("AnotherBindedService");
    }

    @Override
    protected String getLogTag() {
        return "AnotherBindedService";
    }

    public void doSomethingElse() {
        SnapApplication.log("Hello! I'm another binded service and I'm doing something else!");
    }

    private final ISnapBinder mBinder = new LocalBinder();

    public class LocalBinder extends SnapBinder {
        public AnotherBindedService getService() {
            return AnotherBindedService.this;
        }
    }

    @Override
    public ISnapBinder onBind(Intent intent) {
        return mBinder;
    }
}
