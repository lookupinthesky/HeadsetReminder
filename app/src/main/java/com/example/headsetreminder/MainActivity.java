package com.example.headsetreminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static String STATUS_PLUGGED_IN = "HEADPHONE PLUGGED IN";
    private static String STATUS_PLUGGED_OUT = "HEADPHONE PLUGGED OUT";
    HeadphoneReceiver mReceiver;
    TextView headsetStatus;
    TextView elapsedTime;

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        headsetStatus = (TextView) findViewById(R.id.headphone_status);
        elapsedTime = (TextView) findViewById(R.id.time_elapsed);
        mReceiver = new HeadphoneReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, intentFilter);

    }

    private class HeadphoneReceiver extends BroadcastReceiver {


        private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
        private NotificationManager mNotifyManager;
        private static final int NOTIFICATION_ID = 0;
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

        private NotificationCompat.Builder getNotificationBuilder(String title, String content) {

            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(MainActivity.this, PRIMARY_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.drawable.ic_headset);
            return notifyBuilder;

        }

        private void sendNotification(String title, String content) {
            createNotificationChannel();
            NotificationCompat.Builder notifyBuilder = getNotificationBuilder(title, content);
            mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String intentAction = intent.getAction();
            if (!isInitialStickyBroadcast() && intentAction != null && intentAction.equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0: {

                        //Headset unplugged
                        //show notification that headset was unplugged (although only when it was plugged to begin with.)

                        headsetStatus.setText(STATUS_PLUGGED_OUT);
                        sendNotification("Headset UnPlugged", "You've unplugged the headsets");
                        break;

                    }
                    case 1: {

                        //Headset plugged
                        //show notification that headset was plugged
                        headsetStatus.setText(STATUS_PLUGGED_IN);
                        sendNotification("Headset Plugged", "You've plugged the headsets");
                        break;

                    }
                }


            }
        }

        /*private void addNotification() {
         *//* NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(MainActivity.this, PRIMARY_CHANNEL_ID)
                            .setSmallIcon(R.drawable.abc)
                            .setContentTitle("Notifications Example")
                            .setContentText("This is a test notification");*//*

// on notification tap, open activity
            Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);

            //wrapping notification intent into pendingIntent

            PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }*/


    }
}
