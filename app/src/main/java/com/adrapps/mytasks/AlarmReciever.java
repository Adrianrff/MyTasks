package com.adrapps.mytasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.adrapps.mytasks.Domain.Co;

public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
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
                notify((int) System.currentTimeMillis(), notifyBuilder.build());
    }
}
