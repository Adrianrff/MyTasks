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
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.models.DataModel;
import com.adrapps.mytasks.views.MainActivity;

import java.util.Calendar;

public class AlarmReciever extends BroadcastReceiver {
    private Contract.Model mModel;
    LocalTask task;
    Context context;
    String title = "";
    String notes = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        mModel = new DataModel(context);
        this.context = context;

        Bundle bundle = intent.getBundleExtra(Co.LOCAL_TASK);
        this.task = (LocalTask) bundle.getSerializable(Co.LOCAL_TASK);

        if (task != null) {
            title = task.getTitle();
            notes = task.getNotes() == null ? "" : task.getNotes();
            Calendar now = Calendar.getInstance();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(task.getReminder());
            switch (task.getRepeatMode()) {
                case Co.REMINDER_ONE_TIME:
                    setAndShowNotification();
                    mModel.updateReminder(task.getIntId(), 0);
                    break;

                case Co.REMINDER_DAILY:
                    //WORKING
                    setAndShowNotification();
                    c.add(Calendar.DATE, 1);
                    mModel.updateReminder(task.getId(), c.getTimeInMillis());
                    break;

                case Co.REMINDER_DAILY_WEEKDAYS:
                    if (DateHelper.isTodayWeekday()) {
                        setAndShowNotification();
                        c.add(Calendar.DATE, 1);
                        mModel.updateReminder(task.getId(), c.getTimeInMillis());
                    }
                    break;
                case Co.REMINDER_SAME_DAY_OF_WEEK:
                    setAndShowNotification();
                    c.add(Calendar.DATE, 7);
                    task.setReminderNoID(c.getTimeInMillis());
                    AlarmHelper.setOrUpdateAlarmForDate(task, c.getTimeInMillis(), context);
                    mModel.updateReminder(task.getId(), c.getTimeInMillis());
                    break;

                case Co.REMINDER_SAME_DAY_OF_MONTH:
                    setAndShowNotification();
                    c.add(Calendar.MONTH, 1);
                    task.setReminderNoID(c.getTimeInMillis());
                    AlarmHelper.setOrUpdateAlarmForDate(task, c.getTimeInMillis(), context);
                    mModel.updateReminder(task.getId(), c.getTimeInMillis());
            }
        }
    }

    void setAndShowNotification() {
        Intent detailIntent = new Intent(context, MainActivity.class);
        detailIntent.putExtra(Co.LOCAL_TASK, task);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                (int) System.currentTimeMillis(), detailIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder notifyBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_assignment_late_black_24dp)
                .setContentTitle(context.getString(R.string.task_reminder_notification_title))
                .setContentText(title +
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
