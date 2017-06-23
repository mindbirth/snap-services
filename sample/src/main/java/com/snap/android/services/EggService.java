package com.snap.android.services;

public class EggService extends BaseSnapService {
    public static final String LOG_TAG = "EggService";

    public EggService() {
        super(LOG_TAG);
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }
}
