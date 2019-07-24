package com.example.notify;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "Activity";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "" +
                "");
        wakeLock.acquire();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SaveEvent.class));
            context.startForegroundService(new Intent(context, ExpandNotification.class));
        } else {
            context.startService(new Intent(context, SaveEvent.class));
            context.startService(new Intent(context, ExpandNotification.class));
        }

        String event_name = intent.getStringExtra("name");
        String event_location = intent.getStringExtra("location");
        String event_date = intent.getStringExtra("date");

        // create intent to be passed to next class when user clicks on the notification
        Intent notificationIntent = new Intent(context, SaveEvent.class);
        notificationIntent.putExtra("event_name", event_name);
        notificationIntent.putExtra("event_location", event_location);
        notificationIntent.putExtra("event_date", event_date);
        notificationIntent.putExtra("ActionType", "notification");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SaveEvent.class);
        stackBuilder.addNextIntent(notificationIntent);
        Log.d(TAG, "NotificationReceiver: ");
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(100, PendingIntent.FLAG_UPDATE_CURRENT);

        // intent for notification action
        Intent intentAction = new Intent(context, ExpandNotification.class);
        notificationIntent.putExtra("event_name", event_name);
        notificationIntent.putExtra("event_location", event_location);
        notificationIntent.putExtra("event_date", event_date);
        intentAction.putExtra("ActionType", "map");
        TaskStackBuilder stackBuilder1 = TaskStackBuilder.create(context);
        stackBuilder1.addParentStack(ExpandNotification.class);
        stackBuilder1.addNextIntent(intentAction);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, intentAction, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "");
        builder.setContentTitle(event_name);
        builder.setContentText(event_location + " " + event_date);
        builder.setTicker("Event Alert");
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_camera_black_24dp);
        builder.setContentIntent(pendingIntent);
        builder.addAction(R.drawable.ic_location_icon, "Show nagivation", actionPendingIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
        wakeLock.release();
    }

    public void setAlarm(Context context, Date date, Intent intent)  {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = new Date();
        String datestr= formatter.format(date1);
        Date todaysDate = null;
        try {
            todaysDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datestr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();

        c.add(Calendar.MINUTE, 2);
        Log.d(TAG, String.valueOf(c.getTimeInMillis()));
        Long minutesdifference = new Long(TimeUnit.MILLISECONDS.toMinutes(date.getTime() - todaysDate.getTime()));
        Log.d(TAG, String.valueOf(minutesdifference.intValue()));
        c1.add(Calendar.MINUTE, minutesdifference.intValue());

        long tiggerTime = c1.getTimeInMillis();
        Log.d(TAG, String.valueOf(tiggerTime));

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiggerTime, pendingIntent);
    }
}