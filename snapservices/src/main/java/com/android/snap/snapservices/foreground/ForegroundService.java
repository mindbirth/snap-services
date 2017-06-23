package com.android.snap.snapservices.foreground;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.snap.snapservices.logger.SnapLogger;

public abstract class ForegroundService extends Service {

    public static final String NOTIFICATION_ID_EXTRA = "com.android.snap.foreground.extra.notification_id";
    public static final String NOTIFICATION_EXTRA = "com.android.snap.foreground.extra.notification";

    public static final String ACTION_START_FOREGROUND = "com.android.snap.foreground.action.START_FOREGROUND";
    public static final String ACTION_STOP_FOREGROUND = "com.android.snap.foreground.action.STOP_FOREGROUND";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent, startId);
        return START_NOT_STICKY;
    }

    private void onHandleIntent(Intent intent, int startId) {
        log("onHandleIntent called with [intent=" + intent + ";startId=" + startId + "]");

        switch (intent.getAction()) {
            case ACTION_START_FOREGROUND:
                int notificationId = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 0);
                if (notificationId == 0) {
                    log("NotificationId is non-existent. Do nothing.");
                    return;
                }

                Notification notification = intent.getParcelableExtra(NOTIFICATION_EXTRA);
                if (notification == null) {
                    log("Notification is null. Do nothing.");
                    return;
                }

                log("Starting service in foreground.");
                startForeground(notificationId, notification);
                break;
            case ACTION_STOP_FOREGROUND:
                stopForeground(true);
                stopSelf();
                break;
        }
    }

    abstract String getServiceName();

    protected void log(String message) {
        SnapLogger.v("[foregroundService=" + getServiceName() + "] " + message);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
