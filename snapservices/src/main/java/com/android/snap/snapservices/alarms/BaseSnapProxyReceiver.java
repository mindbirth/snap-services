package com.android.snap.snapservices.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.snap.snapservices.logger.SnapLogger;

/**
 * Proxy class that will receive all alarms set and deliver them to the
 * correct Snap Service recipient.
 */
public abstract class BaseSnapProxyReceiver extends BroadcastReceiver {

    public static final String EXTRA_SNAP_INTENT = "com.android.snap.extra.SNAP_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        SnapLogger.v("Received proxy request for intent: " + intent);
        SnapLogger.d("Forwarding as snap Service.");

        startSnapService(SnapAlarmManager.convertIntentToSnapIntent(intent));
    }

    /**
     * Starts the snap service in the correct way. The class implementing this will know if it
     * should launch the snap services in the main process or in the secondary.
     * @param intent The Snap Intent. No need to perform any other change to it.
     */
    protected abstract void startSnapService(Intent intent);
}
