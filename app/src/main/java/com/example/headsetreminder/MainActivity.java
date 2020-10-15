package com.example.headsetreminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static String STATUS_PLUGGED_IN = "HEADPHONE PLUGGED IN";
    private static String STATUS_PLUGGED_OUT = "HEADPHONE PLUGGED OUT";
    public static String ACTION_RECEIVE_TIME = "action_receive_time";


    //  TimeReceiver mReceiver;
    TextView headsetStatus;
    TextView elapsedTime;
    boolean startedFromNotification = false;


    HeadphoneService.ServiceToActivity mInterface = new HeadphoneService.ServiceToActivity() {
        @Override
        public void receiveMessage(String timeStamp, String headphoneStatus) {

            elapsedTime.setText(timeStamp);
            headsetStatus.setText(headphoneStatus);

        }
    };

    HeadphoneService myService;

    private ServiceConnection mConnection;

    private ServiceConnection initializeServiceConnection() {
        Log.d(MainActivity.class.getName(), "initializeServiceConnection called");
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            /*Log.d(MainActivity.class.getName(), "onServiceConnected called");
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            HeadphoneService.LocalBinder binder = (HeadphoneService.LocalBinder) iBinder;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.setInterface(mInterface);*/
                serviceConnected(iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(MainActivity.class.getName(), "onServiceDisconnected called");
            }
        };
    }


    private void serviceConnected(IBinder iBinder) {
        Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
        Log.d(MainActivity.class.getName(), "onServiceConnected called");
        // We've binded to LocalService, cast the IBinder and get LocalService instance
        HeadphoneService.LocalBinder binder = (HeadphoneService.LocalBinder) iBinder;
        myService = binder.getServiceInstance(); //Get instance of your service!
        myService.setInterface(mInterface);
        if (startedFromNotification) {
    //        myService.scheduleTimer(/*notificationMessage*/);
            myService.scheduleTimerThree();
       //     myService.scheduleTimerTwo();
            Log.d(MainActivity.class.getName(), " notification message before schedule timer from notification = " + notificationMessage);
        }
        if(myService.isForeground){
            myService.stopForeground(true);
            myService.isForeground = false;
        }



    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(MainActivity.class.getName(), "onNewIntent called");
        setIntent(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        headsetStatus = (TextView) findViewById(R.id.headphone_status);
        elapsedTime = (TextView) findViewById(R.id.time_elapsed);

        Intent serviceIntent = new Intent(this, HeadphoneService.class);
        //  i.putExtra("MyInterface", mInterface);
        startService(serviceIntent);
        mConnection = initializeServiceConnection();
        bindService(serviceIntent, mConnection, 0);
        Log.d(MainActivity.class.getName(), "onCreate called");
        Log.d(MainActivity.class.getName(), "is mConnection null " + (mConnection == null));


    }

    boolean firstStart;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(MainActivity.class.getName(), "onStart called");
    }


    @Override
    public void onPause() {
//        unregisterReceiver(mReceiver);
        super.onPause();
        Log.d(MainActivity.class.getName(), "onPause called");
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", false).apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(MainActivity.class.getName(), "onDestroy called");
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", false).apply();
        startedFromNotification = false;
        if(myService!=null){
            myService.startServiceAsForeground();
        }
    }

    String notificationMessage;

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isActive", true).apply();


        Intent intent = getIntent();
        String message = intent.getStringExtra("status");
        if (message != null) {
            Log.d(MainActivity.class.getName(), "onResume called");
            Log.d(MainActivity.class.getName(), "message from notification = " + message);
            startedFromNotification = true;
            notificationMessage = message;
           /* if (myService == null) {
                initializeServiceConnection();
            }*/
        }

      /*  if(myService!=null){
            if(myService.isForeground){
                myService.stopForeground(true);
                myService.isForeground = false;
            }
        }*/

    }
       /* if(myService==null){
            firstStart = true;
        }*/
}


