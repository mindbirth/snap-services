package com.android.snap.snapservices;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.WorkerThread;

import com.android.snap.snapservices.binder.ISnapBinder;
import com.android.snap.snapservices.context.SnapContextWrapper;
import com.android.snap.snapservices.logger.SnapLogger;

/**
 * <p>SnapService is a class based on {@link android.app.IntentService}, that handle asynchronous
 * requests (expressed as {@link android.content.Intent}s) on demand.</p>
 *
 * <p>This service follows a similar approach where they are started via {@link SnapServicesContext#startService(Intent)},
 * by passing an Intent.
 * The SnapService is started as needed and handles each intent using a worker thread. Once everything is done,
 * it stops itself.</p>
 *
 * <p>This "work queue processor" pattern is commonly used to offload tasks
 * from an application's main thread.  The SnapService class exists to
 * simplify this pattern and take care of the mechanics.  To use it, extend
 * SnapService and implement {@link #onHandleIntent(Intent)}.  SnapService
 * will receive the Intents, launch a worker thread, and stop the service as
 * appropriate.</p>
 *
 * <p>All requests are handled on a single worker thread -- they may take as
 * long as necessary (and will not block the application's main loop), but
 * only one request will be processed at a time.</p>
 */
public abstract class SnapService extends SnapContextWrapper {

    private String mName;
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private volatile HandlerThread mThread;

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            SnapLogger.v("[name=" + mName + ";handleMessage=" + msg + "]");
            onHandleIntent((Intent) msg.obj);
            stopSelf(msg.arg1);
        }
    }

    public SnapService(String name) {
        super(null);
        mName = name;
    }

    /**
     * Called by the SnapActivityManager after the constructor and before the on create is called.
     *
     * @param context The application context.
     */
    final void attach(Context context) {
        super.attachBaseContext(context);
    }

    /**
     * Called by the SnapServicesContext when the service is first created.
     */
    protected void onCreate() {
        SnapLogger.v("onCreate called [name=" + mName + "]");
        mThread = new HandlerThread("SnapService[" + mName + "]");
        mThread.start();

        mServiceLooper = mThread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    /**
     * This is the initial method that is going to be called to deliver the intent.
     * Afterwards, the intent is delivered to the current service internal queue, where
     * it's going to be processed inside its own worker thread.
     * All intents are processed in the order they arrive, one at a time.
     *
     * @param intent  The intent with all the details for the service to process this request.
     * @param startId A unique integer representing this specific request to
     *                start.  Use with {@link #stopSelf(int)}.
     */
    protected void onStartCommand(Intent intent, int startId) {
        SnapLogger.v("onStartCommand called [name=" + mName + ";intent=" + intent + ";startId=" + startId + "]");
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = intent;
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
    }

    /**
     * Starts a Snap Service in foreground.
     *
     * @param notificationId The ID of the notification that will be shown.
     * @param notification The notification to be shown whilst the service runs in foreground.
     */
    protected void startForeground(int notificationId, Notification notification) {
        SnapActivityManager.getDefault().startForegroundService(new ComponentName(this, this.getClass()),
                notificationId, notification);
    }

    /**
     * Stops a Snap Service from being in foreground.
     */
    protected void stopForeground() {
        SnapActivityManager.getDefault().stopForegroundService(new ComponentName(this, this.getClass()));
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this SnapService object and it is effectively dead.
     *
     * <p>Do <b>NOT</b> call this method directly.</p>
     */
    public void onDestroy() {
        SnapLogger.v("onDestroy called [name=" + mName + "]");
        stopForeground();
        mServiceLooper.quit();
    }

    public ISnapBinder onBind(Intent intent) {
        SnapLogger.v("onBind called [name=" + mName + "]");
        return null;
    }

    /**
     * Stop the service if the most recent time it was started was
     * <var>startId</var>.
     *
     * @param startId The most recent start identifier received in {@link
     *                #onStartCommand}.
     */
    private void stopSelf(int startId) {
        SnapLogger.v("stopSelf called [name=" + mName + ";startId=" + startId + "]");
        SnapServicesContext.stopSelfWorker(getClass().getName(), startId);
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the SnapService stops itself.
     *
     * @param intent The value passed to {@link SnapServicesContext#startService(Intent)}.
     */
    @WorkerThread
    protected abstract void onHandleIntent(Intent intent);
}
