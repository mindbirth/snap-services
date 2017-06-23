package com.android.snap.snapservices.logger;

import android.util.Log;

import com.android.snap.snapservices.configuration.SnapConfigOptions;

/**
 * Logger utility to manage all logging for this library.
 */
public class SnapLogger {
    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int DISABLED = 5;

    private static final String LOG_TAG = "SnapServicesLib";

    /**
     * The current log level. If defaults to {@link #ERROR}
     */
    private static int logLevel = ERROR;

    private SnapLogger() {

    }

    public static void configure(@SnapConfigOptions.LogLevel int logLevel) {
        SnapLogger.logLevel = logLevel;
    }

    public static void v(String message) {
        log(VERBOSE, message);
    }

    public static void v(String message, Throwable throwable) {
        log(VERBOSE, message, throwable);
    }

    public static void d(String message) {
        log(DEBUG, message);
    }

    public static void d(String message, Throwable throwable) {
        log(DEBUG, message, throwable);
    }

    public static void i(String message) {
        log(INFO, message);
    }

    public static void i(String message, Throwable throwable) {
        log(INFO, message, throwable);
    }

    public static void w(String message) {
        log(WARN, message);
    }

    public static void w(String message, Throwable throwable) {
        log(WARN, message, throwable);
    }

    public static void e(String message, Throwable throwable) {
        log(VERBOSE, message, throwable);
    }

    private static void log(int logLevel, String message) {
        if (SnapLogger.logLevel == DISABLED || logLevel < SnapLogger.logLevel) return;

        message = getThreadInfo() + " " + message;
        switch (logLevel) {
            case VERBOSE:
                Log.v(LOG_TAG, message);
                break;
            case DEBUG:
                Log.d(LOG_TAG, message);
                break;
            case INFO:
                Log.i(LOG_TAG, message);
                break;
            case WARN:
                Log.w(LOG_TAG, message);
                break;
            case ERROR:
                Log.e(LOG_TAG, message);
                break;
        }
    }

    private static void log(int logLevel, String message, Throwable throwable) {
        if (SnapLogger.logLevel == DISABLED || logLevel < SnapLogger.logLevel) return;

        message = getThreadInfo() + " " + message;
        switch (logLevel) {
            case VERBOSE:
                Log.v(LOG_TAG, message, throwable);
                break;
            case DEBUG:
                Log.d(LOG_TAG, message, throwable);
                break;
            case INFO:
                Log.i(LOG_TAG, message, throwable);
                break;
            case WARN:
                Log.w(LOG_TAG, message, throwable);
                break;
            case ERROR:
                Log.e(LOG_TAG, message, throwable);
                break;
        }
    }

    private static String getThreadInfo() {
        return "[pid=" + android.os.Process.myPid() + ";thread-name=" + Thread.currentThread().getName()
                + ";thread-id=" + Thread.currentThread().getId() + "]";
    }
}
