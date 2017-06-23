package com.snap.android.services;

public class MixService extends BaseSnapService {
    public static final String LOG_TAG = "MixService";

    public MixService() {
        super(LOG_TAG);
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }
}
