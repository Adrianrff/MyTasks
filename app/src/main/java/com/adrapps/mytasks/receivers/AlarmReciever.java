package com.adrapps.mytasks.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.adrapps.mytasks.R;
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
               task.setReminderNoID(0);
               mModel.updateReminder(task.getIntId(), 0, task.getRepeatMode());
               setAndShowNotification();
               break;

            case Co.REMINDER_DAILY:
               //WORKING
               c.add(Calendar.DATE, 1);
               task.setReminderNoID(c.getTimeInMillis());
               mModel.updateReminder(task.getId(), c.getTimeInMillis());
               setAndShowNotification();
               break;

            case Co.REMINDER_DAILY_WEEKDAYS:
               Calendar today = Calendar.getInstance();
               if (DateHelper.isTomorrowWeekday()) {
                  c.add(Calendar.DATE, 1);
               } else {
                  c.add(Calendar.DATE, today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ? 3 : 2);
               }
               task.setReminderNoID(c.getTimeInMillis());
               mModel.updateReminder(task.getId(), c.getTimeInMillis());
               if (DateHelper.isWeekday(today)) {
                  setAndShowNotification();
               }
               break;

            case Co.REMINDER_WEEKLY:
               c.add(Calendar.DATE, 7);
               task.setReminderNoID(c.getTimeInMillis());
               mModel.updateReminder(task.getId(), c.getTimeInMillis());
               AlarmHelper.setOrUpdateAlarmForDate(task, c.getTimeInMillis(), context);
               setAndShowNotification();
               break;

            case Co.REMINDER_MONTHLY:
               c.add(Calendar.MONTH, 1);
               task.setReminderNoID(c.getTimeInMillis());
               mModel.updateReminder(task.getId(), c.getTimeInMillis());
               AlarmHelper.setOrUpdateAlarmForDate(task, c.getTimeInMillis(), context);
               setAndShowNotification();
         }
      }
   }

   void setAndShowNotification() {
      Intent detailActivityIntent = new Intent(context, MainActivity.class);
      Intent markCompletedIntent = new Intent(context, MarkCompletedReceiver.class);
      Bundle extras = new Bundle();
      extras.putSerializable(Co.LOCAL_TASK, task);
      extras.putString(Co.TASK_STATUS, Co.TASK_COMPLETED);
      markCompletedIntent.putExtra(Co.BUNDLED_EXTRA, extras);
      detailActivityIntent.putExtra(Co.LOCAL_TASK, task);
      TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
      stackBuilder.addParentStack(MainActivity.class);
      stackBuilder.addNextIntent(detailActivityIntent);
      PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(Co.NOT_ID_SUFIX + task.getIntId(), PendingIntent.FLAG_UPDATE_CURRENT);
      PendingIntent markCompletedPendingIntent = PendingIntent.getBroadcast(context,
            (int) task.getReminderId(), markCompletedIntent, PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder notifyBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.ic_assignment_late_black_24dp)
            .setContentTitle(context.getString(R.string.task_reminder_notification_title))
            .setContentText(title +
                  " - " + context.getString(R.string.touch_for_details))
            .setDefaults(Notification.DEFAULT_ALL);
      if (task.getNotes() != null) {
         notifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText((title + "\n" + notes)));
      }
      notifyBuilder.addAction(R.mipmap.ic_completed, "Completada", markCompletedPendingIntent)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
                  ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
            notify(Co.NOT_ID_SUFIX + task.getIntId(), notifyBuilder.build());
   }

}
