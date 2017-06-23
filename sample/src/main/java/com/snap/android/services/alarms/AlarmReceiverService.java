package com.snap.android.services.alarms;

import com.snap.android.services.BaseSnapService;

public class AlarmReceiverService extends BaseSnapService {
    public static final String LOG_TAG = "AlarmReceiverService";

    public AlarmReceiverService() {
        super(LOG_TAG);
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }
}
