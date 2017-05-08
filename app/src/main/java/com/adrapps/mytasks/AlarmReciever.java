package com.adrapps.mytasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.models.DataModel;
import com.adrapps.mytasks.views.MainActivity;

public class AlarmReciever extends BroadcastReceiver {
    private Contract.Model mModel;

    @Override
    public void onReceive(Context context, Intent intent) {
        mModel = new DataModel(context);
        String title = "";
        String notes = "";
        Intent detailIntent = new Intent(context, MainActivity.class);
        Bundle bundle = intent.getBundleExtra(Co.LOCAL_TASK);
        LocalTask task = (LocalTask) bundle.getSerializable(Co.LOCAL_TASK);
        detailIntent.putExtra(Co.LOCAL_TASK, task);
        if (task != null) {
            title = task.getTitle();
            notes = task.getNotes() == null ? "" : task.getNotes();
            mModel.updateReminder(task.getId(), 0);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                (int) System.currentTimeMillis(),detailIntent,PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder notifyBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_assignment_late_black_24dp)
                .setContentTitle(context.getString(R.string.task_reminder_notification_title))
                .setContentText(title  +
                        " - " + context.getString(R.string.touch_for_details))
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new Notification.BigTextStyle().bigText((title + "\n" +
                        notes)))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
                notify((int) System.currentTimeMillis(), notifyBuilder.build());
    }

}
