package com.android.snap.snapservices.alarms;

import android.content.Intent;

import com.android.snap.snapservices.SnapServicesContext;

/**
 * Proxy class that will receive all proxy request on the main process and deliver them to the
 * correct Snap Service recipient.
 */
public class SnapProxyReceiver extends BaseSnapProxyReceiver {

    @Override
    protected void startSnapService(Intent intent) {
        SnapServicesContext.startService(intent);
    }
}
