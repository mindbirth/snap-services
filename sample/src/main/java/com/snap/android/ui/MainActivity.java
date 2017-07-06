package com.snap.android.ui;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.snap.snapservices.SnapServicesContext;
import com.android.snap.snapservices.alarms.SnapAlarmManager;
import com.android.snap.snapservices.binder.ISnapBinder;
import com.android.snap.snapservices.binder.SnapServiceConnection;
import com.snap.android.R;
import com.snap.android.services.AnotherBindedService;
import com.snap.android.services.BindedService;
import com.snap.android.services.EggService;
import com.snap.android.services.ForkService;
import com.snap.android.services.MixService;
import com.snap.android.services.alarms.AlarmReceiverService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvThreadCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.b_service_egg).setOnClickListener(this);
        findViewById(R.id.b_service_fork).setOnClickListener(this);
        findViewById(R.id.b_service_mix).setOnClickListener(this);
        findViewById(R.id.b_launch_snap_alarm).setOnClickListener(this);
        findViewById(R.id.b_launch_snap_alarm_on_separate_process).setOnClickListener(this);
        findViewById(R.id.b_refresh_thread_count).setOnClickListener(this);
        findViewById(R.id.b_bind_service).setOnClickListener(this);
        findViewById(R.id.b_execute_inside_bind_service).setOnClickListener(this);
        findViewById(R.id.b_another_bind_service).setOnClickListener(this);
        findViewById(R.id.b_execute_inside_another_bind_service).setOnClickListener(this);
        findViewById(R.id.b_unbind_another_bind_service).setOnClickListener(this);
        findViewById(R.id.b_unbind_service).setOnClickListener(this);
        findViewById(R.id.b_start_service).setOnClickListener(this);
        findViewById(R.id.b_start_another_service).setOnClickListener(this);
        findViewById(R.id.b_start_egg_in_foreground).setOnClickListener(this);
        findViewById(R.id.b_start_egg_in_another_process).setOnClickListener(this);

        tvThreadCount = (TextView) findViewById(R.id.tv_thread_count);
        tvThreadCount.setClickable(false);
    }

    private BindedService mService = null;
    private boolean mServiceBinded = false;

    private SnapServiceConnection mConnection = new SnapServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, ISnapBinder service) {
            BindedService.LocalBinder binder = (BindedService.LocalBinder) service;
            mService = binder.getService();
            mServiceBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mServiceBinded = false;
        }
    };

    private AnotherBindedService mAnotherService;
    private boolean mAnotherServiceBinded = false;
    private SnapServiceConnection mAnotherConnection = new SnapServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, ISnapBinder service) {
            AnotherBindedService.LocalBinder binder = (AnotherBindedService.LocalBinder) service;
            mAnotherService = binder.getService();
            mAnotherServiceBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAnotherService = null;
            mAnotherServiceBinded = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SnapServicesContext.unbindService(mConnection);
        SnapServicesContext.unbindService(mAnotherConnection);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.b_refresh_thread_count) {
            tvThreadCount.setText("Current thread count: " + Thread.activeCount());

            Intent laterIntent = new Intent(getApplicationContext(), EggService.class);
            laterIntent.setAction("eggservice.action.DELETE_INTENT");

            PendingIntent laterPendingIntent = SnapServicesContext
                    .generatePendingIntentForService(getApplicationContext(), laterIntent, 1);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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

            Notification build = new NotificationCompat.Builder(getApplicationContext(), "my_channel_01")
                    //.addAction(R.drawable.ic_launcher, "NOW!", laterPendingIntent)
                    .setDeleteIntent(laterPendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("super expanded text"))
                    .build();

            final NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(123455, build);
            return;
        }

        //Launch Snap Service in 10 seconds, on main process
        if (v.getId() == R.id.b_launch_snap_alarm) {
            Intent intent = new Intent(getApplicationContext(), AlarmReceiverService.class);
            intent.setAction("com.snap.android.ui.SUPER_ALARM_ACTION");

            SnapAlarmManager.setAlarm(getApplicationContext(), AlarmManager.ELAPSED_REALTIME_WAKEUP, intent, 1,
                    SystemClock.elapsedRealtime() + 10000);
            Log.d("SnapApplication", "MainActivity: Launch AlarmReceiver, on main process, in 10 seconds");
            return;
        }

        //Launch Snap Service in 10 seconds, on secondary process
        if (v.getId() == R.id.b_launch_snap_alarm_on_separate_process) {
            Intent intent = new Intent(getApplicationContext(), AlarmReceiverService.class);
            intent.setAction("com.snap.android.ui.SUPER_ALARM_ACTION");

            SnapAlarmManager.setAlarmOnSeparateProcess(getApplicationContext(), AlarmManager.ELAPSED_REALTIME_WAKEUP, intent, 1,
                    SystemClock.elapsedRealtime() + 10000);
            Log.d("SnapApplication", "MainActivity: Launch AlarmReceiver, on another process, in 10 seconds");
            return;
        }

        //Bind a Snap Service (BindedService)
        if (v.getId() == R.id.b_bind_service) {
            Intent bindIntent = new Intent(getApplicationContext(), BindedService.class);
            SnapServicesContext.bindService(bindIntent, mConnection);
            return;
        }

        //Run doSomething() inside BindedService
        if (v.getId() == R.id.b_execute_inside_bind_service) {
            if (!mServiceBinded || mService == null) {
                Toast.makeText(getApplicationContext(), "Please bind the service first!", Toast.LENGTH_SHORT).show();
                return;
            }

            mService.doSomething();
            return;
        }

        //Bind a Snap Service (AnotherBindedService)
        if (v.getId() == R.id.b_another_bind_service) {
            Intent bindIntent = new Intent(getApplicationContext(), AnotherBindedService.class);
            SnapServicesContext.bindService(bindIntent, mAnotherConnection);
            return;
        }

        //Run doSomethingElse() inside BindedService
        if (v.getId() == R.id.b_execute_inside_another_bind_service) {
            if (!mAnotherServiceBinded || mAnotherService == null) {
                Toast.makeText(getApplicationContext(), "Please bind the service first!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAnotherService.doSomethingElse();
            return;
        }

        //Unbind service
        if (v.getId() == R.id.b_unbind_service) {
            SnapServicesContext.unbindService(mConnection);
            return;
        }

        //Unbind another service
        if (v.getId() == R.id.b_unbind_another_bind_service) {
            SnapServicesContext.unbindService(mAnotherConnection);
            return;
        }

        //Start EggService on another process.
        if (v.getId() == R.id.b_start_egg_in_another_process) {
            Intent intent = new Intent(getApplicationContext(), EggService.class);
            intent.setAction("com.snapservice.action.EGG");
            intent.putExtra("EXTRA_KEY", "some_key");
            SnapServicesContext.startServiceOnOtherProcess(intent);
            return;
        }

        Intent intent = null;
        switch (v.getId()) {
            case R.id.b_service_egg:
                intent = new Intent(getApplicationContext(), EggService.class);
                intent.setAction("com.snapservice.action.EGG");
                intent.putExtra("EXTRA_KEY", "some_key");
                break;
            case R.id.b_service_fork:
                intent = new Intent(getApplicationContext(), ForkService.class);
                intent.setAction("com.snapservice.action.FORK");
                break;
            case R.id.b_service_mix:
                intent = new Intent(getApplicationContext(), MixService.class);
                intent.setAction("com.snapservice.action.MIX");
                break;
            case R.id.b_start_service:
                intent = new Intent(getApplicationContext(), BindedService.class);
                intent.setAction("com.snapservice.action.binded");
                break;
            case R.id.b_start_another_service:
                intent = new Intent(getApplicationContext(), AnotherBindedService.class);
                intent.setAction("com.snapservice.action.anotherbinded");
                break;
            case R.id.b_start_egg_in_foreground:
                intent = new Intent(getApplicationContext(), EggService.class);
                intent.setAction(EggService.START_FOREGROUND_ACTION);
                break;
        }

        if (intent != null) {
            SnapServicesContext.startService(intent);
        }
    }
}
