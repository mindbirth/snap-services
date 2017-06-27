package com.snap.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.snap.snapservices.SnapService;
import com.snap.android.R;

import java.util.Random;

public abstract class BaseSnapService extends SnapService {

    public static final String START_FOREGROUND_ACTION = "com.snapservice.action.SOME_ACTION_IN_FOREGROUND";

    public BaseSnapService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        log("Intent received: " + intent);
        try {

            switch (intent.getAction()) {
                case START_FOREGROUND_ACTION:
                    startForeground(100, buildNotification(getApplicationContext(), "Title", "Content",
                            "ContentInfo", R.drawable.ic_launcher, "Super expanded text!"));
                    break;
            }

            int i = new Random().nextInt(10);
            log("Sleeping for " + i + " seconds...");
            Thread.sleep(i * 1000);
            log("Sleeping for " + i + " seconds... DONE!!!");

        /*
        Intent eggIntent = new Intent(getApplicationContext(), EggService.class);
        eggIntent.setAction("com.snapservice.action.EGG");
        eggIntent.putExtra("EXTRA_KEY", "some_key");

        //You can start a service doing this:
        SnapServicesContext.startService(eggIntent);

        Or doing this:
        startService(eggIntent);

        This is possible because SnapService extends from SnapContextWrapper which
        implements the Snap way of launching services.
        */

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Notification buildNotification(Context context, String contentTitle, String contentText,
                                                String contentInfo, int smallIcon, String expandedText) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // The id of the channel.
            String id = "my_channel_01";
            // The user-visible name of the channel.
            CharSequence name = "channel_name";
            // The user-visible description of the channel.
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "my_channel_01")
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_SOUND
                        | Notification.DEFAULT_LIGHTS
                        | Notification.FLAG_AUTO_CANCEL)
                .setContentInfo(contentInfo);

        // Make notification expandable.
        if (!TextUtils.isEmpty(expandedText)) {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(expandedText));
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Bitmap largeIcon = ((BitmapDrawable) ContextCompat.getDrawable(context,
                        R.drawable.ic_launcher)).getBitmap();

            builder.setLargeIcon(largeIcon);
            if (smallIcon > 0) {
                builder.setSmallIcon(smallIcon);
            }
        } else {
            builder.setSmallIcon(R.drawable.ic_launcher).setColor(ContextCompat.getColor(
                    context, R.color.colorAccent));
        }

        return builder.build();
    }

    protected void log(String message) {
        Log.d("SnapApplication", getLogTag() + ": " + message);
    }

    protected abstract String getLogTag();
}
