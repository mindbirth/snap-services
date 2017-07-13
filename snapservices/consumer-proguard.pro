-optimizationpasses 5


-keep public class * extends com.android.snap.snapservices.SnapService {
    public <methods>;
    public <fields>;
}

-keep public class * extends com.android.snap.snapservices.SnapService

-keep public class com.android.snap.snapservices.SnapService {
    public <methods>;
    protected <methods>;
    public <fields>;
}
-keep public class com.android.snap.snapservices.alarms.SnapAlarmManager {
    public <methods>;
    protected <methods>;
    public <fields>;
}
-keep public class com.android.snap.snapservices.logger.SnapLogger {
    public <methods>;
    protected <methods>;
    public <fields>;
}
-keep public class com.android.snap.snapservices.SnapServicesContext {
    public <methods>;
    protected <methods>;
    public <fields>;
}
-keep public class com.android.snap.snapservices.configuration.SnapConfigOptions {
    public <methods>;
    protected <methods>;
    public <fields>;
}

-keep public class com.android.snap.snapservices.context.SnapContextWrapper {
    public <methods>;
    protected <methods>;
    public <fields>;
}
-keep public class com.android.snap.snapservices.binder.SnapBinder {
    public <methods>;
    protected <methods>;
    public <fields>;
}
-keep interface * {
  public <methods>;
  public <fields>;
}

-keep class com.android.snap.snapservices.configuration.SnapConfigOptions$Builder {
    public <methods>;
    protected <methods>;
    public <fields>;
}

-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContextWrapper
-keepattributes InnerClasses

#To completely remove the logging entries and the strings inside it
#-assumenosideeffects class com.android.snap.snapservices.logger.SnapLogger {
#    public static void v(...);
#    public static void d(...);
#    public static void i(...);
#    public static void w(...);
#    public static void e(...);
#}

#-assumenosideeffects class java.lang.StringBuilder {
#    public java.lang.StringBuilder append(String);
#    public java.lang.StringBuilder append(...);
#    public java.lang.String toString();
#}