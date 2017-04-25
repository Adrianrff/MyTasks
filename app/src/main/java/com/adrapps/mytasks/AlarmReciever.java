package com.adrapps.mytasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.Views.NewOrDetailActivity;

public class AlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent detailIntent = new Intent(context, NewOrDetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                (int) System.currentTimeMillis(),detailIntent,PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder notifyBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_assignment_late_black_24dp)
                .setContentTitle(context.getString(R.string.task_reminder_notification_title))
                .setContentText(intent.getStringExtra(Co.TASK_TITLE))
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new Notification.BigTextStyle().bigText("Your task " +
                                intent.getStringExtra(Co.TASK_TITLE) + " is due"))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
                notify((int) System.currentTimeMillis(), notifyBuilder.build());
    }
}
