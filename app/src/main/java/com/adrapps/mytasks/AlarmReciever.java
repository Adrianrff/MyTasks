package com.adrapps.mytasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.adrapps.mytasks.Domain.Co;

public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context,"Alarm recieved",Toast.LENGTH_LONG).show();
        Notification.Builder notifyBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_assignment_late_black_24dp)
                .setContentTitle(context.getString(R.string.task_reminder_notification_title))
                .setContentText(context.getString(R.string.uncompleted_task_text) + " " +
                        intent.getStringExtra(Co.TASK_DUE))
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new Notification.BigTextStyle().bigText(context.getString(R.string.uncompleted_task_text) + " " +
                        intent.getStringExtra(Co.TASK_DUE) + ".\n" +
                context.getString(R.string.touch_for_details)));
//                .setContentIntent(pendingIntent);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
                notify(1522, notifyBuilder.build());
    }
}
