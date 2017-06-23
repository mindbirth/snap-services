package com.android.snap.snapservices.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.android.snap.snapservices.SnapService;
import com.android.snap.snapservices.alarms.SnapAlarmManager;
import com.android.snap.snapservices.logger.SnapLogger;

/**
 * Class that manages how a notification is created for SnapServices.
 *
 * <p>The notification actions are created like you normally would for any other Snap Service.</p>
 */
public class SnapNotification extends NotificationCompat {

    public static class Builder extends NotificationCompat.Builder {

        public Builder(Context context) {
            super(context);
        }

        @Override
        public NotificationCompat.Builder addAction(Action action) {
            throw new RuntimeException("Method not allowed. Use addAction(int, CharSequence, PendingIntent) instead");
        }

        @Override
        public NotificationCompat.Builder addAction(int icon, CharSequence title, PendingIntent pendingIntent) {
            return super.addAction(icon, title, checkAndConvertPendingIntent(pendingIntent));
        }

        @Override
        public NotificationCompat.Builder setDeleteIntent(PendingIntent pendingIntent) {
            return super.setDeleteIntent(checkAndConvertPendingIntent(pendingIntent));
        }

        @Override
        public NotificationCompat.Builder setContentIntent(PendingIntent pendingIntent) {
            return super.setContentIntent(checkAndConvertPendingIntent(pendingIntent));
        }

        private PendingIntent checkAndConvertPendingIntent(PendingIntent pendingIntent) {
            if (pendingIntent == null) return null;

            boolean isSnapService = false;
            Intent originalIntent = pendingIntent.getIntent();
            try {
                //checks if this intent is meant to a SnapService.
                isSnapService = SnapService.class.isAssignableFrom(Class.forName(originalIntent.getComponent().getClassName()));
            } catch (ClassNotFoundException ex) {
                SnapLogger.e("Error checking if class inside intent extends from SnapService", ex);
            }

            if (isSnapService) {
                Intent proxyIntent = SnapAlarmManager.convertSnapIntentToIntent(mContext, originalIntent);
                return PendingIntent.getService(mContext, 0, proxyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            return pendingIntent;
        }
    }
}
