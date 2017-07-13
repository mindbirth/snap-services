package com.android.snap.snapservices;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.android.snap.snapservices.alarms.SnapAlarmManager;
import com.android.snap.snapservices.binder.ISnapBinder;
import com.android.snap.snapservices.binder.SnapServiceConnection;
import com.android.snap.snapservices.configuration.SnapConfigOptions;
import com.android.snap.snapservices.foreground.ForegroundService;
import com.android.snap.snapservices.foreground.SnapForegroundService1;
import com.android.snap.snapservices.foreground.SnapForegroundService2;
import com.android.snap.snapservices.foreground.SnapForegroundService3;
import com.android.snap.snapservices.foreground.SnapForegroundService4;
import com.android.snap.snapservices.logger.SnapLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal object to control all lifecycle of the Snap Services.
 *
 * <p>This is the object that controls how to start and stop Snap Services, bind and unbind services.</p>
 */
class SnapActivityManager {

    private class OSnapHandler extends Handler {
        private static final int SNAP_WHAT_DELIVER_WORK = 1000;
        private static final int SNAP_WHAT_STOP_WORK = 2000;

        OSnapHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (mLock) {
                switch (msg.what) {
                    case SNAP_WHAT_DELIVER_WORK:
                        if (msg.obj == null) return;
                        Intent intent = (Intent) msg.obj;
                        ComponentName component = intent.getComponent();
                        SnapService serviceWorker = getServiceWorker(component);
                        if (serviceWorker == null) return;
                        int startId = getID();
                        mServiceWorkersIds.put(component, startId);
                        serviceWorker.onStartCommand((Intent) msg.obj, startId);
                        break;
                    case SNAP_WHAT_STOP_WORK:
                        stopServiceWorker((ComponentName) msg.obj, msg.arg1);
                        break;
                }
            }
        }
    }

    private static final int BIND_SERVICE_START_ID = -1;

    static SnapActivityManager sInstance;

    private Handler mHandler;

    private final SnapConfigOptions options;
    private final String packageName;
    private Object mLock = new Object();
    private final static AtomicInteger mStartId = new AtomicInteger(1);
    private Context context;


    private final Map<ComponentName, SnapService> mServiceWorkers = new ConcurrentHashMap<>();
    private final Map<ComponentName, Integer> mServiceWorkersIds = new ConcurrentHashMap<>();

    private final Map<ComponentName, Map<ComponentName, SnapServiceConnection>> mBoundedServices = new ConcurrentHashMap<>();
    private final Map<ComponentName, Class> mForegroundServices = new ConcurrentHashMap<>();

    private static final Class[] AVAILABLE_FOREGROUND_SERVICES = new Class[]{
            SnapForegroundService1.class,
            SnapForegroundService2.class,
            SnapForegroundService3.class,
            SnapForegroundService4.class
    };

    private SnapActivityManager(Context context, SnapConfigOptions options) {
        this.context = context;
        this.options = options;
        this.packageName = context.getPackageName();

        init();
    }

    /**
     * Startup the Snap services manager. This is the start point of this manager.
     * You should call this to initialize the manager and to configure the log level you want.
     *
     * @param applicationContext The application context.
     * @param snapConfigOptions The Snap Configuration Options with which this manager should be initialized with.
     */
    synchronized static void startup(Context applicationContext, SnapConfigOptions snapConfigOptions) {
        if (applicationContext == null) {
            throw new RuntimeException("Context cannot be null!");
        }

        if (sInstance == null) {
            sInstance = new SnapActivityManager(applicationContext.getApplicationContext(), snapConfigOptions);
        }
    }

    static SnapActivityManager getDefault() {
        return sInstance;
    }

    /**
     * Initializes all needed objects for this manager to work properly.
     */
    private synchronized void init() {
        mHandler = new SnapActivityManager.OSnapHandler(Looper.getMainLooper());
    }

    /**
     * Atomically increments the start id by one.
     *
     * @return the updated start id.
     */
    private static int getID() {
        return mStartId.incrementAndGet();
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
    synchronized void startSnapService(Intent intent) {
        SnapLogger.v("Deliver Work on main process with intent: " + intent);

        if (intent == null) {
            SnapLogger.d("Tried to start snap service with null intent. Do nothing.");
            return;
        }

        if (verifyIfIsForkedProcess()) {
            SnapLogger.d("We're inside another process. Forward to the main process via an alarm.");
            SnapAlarmManager.setAlarm(context, AlarmManager.ELAPSED_REALTIME_WAKEUP, intent, 1, 1);
            return;
        }

        Message message = new Message();
        message.what = OSnapHandler.SNAP_WHAT_DELIVER_WORK;
        message.obj = intent;
        mHandler.sendMessage(message);
    }

    synchronized void startSnapServiceOnAnotherProcess(Intent intent) {
        SnapLogger.v("Deliver Work on secondary process with intent: " + intent);

        if (intent == null) {
            SnapLogger.d("Tried to start snap service with null intent. Do nothing.");
            return;
        }

        if (!verifyIfIsForkedProcess()) {
            SnapLogger.d("We're not inside another process. Forward to the other process via an alarm.");
            SnapAlarmManager.setAlarmOnSeparateProcess(context, AlarmManager.ELAPSED_REALTIME_WAKEUP, intent, 1, 1);
            return;
        }

        Message message = new Message();
        message.what = OSnapHandler.SNAP_WHAT_DELIVER_WORK;
        message.obj = intent;
        mHandler.sendMessage(message);
    }
    /**
     * Connect to a Snap Service, creating it if needed.  This defines
     * a dependency between your application and the Snap Service.  The given
     * <var>conn</var> will receive the service object when it is created and be
     * told if it dies and restarts.</p>
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
    synchronized boolean bindService(Intent service, SnapServiceConnection conn) {
        if (service == null) return false;

        synchronized (mBoundedServices) {
            SnapLogger.v("Request binding for [intent=" + service + "]");
            ComponentName component = service.getComponent();
            try {
                SnapService serviceWorker = getServiceWorker(component);
                if (serviceWorker == null) return false;
                ISnapBinder iSnapBinder = serviceWorker.onBind(service);
                conn.onServiceConnected(component, iSnapBinder);
                addBoundedServiceConnection(component, conn);
                return true;
            } catch (Exception ex) {
                SnapLogger.e("Error binding service [component=" + component + "]", ex);
                return false;
            }
        }
    }

    /**
     * Disconnect from an application service.  You will no longer receive
     * calls as the service is restarted, and the service is now allowed to
     * stop at any time.
     *
     * @param conn The connection interface previously supplied to
     *             bindService().  This parameter must not be null.
     *
     * @see #bindService
     */
    synchronized boolean unbindService(SnapServiceConnection conn) {
        if (conn == null) return false;

        synchronized (mBoundedServices) {
            for (ComponentName componentName : mBoundedServices.keySet()) {
                Map<ComponentName, SnapServiceConnection> stringSnapServiceConnectionMap
                        = mBoundedServices.get(componentName);
                SnapServiceConnection remove = stringSnapServiceConnectionMap.remove(
                        new ComponentName(packageName, conn.getClass().getName()));
                if (remove != null) {
                    SnapLogger.v("Unbinding service [componentName=" + componentName + "]");
                    remove.onServiceDisconnected(componentName);

                    if (stringSnapServiceConnectionMap.isEmpty()) {
                        mBoundedServices.remove(componentName);
                    }

                    stopSelfWorker(componentName.getClassName(), BIND_SERVICE_START_ID);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Manages the snap service connections, creating a list of them if needed.
     * @param connectionComponent Indicates the Snap Service to retrieve its connection list.
     * @return A map of its connections.
     */
    private synchronized Map<ComponentName, SnapServiceConnection> getSnapServiceConnections(ComponentName connectionComponent) {
        synchronized (mBoundedServices) {
            Map<ComponentName, SnapServiceConnection> snapServiceConnections = mBoundedServices.get(connectionComponent);
            if (snapServiceConnections == null) {
                snapServiceConnections = new HashMap<>();
                mBoundedServices.put(connectionComponent, snapServiceConnections);
            }
            return snapServiceConnections;
        }
    }

    /**
     * Adds a SnapService to the bounded services list, along with the connection the service is bounded with.
     * The same service can have multiple connections.
     * @param serviceComponent Indicates the Snap Service to bound.
     * @param snapServiceConnection The connection the service is bounded with.
     */
    private synchronized void addBoundedServiceConnection(ComponentName serviceComponent, SnapServiceConnection snapServiceConnection) {
        synchronized (mBoundedServices) {
            Map<ComponentName, SnapServiceConnection> snapServiceConnections = getSnapServiceConnections(serviceComponent);

            ComponentName serviceConnectionComponent = new ComponentName(packageName,
                    snapServiceConnection.getClass().getName());

            if (snapServiceConnections.isEmpty() || !snapServiceConnections.containsKey(serviceConnectionComponent)) {
                snapServiceConnections.put(serviceConnectionComponent, snapServiceConnection);
            }
        }
    }

    /**
     * Checks if the provided Snap Service is bounded or not.
     * @param serviceComponent Indicates the Snap Service to validate.
     * @return True if is bounded, false otherwise.
     */
    private synchronized boolean isServiceBounded(ComponentName serviceComponent) {
        synchronized (mBoundedServices) {
            return mBoundedServices.containsKey(serviceComponent);
        }
    }

    /**
     * Requests the internal manager to stop the supplied Snap Service.
     *
     * @param className The class name of the Snap Service.
     * @param startId The ID in which the Snap Service was started with.
     */
    synchronized void stopSelfWorker(String className, int startId) {
        SnapLogger.v("Requesting to stopSelfWorker [className=" + className + ";startId=" + startId + "]");
        Message message = new Message();
        message.what = OSnapHandler.SNAP_WHAT_STOP_WORK;
        message.obj = new ComponentName(packageName, className);
        message.arg1 = startId;
        mHandler.sendMessage(message);
    }

    /**
     * Manages the running services by providing an existing one or creating one if needed.
     * The Snap Service is then added into a pool of running Snap Services.
     * @param componentName Component Name indicating the Snap Service to get.
     * @return An instance of the expected Snap Service, or null if it couldn't be created.
     */
    private synchronized SnapService getServiceWorker(ComponentName componentName) {
        synchronized (mServiceWorkers) {
            SnapService snapService = mServiceWorkers.get(componentName);

            if (snapService != null) {
                return snapService;
            }

            Class<?> workServiceClass = null;
            try {
                workServiceClass = Class.forName(componentName.getClassName());
            } catch (ClassNotFoundException e) {
                SnapLogger.e("Error getting class for name", e);
            }

            if (workServiceClass == null) return null;

            Object workerServiceObject = null;
            try {
                workerServiceObject = workServiceClass.newInstance();
            } catch (Exception e) {
                SnapLogger.e("Error instantiating class", e);
            }

            if (workerServiceObject == null || !(workerServiceObject instanceof SnapService))
                return null;

            SnapService worker = (SnapService) workerServiceObject;
            worker.attach(context);
            worker.onCreate();
            mServiceWorkers.put(componentName, worker);
            return worker;
        }
    }

    /**
     * Tries to stop the supplied Snap Service.
     *
     * @param serviceComponent The Snap Service to be stopped.
     * @param startId The ID in which the Snap Service was started with.
     */
    private synchronized void stopServiceWorker(ComponentName serviceComponent, int startId) {
        synchronized (mLock) {
            SnapLogger.v("Stopping service [component=" + serviceComponent + ";startId=" + startId + "]");
            Integer currentStartId = mServiceWorkersIds.get(serviceComponent);

            if (currentStartId == null && startId == BIND_SERVICE_START_ID || currentStartId != null && currentStartId == startId) {
                mServiceWorkersIds.remove(serviceComponent);

                if (isServiceBounded(serviceComponent)) {
                    SnapLogger.v("Service [component=" + serviceComponent + ";startId=" + startId + "] is still bounded. Don't stop.");
                    return;
                }

                SnapService remove = mServiceWorkers.remove(serviceComponent);

                if (remove == null) {
                    SnapLogger.v("Service [component=" + serviceComponent + ";startId=" + startId + "] killed in the meantime.");
                    return;
                }

                try {
                    remove.onDestroy();
                } catch (Exception ex) {
                    SnapLogger.v("Error destroying service [component=" + serviceComponent + ";startId=" + startId + "]", ex);
                }

                SnapLogger.v("Service [component=" + serviceComponent + ";startId=" + startId + "] stopped!");

                if (options.isKillSeparateProcessOnFinish() && verifyIfIsForkedProcess()) {
                    SnapLogger.v("This is the other process. Stop it!");
                    Process.killProcess(Process.myPid());
                }

            } else {
                SnapLogger.v("Service [component=" + serviceComponent + ";startId=" + startId + "] still running. Don't stop me now!");
            }
        }
    }

    synchronized void startForegroundService(ComponentName service, int notificationId,
                                                    Notification notification) {
        synchronized (mForegroundServices) {
            Class foregroundServiceClass = mForegroundServices.get(service);

            if (foregroundServiceClass == null) {
                // that notification ID is still not in use. Find a foreground service to use.
                for (Class availableForegroundService : AVAILABLE_FOREGROUND_SERVICES) {
                    if (!mForegroundServices.containsValue(availableForegroundService)) {
                        foregroundServiceClass = availableForegroundService;
                        break;
                    }
                }
            }

            if (foregroundServiceClass == null) {
                //there's no foreground service available. try next time.
                return;
            }

            //use the service available
            Intent foregroundService = new Intent(context, foregroundServiceClass);
            foregroundService.setAction(ForegroundService.ACTION_START_FOREGROUND);
            foregroundService.putExtra(ForegroundService.NOTIFICATION_EXTRA, notification);
            foregroundService.putExtra(ForegroundService.NOTIFICATION_ID_EXTRA, notificationId);
            startService(foregroundService);

            mForegroundServices.put(service, foregroundServiceClass);
        }
    }

    /**
     * Requests the provided service to be removed from background.
     *
     * @param service The service to be stopped form background.
     */
    synchronized void stopForegroundService(ComponentName service) {
        synchronized (mForegroundServices) {
            Class foregroundServiceClass = mForegroundServices.remove(service);
            if (foregroundServiceClass == null) {
                return;
            }

            Intent foregroundService = new Intent(context, foregroundServiceClass);
            foregroundService.setAction(ForegroundService.ACTION_STOP_FOREGROUND);
            startService(foregroundService);
        }
    }

    /**
     * Wrapper to start a foreground service.
     *
     * @param foregroundService The service to be started.
     */
    private void startService(Intent foregroundService) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(foregroundService);
        } else {
            //Android O
            context.startForegroundService(foregroundService);
        }
    }

    /**
     * Verifies current process name corresponds to ":snap-service-fork".
     *
     * @return True if this is the forked process.
     */
    boolean verifyIfIsForkedProcess() {
        int myPid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == myPid) {
                String currentProcName = processInfo.processName;
                if (!TextUtils.isEmpty(currentProcName)
                        && currentProcName.equals(context .getPackageName() + ":snap_service_fork")) {
                    return true;
                }
            }
        }

        return false;
    }
}
