# Snap Services

A drop-in replacement (almost ;)) for Android services!

## Getting Started

This library is meant to serve as a clean and straightforward way to replace Android Services due to the new Android O limitations in using them. 
The purpose of this library is to keep your existing code working, without much changes required.

## Why do I need this?
Due to the limitations to start a service in Android O, we can no longer rely on Services to execute background tasks when we need it.

Without this library you are very confined to what you can do. You can either:

1. Abuse of the AlarmManager to launch Services to run immediately.
2. Abuse of the JobScheduler to launch "jobs" to run immediately.
3. Use bounded Services (if this is feasible at all)
4. Refactor your entire logic to start using specific components, with very limited abstraction and re-usability, in order to be able to run stuff in background whenever you need.
5. Use foreground services.

At the end, there's no direct solution to run something in background when you want, without the use of foreground services, and, IMHO, this is not a very good option.
 If you simply want to perform an internal cleanup/refresh/etc, do you really need to bother the user with yet another useless notification? (I fear that, when Android O does come, users will start to be flooded with such notifications giving the user no other option than to simply disallow them all). 

But enough of my random musings.

### What can this offer me?

Well, this library follows the Services API when possible, to give you the possibility of launching background services like you are used to.
However, instead of using Android Services, we use our own SnapServices. 

All Android services should be replaced with SnapServices. Most features that you were used to have with Services, you still have with SnapServices.

Features:

* follows the IntentService approach, where all work is queued and delivered on a separate thread
* start Snap Services like you would start an Android Service
* bind to and unbind from Snap Services like you're used to do with Android Services
* schedule alarms for Snap Services like you're used to do with Android Services
* notification actions support Snap Services. This will allow you to add PendingIntents to SnapServices on ANY notification you launch.
* all SnapServices have a real Application Context, wrapped around inside SnapContextWrapper (which extends from ContextWrapper), to give the option to start another Snap Service without the need of accessing SnapServicesContext.

This library also extends on features that existed in the past, but were limited:
* run ANY Snap Service on another process
* schedule ANY Snap Service to run on another process

**NOTE:** just like Android Services, Snap Services only have one instance running at ANY single time. The SnapService will only be killed if all actions were handled and if it's not binded.

## Examples

* Create a Service:
```
public class ExampleService extends SnapService {
    
    public ExampleService() {
        super("ExampleService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        
    }
}
```

* Start a Snap Service:
```
Intent intent = new Intent(getApplicationContext(), ExampleService.class);
intent.setAction("com.exampleservice.myaction");
intent.putExtra("EXTRA_KEY", "value for this key")
SnapServicesContext.startService(intent)
``` 

If you're inside a SnapService, you don't need to call ```SnapServicesContext.startService(Intent)```. Instead, you can invoke ```startService(Intent)``` directly from the SnapService.

* Start a Snap Service on another process
```
Intent intent = new Intent(getApplicationContext(), ExampleService.class);
intent.setAction("com.exampleservice.my.other.action");
intent.putExtra("EXTRA_KEY", "value for this key")
SnapServicesContext.startServiceOnOtherProcess(intent)
``` 

Once again, if you're inside a SnapService, you don't need to call ```SnapServicesContext.startServiceOnOtherProcess(Intent)```. Instead, you can invoke ```startServiceOnOtherProcess(Intent)``` directly from the SnapService.

* Bind a Snap Service

Binding a Snap Service still follows the same approach as you would do for an Android Service, but, instead of using the ```IBinder``` interface, ```Binder``` and ```ServiceConnection``` classes from Android, you use the ```ISnapBinder``` interface, ```SnapBinder``` and ```SnapServiceConnection```.  

You should do what you do already in order to bind a service:
1. Implement a Binder:
````
private final ISnapBinder mBinder = new LocalBinder();

public class LocalBinder extends SnapBinder {
    public ExampleService getService() {
        return ExampleService.this;
    }
}
````


2. Override the onBind:
```` 
@Override
public ISnapBinder onBind(Intent intent) {
    return mBinder;
}
````

3. Create your Service Connection:
````
private SnapServiceConnection mConnection = new SnapServiceConnection() {
    
    @Override
    public void onServiceConnected(ComponentName name, ISnapBinder service) {
        ExampleService.LocalBinder binder = (ExampleService.LocalBinder) service;
        mService = binder.getService();
        //additional logic
        ...
        mServiceBinded = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
        mServiceBinded = false;
    }
};
````

4. When you're ready, bind the service:
````
Intent bindIntent = new Intent(getApplicationContext(), ExampleService.class);
SnapServicesContext.bindService(bindIntent, mConnection);
```` 

5. When done, don't forget to unbind it:
````
SnapServicesContext.unbindService(mConnection);
```` 

* Send a notification with actions pointing to Snap Services:

````
Intent onDeleteIntent = new Intent(getApplicationContext(), ExampleService.class);
onDeleteIntent.setAction("com.exampleservice.ON_DELETE");

PendingIntent onDeletePendingIntent = SnapServicesContext.generatePendingIntentForService(getApplicationContext(), onDeleteIntent, 0);
````

then add the action to the notification: 

````
Notification build = new NotificationCompat.Builder(getApplicationContext(), "my_notif_channel")
    .setDeleteIntent(onDeletePendingIntent);
````

## How to import

* Make sure you have ````jcenter()```` configured on your project.
* Add this line to your dependencies: ````compile 'com.snapround.android:snapservices:1.2.3'````
* Happy coding!

## Contributing

Please read [CONTRIBUTING.md](https://github.com/mindbirth/snap-services/blob/master/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

See also the list of [contributors](https://github.com/mindbirth/snap-services/graphs/contributors) who participated in this project.

## Acknowledgments

* Thank you Google for removing the possibility to use background Services...

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
