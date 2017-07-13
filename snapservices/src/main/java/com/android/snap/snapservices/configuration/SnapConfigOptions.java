package com.android.snap.snapservices.configuration;

import android.support.annotation.IntDef;

import com.android.snap.snapservices.logger.SnapLogger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class that should be passed when initializing the SnapServicesContext, to tell it how it should behave.
 */
public class SnapConfigOptions {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SnapLogger.VERBOSE,
            SnapLogger.DEBUG,
            SnapLogger.INFO,
            SnapLogger.WARN,
            SnapLogger.ERROR,
            SnapLogger.DISABLED
    })
    public @interface LogLevel {}

    private final boolean killSeparateProcessOnFinish;
    private final int logLevel;

    public boolean isKillSeparateProcessOnFinish() {
        return killSeparateProcessOnFinish;
    }

    @LogLevel
    public int getLogLevel() {
        return logLevel;
    }

    private SnapConfigOptions(SnapConfigOptions.Builder builder) {
        this.killSeparateProcessOnFinish = builder.killSeparateProcess;
        this.logLevel = builder.logLevel;
    }

    public static final class Builder {

        private boolean killSeparateProcess = false;
        private int logLevel = SnapLogger.DISABLED;

        public Builder() {

        }

        public Builder killSeparateProcess(boolean kill) {
            this.killSeparateProcess = kill;
            return this;
        }

        public Builder setLogLevel(@LogLevel int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public SnapConfigOptions build() {
            return new SnapConfigOptions(this);
        }
    }

}
