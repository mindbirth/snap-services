package com.android.snap.snapservices;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.snap.snapservices.alarms.SnapAlarmManager;
import com.android.snap.snapservices.binder.SnapServiceConnection;
import com.android.snap.snapservices.configuration.SnapConfigOptions;
import com.android.snap.snapservices.logger.SnapLogger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Class responsible to manage all the services. This will is responsible to start and stop services,
 * following the expected lifecycle.</p>
 *
 * <p>This engine is meant to be as simples and as straightforward as possible, following the same API
 * that is already used to start Android Services.</p>
 *
 * <p>To start a service you should invoke the {@link #startService(Intent)} and pass the Intent, with
 * the details you're already used to when invoking Android Services.</p>
 *
 * <b>Example:</b>
 * <pre>
 * {@code
 * Intent intent = new Intent(getApplicationContext(), SomeSnapService.class);
 * intent.setAction("com.myaction");
 * intent.putExtra("EXTRA_KEY", "value for this key")
 * SnapServicesContext.startSnapService(intent)
 * }
 * </pre>
 * The above example shows how the service should be started. There will only be one instance
 * of each service running at the same time. If the service isn't running, it will be started and the
 * Intent delivered to it. If it's already running, only the intent will be delivered.
 *
 * <p>The SnapService follows a well-defined lifecycle:</p>
 * <ul>
 *      <li>{@link SnapService#onHandleIntent(Intent)}</li>
 *      <li>{@link SnapService#onDestroy()}</li>
 * </ul>
 * <p>There are other calls to the available methods inside the SnapService, but those shouldn't be called directly.
 * This flow will be followed. If some exception happens when invoking either of the above methods,
 * the SnapService will continue to process the messages in the queue (if any) and finishing once all are done.</p>
 */
public class SnapServicesContext {

    private static SnapServicesContext sInstance;
    private final Context context;
    private static boolean mInitialized;
    private static boolean sIsInitialized = false;

    private final SnapConfigOptions options;
    private final static AtomicInteger mStartId = new AtomicInteger(1);


    private SnapServicesContext(Context context, SnapConfigOptions options) {
        this.context = context.getApplicationContext();
        this.options = options;

        init();
    }

    /**
     * Contains the current version of this library.
     *
     * @return A string, representing the current version o this library.
     */
    public static String getVersion() {
        return BuildConfig.SNAP_SERVICES_VERSION;
    }

    /**
     * Startup the Snap services manager. This is the start point of this manager.
     * You should call this to initialize the manager and to configure the log level you want.
     *
     * @param applicationContext The application context.
     * @param snapConfigOptions The Snap Configuration Options with which this manager should be initialized with.
     */
    public synchronized static void startup(Context applicationContext, SnapConfigOptions snapConfigOptions) {
        if (applicationContext == null) {
            throw new RuntimeException("Context cannot be null!");
        }

        if (sInstance == null) {
            sInstance = new SnapServicesContext(applicationContext.getApplicationContext(), snapConfigOptions);
            sIsInitialized = true;
        }
    }

    public synchronized static boolean isRunning() {
        return sIsInitialized;
    }

    /**
     * Initializes all needed objects for this manager to work properly.
     */
    private synchronized void init() {
        if (mInitialized) {
            SnapLogger.w("Already initialized");
            return;
        }

        SnapLogger.configure(options.getLogLevel());
        SnapActivityManager.startup(context, options);
        mInitialized = true;
    }

    /** Request that a given Snap Service be started.  The Intent
     * should contain either the complete class name of a specific service
     * implementation to start or a specific package name to target. If the
     * Intent doesn't have the name of the Snap Service to start, it will not start it.
     *
     * <p>If this service is not already running, it will be instantiated and started (creating a
     * process for it if needed); if it is running then it remains running.</p>
     *
     * <p>Every call to this method will result in delivering the provided intent to the SnapService
     * {@link SnapService#onHandleIntent(Intent)}</p>
     *
     * @param intent Identifies the Snap Service to be started.  The Intent must be either
     *               fully explicit (supplying a component name) or specify a specific package
     *               name it is targeted to.  Additional values
     *               may be included in the Intent extras to supply arguments along with
     *               this specific start call.
     */
    public synchronized static void startService(Intent intent) {
        if (!mInitialized) {
            SnapLogger.w("Not initialized. To use, please initialize first.");
            return;
        }

        SnapActivityManager.getDefault().startSnapService(intent);
    }

    public synchronized static void startServiceOnOtherProcess(Intent intent) {
        if (!mInitialized) {
            SnapLogger.w("Not initialized. To use, please initialize first.");
            return;
        }

        SnapActivityManager.getDefault().startSnapServiceOnAnotherProcess(intent);
    }

    /**
     * Requests the Snap Service to be stopped.
     *
     * @param className The name of the service to be stopped.
     * @param startId The id which the Snap Service was started with
     */
    synchronized static void stopSelfWorker(String className, int startId) {
        SnapActivityManager.getDefault().stopSelfWorker(className, startId);
    }

    /**
     * Connect to a Snap Service, creating it if needed.  This defines
     * a dependency between your application and the Snap Service.  The given
     * <var>conn</var> will receive the service object when it is created and be
     * told if it dies and restarts.
     *
     * <p class="note"><em>The Snap Service will stay alive until you
     * explicitly call {@link #unbindService(SnapServiceConnection)}. Until you do
     * the service will not be killed and remains ready to run.</em></p>
     *
     * <p class="note">Note: although right now it's possible to call this
     * from within a {@link BroadcastReceiver}, this is something you shouldn't do.
     * This will lead to binded Services being created that will not be unbind,
     * causing service leaks. The standard practice is to
     * {@link SnapServicesContext#startService(Intent)} with the arguments
     * containing the command to be sent, with the service calling its
     * {@link SnapService#stopSelf(int)} method when done executing
     * that command.</p>
     *
     * @param service Identifies the service to connect to.  The Intent needs to
     *                specify an explicit component name.
     * @param conn Receives information as the service is started and stopped.
     *             This must be a valid SnapServiceConnection object; it must not be null.
     * @return If you have successfully bound to the service, {@code true} is returned
     *             {@code false} is returned if the connection is not made so you will not
     *             receive the service object.
     * @see #unbindService
     */
    public synchronized static boolean bindService(Intent service, SnapServiceConnection conn) {
        if (!mInitialized) {
            SnapLogger.w("Not initialized. To use, please initialize first.");
            return false;
        }

        return SnapActivityManager.getDefault().bindService(service, conn);
    }

    /**
     * Disconnect from an application service.  You will no longer receive
     * calls as the service is restarted, and the service is now allowed to
     * stop at any time.
     *
     * @param conn The connection interface previously supplied to
     *             bindService().  This parameter must not be null.
     *
     * @return True if successfully unbinded, false otherwise
     * @see #bindService
     */
    public synchronized static boolean unbindService(SnapServiceConnection conn) {
        if (!mInitialized) {
            SnapLogger.w("Not initialized. To use, please initialize first.");
            return false;
        }

        return SnapActivityManager.getDefault().unbindService(conn);
    }

    /**
     * Verifies if this is currently running on the secondary process.
     *
     * @return True if it is, false otherwise.
     */
    public synchronized static boolean isThisTheOtherProcess() {
        if (!mInitialized) {
            SnapLogger.w("Not initialized. To use, please initialize first.");
            return false;
        }

        return SnapActivityManager.getDefault().verifyIfIsForkedProcess();
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

     * @param context The application context.
     * @param intent The intent to be added into a pending intent.
     * @return The PendingIntent already prepared to be delivered to a SnapService OR, to an Android Service
     * if you passed that one instead.
     */
    public synchronized static PendingIntent generatePendingIntentForService(Context context, Intent intent) {

        boolean isSnapService = false;
        try {
            //checks if this intent is meant to a SnapService.
            isSnapService = SnapService.class.isAssignableFrom(Class.forName(intent.getComponent().getClassName()));
        } catch (ClassNotFoundException ex) {
            SnapLogger.e("Error checking if class inside intent extends from SnapService", ex);
        }

        if (isSnapService) {
            Intent proxyIntent = SnapAlarmManager.convertSnapIntentToIntent(context, intent);
            return PendingIntent.getBroadcast(context, 0, proxyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        //This is not a Snap Service. Still, generate the proper pending intent for it.
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
