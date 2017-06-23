package com.android.snap.snapservices.alarms;

import android.content.Intent;

import com.android.snap.snapservices.SnapServicesContext;

/**
 * Proxy class that will receive all proxy request on the main process and deliver them to the
 * correct Snap Service recipient.
 */
public class SnapProxyService extends BaseSnapProxyService {
    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public SnapProxyService() {
        super("SnapProxyService");
    }

    @Override
    protected void startSnapService(Intent intent) {
        SnapServicesContext.startService(intent);
    }
}
