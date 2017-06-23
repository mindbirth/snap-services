package com.snap.android;

import android.app.Application;
import android.util.Log;

import com.android.snap.snapservices.SnapServicesContext;
import com.android.snap.snapservices.configuration.SnapConfigOptions;
import com.android.snap.snapservices.logger.SnapLogger;

public class SnapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate!!!");

        SnapServicesContext.startup(getApplicationContext(), new SnapConfigOptions.Builder()
                .killSeparateProcess(true).setLogLevel(SnapLogger.VERBOSE).build());
        log("SnapServices version: " + SnapServicesContext.getVersion());
    }

    public static void log(String message) {
        Log.d("SnapApplication", "[" + android.os.Process.myPid() + "] SnapApplication: " + message);
    }
}
