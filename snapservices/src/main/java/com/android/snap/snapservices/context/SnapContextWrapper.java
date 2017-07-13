package com.android.snap.snapservices.context;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.android.snap.snapservices.SnapServicesContext;

/**
 * Proxying implementation of ContextWrapper that simply delegates all of its calls to
 * another Context.  Can be subclassed to modify behavior without changing
 * the original Context.
 */
public class SnapContextWrapper extends ContextWrapper {

    public SnapContextWrapper(Context base) {
        super(base);
    }

    /**
     * This method launch a Snap Service, without the need to request SnapServicesContext.startService(Intent).
     *
     * <p>If you do want to start a real Android Service, this method will be smart enough to detect that the component you want
     * to launch is not a Snap Service. When this happens, a real Android service will be launched</p>
     *
     * @param service The service to launch.
     * @return The component that was launched.
     */
    @Override
    public ComponentName startService(Intent service) {
        SnapServicesContext.startService(service);
        return service.getComponent();
    }

    /**
     * The same as {@link #startService(Intent)} but instead launches the Snap Service on a secondary process.
     *
     * <p>If you do want to start a real Android Service, this method will be smart enough to detect that the component you want
     * to launch is not a Snap Service. When this happens, a real Android service will be launched</p>
     *
     * @param service The service to launch.
     * @return The component that was launched.
     */
    public ComponentName startServiceOnOtherProcess(Intent service) {
        SnapServicesContext.startServiceOnOtherProcess(service);
        return service.getComponent();
    }

    /**
     * <p>This method was added to Android O due to the impossibility of start services via the usual
     * {@link android.content.Context#startService(Intent)}.</p>
     *
     * <p>However, with Snap Service we don't have that limitation. That's why, for SnapServices, this method is only a proxy to
     * our own {@link #startService(Intent)}.</p>
     *
     * <p>If you do want to start a real Android Service, please do not use this context and instead, request the application context.</p>
     *
     * <p>If you need to start a Snap Service in foreground, you should do it like you would normally
     * do with an Android Service: after the service starts, call {@link com.android.snap.snapservices.SnapService#startForeground(int, Notification)}</p>
     *
     * @param service The service to start.
     * @return The component name of the started service.
     */
    public ComponentName startForegroundService(Intent service) {
        return startService(service);
    }

    /**
     * Generates a Pending Intent for a Snap Service.
     *
     * <p>If you don't pass a SnapService as the destination, this will still generate the Pending Intent
     * for the actual component you specified on the Intent, and not be converted in any way.</p>
     *
     * <p>The request code for this PendingIntent will always be 0</p>
     *
     * <p><em>NOTE:</em>You should ALWAYS use this method when you want to generate a pending intent for a notification.</p>
     * @param intent The intent to be added into a pending intent.
     * @param requestCode The private request code of the sender.
     * @return The PendingIntent already prepared to be delivered to a SnapService OR, to an Android Service
     * if you passed that one instead.
     * @see SnapServicesContext#generatePendingIntentForService(Context, Intent, int)
     */
    public PendingIntent generatePendingIntentForService(Intent intent, int requestCode) {
        return SnapServicesContext.generatePendingIntentForService(getApplicationContext(), intent, requestCode);
    }
}
