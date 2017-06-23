package com.android.snap.snapservices.binder;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Keep;

import com.android.snap.snapservices.SnapServicesContext;

/**
 * Interface for monitoring the state of an application service.  See
 * {@link SnapBinder} and
 * {@link SnapServicesContext#bindService(Intent, SnapServiceConnection)} for more information.
 * <p>Like many callbacks from the system, the methods on this class are called
 * from the main thread of your process.
 */
public interface SnapServiceConnection {
    /**
     * Called when a connection to the Service has been established, with
     * the {@link ISnapBinder} of the communication channel to the
     * Service.
     *
     * @param name The concrete component name of the service that has
     * been connected.
     *
     * @param service The ISnapBinder of the Service's communication channel,
     * which you can now make calls on.
     */
    @Keep
    public void onServiceConnected(ComponentName name, ISnapBinder service);

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does <em>not</em> remove the SnapServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to {@link #onServiceConnected} when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     * connection has been lost.
     */
    @Keep
    public void onServiceDisconnected(ComponentName name);
}
