package com.example.headsetreminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HeadphoneService extends Service {


    HeadphoneReceiver mReceiver;
    ServiceToActivity mInterface;
    private final IBinder mBinder = new LocalBinder();
    Handler handler;
    public String headphoneStatus;
    public boolean isStopwatchRunning = false;

    public void setInterface(ServiceToActivity mInterface) {
        this.mInterface = mInterface;
    }

    @Override
    public void onCreate() {
        mReceiver = new HeadphoneReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, intentFilter);
        handler = new Handler(Looper.getMainLooper());
    }

    boolean isActivityInForeground() {

        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isActive", true);

    }

    boolean isForeground = false;

    public void startServiceAsForeground() {
        if (!headphoneStatus.equals("")) {
            startForeground(FOREGROUND_NOTIFICATION_ID, getNotification("Running in Background", headphoneStatus, FOREGROUND_NOTIFICATION_ID));
            isForeground = true;
        }

    }

    /*private String getNotificationTitle(String headphoneStatus){
        if(headphoneStatus.equals("Headset Plugged"))
    }*/

    MyStopwatch stopwatch = new MyStopwatch();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    boolean isTimerRunning = false;
    Timer timer;

    public void scheduleTimer( /*String headphone_status*/) {
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = false;
        }

        int delay = 0; // delay for 0 sec.
        int period = 1; // repeat every 1 millisecond.
        timer = new Timer();
        if (!isTimerRunning) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (stopwatch.isRunning()) {
                                mInterface.receiveMessage(stopwatch.getElapsedTime(), headphoneStatus);
                                Log.d(HeadphoneService.class.getName(), "headphoneStatus inside scheduleTimer after notification = " + headphoneStatus);
                            }
                        }
                    });
                }
            }, delay, period);
            isTimerRunning = true;
        }

    }

    public void scheduleTimerThree(){
        handler.removeCallbacks(runnableCode);
        handler.post(runnableCode);
    }

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            Log.d("Handlers", "Called on main thread");
            if (stopwatch.isRunning()) {
                mInterface.receiveMessage(stopwatch.getElapsedTime(), headphoneStatus);
                Log.d(HeadphoneService.class.getName(), "headphoneStatus inside scheduleTimer after notification = " + headphoneStatus);
            }
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, 20);
        }
    };
// Start the initial runnable task by posting through the handler



    /*public void scheduleTimerTwo() {

        // stopTimer
       *//* if(isTimerRunning)
        stopTimer();*//*
        //start Timer
      //  handler.postDelayed(repeatTask, 1);
        repeatTask.run();
    }

    public void stopTimer(){
        handler.removeCallbacks(repeatTask);
        isTimerRunning = false;
    }


    Runnable repeatTask = new Runnable() {
        @Override
        public void run() {
        //    if(!isTimerRunning)
            if (stopwatch.isRunning()) {
                mInterface.receiveMessage(stopwatch.getElapsedTime(), headphoneStatus);
                Log.d(HeadphoneService.class.getName(), "headphoneStatus inside scheduleTimer after notification = " + headphoneStatus);
                isTimerRunning = true;
                handler.postDelayed(this, 1);
            }
        }
    };*/

    Thread stopwatchThread;

    private void resetAndStartStopwatch() {

        stopwatchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                stopwatch.reset();
                stopwatch.start();
                //   isStopwatchRunning = true;
            }
        });

        stopwatchThread.start();

    }

    private void closeStopwatchThread() {
        if (stopwatch.isRunning()) {
            stopwatch.stop();
            // isStopwatchRunning = false;
        }
    }


    //returns the instance of the service
    public class LocalBinder extends Binder {
        public HeadphoneService getServiceInstance() {
            return HeadphoneService.this;
        }
    }

    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private boolean isNotifyManagerCreated = false;


    public void createNotificationChannel() {
        if (!isNotifyManagerCreated) {
            mNotifyManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.O) {
                // Create a NotificationChannel
                NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                        "Mascot Notification", NotificationManager
                        .IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setDescription("Notification from Mascot");
                mNotifyManager.createNotificationChannel(notificationChannel);

            }
            isNotifyManagerCreated = true;

        }
    }

    private NotificationCompat.Builder getNotificationBuilder(String title, String content, int notificationId) {

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(HeadphoneService.this, PRIMARY_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_headset)
                .setAutoCancel(true);
        Intent notificationIntent = new Intent(HeadphoneService.this, MainActivity.class);
        if (notificationId == FOREGROUND_NOTIFICATION_ID) {
            notificationIntent.putExtra("status", content);
        } else {
            notificationIntent.putExtra("status", title);
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(HeadphoneService.this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notifyBuilder.setContentIntent(contentIntent);


        return notifyBuilder;

    }

    NotificationCompat.Builder notifyBuilder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification(String title, String content, int notificationId) {
        createNotificationChannel();
        //       notifyBuilder = getNotificationBuilder(title, content);
        mNotifyManager.notify(notificationId, getNotification(title, content, notificationId));
    }

    public Notification getNotification(String title, String content, int notificationId) {
        return getNotificationBuilder(title, content, notificationId).build();

    }


    private class HeadphoneReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            String intentAction = intent.getAction();
            if (!isInitialStickyBroadcast() && intentAction != null && intentAction.equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0: {
                        headphoneStatus = "Headset UnPlugged";
                        //Headset unplugged
                        //show notification that headset was unplugged (although only when it was plugged to begin with.)

                       /* stopwatch.reset();
                        stopwatch.start();
                        isStopwatchRunning = true;*/
                        closeStopwatchThread();
                        resetAndStartStopwatch();
                        /*if (isForeground) {
                            stopForeground(true);
                        }*/


                        if (isActivityInForeground()) {
                            //restart timer
                            //          startHandler();
                            //  restartTimer();
                            //updateUI
                 //           scheduleTimer(/*"Headset UnPlugged"*/);
                            scheduleTimerThree();
                    //        scheduleTimerTwo();

                            //     setUpWorkManager(false);
                        } else {
                            sendNotification("Headset UnPlugged", "You've unplugged the headsets", isForeground ? FOREGROUND_NOTIFICATION_ID : NOTIFICATION_ID);
//                            if (isForeground) {
//                                stopForeground(true);
//                            }
                        }


                        /*else {
                            startForeground(NOTIFICATION_ID, );
                        }*/

                        //      headsetStatus.setText(STATUS_PLUGGED_OUT);

                        break;

                    }
                    case 1: {
                        headphoneStatus = "Headset Plugged";
                        //Headset plugged
                        //show notification that headset was plugged
                        //   headsetStatus.setText(STATUS_PLUGGED_IN);
                        closeStopwatchThread();
                        resetAndStartStopwatch();
                        /*stopwatch.reset();
                        stopwatch.start();
                        isStopwatchRunning = true;*/
                        /*if (isForeground) {
                            stopForeground(true);
                        }*/
                        //resetTimer

                        if (isActivityInForeground()) {
                            //restart timer
                            //   restartTimer();
                            //updateUI
                     //       scheduleTimer(/*"Headset Plguged"*/);
                         //   scheduleTimerTwo();
                            scheduleTimerThree();
                            //       setUpWorkManager(true);
                        } else {
                            sendNotification("Headset Plugged", "You've plugged the headsets", isForeground ? FOREGROUND_NOTIFICATION_ID : NOTIFICATION_ID);
                           /* if (isForeground) {
                                stopForeground(true);
                            }*/
                        }
                        break;

                    }
                }


            }
        }


    }


    public interface ServiceToActivity {

        void receiveMessage(String timeStamp, String headphoneStatus);
    }
}


