package com.android.snap.snapservices.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This class controls the alarms to launch Snap Services.
 *
 * <p>This manager was created to give Snap Services support to be launched via alarms.
 * Currently, you can launch an alarm by invoking {@link #setAlarm(Context, int, Intent, int, long)}
 * or {@link #setAlarmOnSeparateProcess(Context, int, Intent, int, long)}</p>
 *
 * <p>To extend the functionality of SnapServices, you're also able to launch a Snap Service on a separate process.
 * This might be useful to better control the different executions of your app.</p>
 *
 * <p>Note that, you're the one controlling where each Snap Service runs so you need to explicitly
 * decide where to set, verify and cancel the alarm (if in the normal process or on the separate process).</p>
 */
public class SnapAlarmManager {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            AlarmManager.RTC,
            AlarmManager.RTC_WAKEUP,
            AlarmManager.ELAPSED_REALTIME,
            AlarmManager.ELAPSED_REALTIME_WAKEUP
    })
    @interface AlarmType {}

    /**
     * Sets an alarm for the given Snap Service.
     * You should pass a different action for every alarm you want to track.
     *
     * <p>The common practice should be to have only one alarm per action. In case you want
     * to configure multiple alarms with the same action for the same service, consider configuring a new
     * alarm when receiving it.</p>
     *
     * @param context The application context.
     * @param alarmType The type of alarm. This needs to be one defined here {@link AlarmType}
     * @param serviceIntent The intent that will be delivered to the service when the alarm fires.
     * @param requestCode The unique request code for this alarm. Note that this needs to be
     *                    different per type of alarm.
     * @param interval The interval, depending on the {@link AlarmType} you define.
     */
    public static void setAlarm(Context context, @AlarmType int alarmType, Intent serviceIntent,
                                int requestCode, long interval) {
        internalSetAlarm(context, alarmType, convertSnapIntentToIntent(context, serviceIntent), requestCode, interval);
    }

    /**
     * Sets an alarm for the given Snap Service.
     * You should pass a different action for every alarm you want to track.
     *
     * <p>The common practice should be to have only one alarm per action. In case you want
     * to configure multiple alarms with the same action for the same service, consider configuring a new
     * alarm when receiving it.</p>
     *
     * @param context The application context.
     * @param alarmType The type of alarm. This needs to be one defined here {@link AlarmType}
     * @param serviceIntent The intent that will be delivered to the service when the alarm fires.
     * @param requestCode The unique request code for this alarm. Note that this needs to be
     *                    different per type of alarm.
     * @param interval The interval, depending on the {@link AlarmType} you define.
     */
    public static void setAlarmOnSeparateProcess(Context context, @AlarmType int alarmType, Intent serviceIntent,
                                int requestCode, long interval) {
        Intent realIntent = convertSnapIntentToIntent(context, serviceIntent);
        realIntent.setClass(context, SnapForkedProxyService.class);
        internalSetAlarm(context, alarmType, realIntent, requestCode, interval);
    }

    /**
     * Verifies if the respective alarm is currently in the Android Alarm Manager.
     * Note that verifying if an alarm is set is currently not possible in Android,
     * this method in fact verifies if the <b>PendingIntent</b> used to create the
     * alarm exists.
     *
     * @param context The application context.
     * @param serviceIntent The intent used to launch the alarm.
     * @param requestCode The request code set when the alarm was set.
     * @return True if the alarm is already set, false otherwise.
     * @see PendingIntent#FLAG_NO_CREATE
     */
    public static boolean isAlarmSet(Context context, Intent serviceIntent, int requestCode) {
        return internalIsAlarmSet(context, convertSnapIntentToIntent(context, serviceIntent), requestCode);
    }

    /**
     * Verifies if the respective alarm is currently in the Android Alarm Manager.
     * Note that verifying if an alarm is set is currently not possible in Android,
     * this method in fact verifies if the <b>PendingIntent</b> used to create the
     * alarm exists.
     *
     * @param context The application context.
     * @param serviceIntent The intent used to launch the alarm.
     * @param requestCode The request code set when the alarm was set.
     * @return True if the alarm is already set, false otherwise.
     * @see PendingIntent#FLAG_NO_CREATE
     */
    public static boolean isAlarmSetOnSeparateProcess(Context context, Intent serviceIntent, int requestCode) {
        Intent realIntent = convertSnapIntentToIntent(context, serviceIntent);
        realIntent.setClass(context, SnapForkedProxyService.class);
        return internalIsAlarmSet(context, realIntent, requestCode);
    }

    /**
     * Cancels an alarm. It also cancels the PendingIntent used to set the alarm, because
     * this way later on we can verify whether the alarm is set or not by verifying if the
     * PendingIntent exists or not.
     *
     * @param context The application context.
     * @param serviceIntent The intent used to launch the alarm.
     * @param requestCode The request code set when the alarm was set.
     */
    public static void cancelAlarm(Context context, Intent serviceIntent, int requestCode) {
        if (serviceIntent == null) {
            return;
        }

        Intent realIntent = convertSnapIntentToIntent(context, serviceIntent);
        internalCancelAlarm(context, realIntent, requestCode);
    }

    /**
     * Cancels an alarm. It also cancels the PendingIntent used to set the alarm, because
     * this way later on we can verify whether the alarm is set or not by verifying if the
     * PendingIntent exists or not.
     *
     * @param context The application context.
     * @param serviceIntent The intent used to launch the alarm.
     * @param requestCode The request code set when the alarm was set.
     */
    public static void cancelAlarmForSeparateProcess(Context context, Intent serviceIntent, int requestCode) {
        if (serviceIntent == null) {
            return;
        }

        Intent realIntent = convertSnapIntentToIntent(context, serviceIntent);
        realIntent.setClass(context, SnapForkedProxyService.class);
        internalCancelAlarm(context, realIntent, requestCode);
    }

    private static void internalSetAlarm(Context context, @AlarmType int alarmType, Intent serviceIntent,
                                         int requestCode, long interval) {
        PendingIntent s = PendingIntent.getService(context, requestCode, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(alarmType, interval, s);
    }

    /**
     * Internal method that checks if an alarm is set.
     *
     * @param serviceIntent The intent used to launch the alarm.
     * @param requestCode The request code set when the alarm was set.
     * @return True if the alarm is already set, false otherwise.
     * @see PendingIntent#FLAG_NO_CREATE
     */
    private static boolean internalIsAlarmSet(Context context, Intent serviceIntent, int requestCode) {
        return PendingIntent.getService(context, requestCode,
                serviceIntent, PendingIntent.FLAG_NO_CREATE) != null;
    }

    /**
     * Internal method that cancels an alarm.
     *
     * @param context The application context.
     * @param serviceIntent The intent used to launch the alarm.
     * @param requestCode The request code set when the alarm was set.
     */
    private static void internalCancelAlarm(Context context, Intent serviceIntent, int requestCode) {
        PendingIntent serviceScheduled = PendingIntent.getService(context,
                requestCode, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(serviceScheduled);
        serviceScheduled.cancel();
    }

    /**
     * Converts an Intent created to launch a Snap Service into an intent to lunch an actual Android Service
     * @param context The application context.
     * @param intent The intent to be converted into a real Android Service intent.
     * @return The converted intent.
     */
    public static Intent convertSnapIntentToIntent(Context context, Intent intent) {
        Intent internalServiceIntent = new Intent(context, SnapProxyService.class);
        internalServiceIntent.setAction(intent.getAction());
        internalServiceIntent.putExtra(SnapProxyService.EXTRA_SNAP_INTENT, intent);
        return internalServiceIntent;
    }

    /**
     * Converts a real Android Service intent into a Snap Intent, to be delivered to the Snap Services.
     * @param intent The intent to convert.
     * @return The converted intent, containing the original information to be delivered to the Snap Service.
     */
    static Intent convertIntentToSnapIntent(Intent intent) {
        if (intent == null) return null;

        return intent.getParcelableExtra(SnapProxyService.EXTRA_SNAP_INTENT);
    }
}
