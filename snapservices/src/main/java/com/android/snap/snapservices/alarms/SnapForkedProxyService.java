package com.android.snap.snapservices.alarms;

import android.content.Intent;

import com.android.snap.snapservices.SnapServicesContext;

/**
 * Proxy class that will receive all proxy request on a separate process and deliver them to the
 * correct Snap Service recipient.
 */
public class SnapForkedProxyService extends BaseSnapProxyService {
    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public SnapForkedProxyService() {
        super("SnapForkedProxyService");
    }

    @Override
    protected void startSnapService(Intent intent) {
        SnapServicesContext.startServiceOnOtherProcess(intent);
    }
}
