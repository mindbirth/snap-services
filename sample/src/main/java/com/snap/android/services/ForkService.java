package com.snap.android.services;

public class ForkService extends BaseSnapService {
    public static final String LOG_TAG = "ForkService";

    public ForkService() {
        super(LOG_TAG);
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }
}
