package com.adrapps.mytasks.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.models.DataModel;
import com.adrapps.mytasks.views.MainActivity;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Calendar;

public class AlarmReciever extends BroadcastReceiver {
   private Contract.Model mModel;
   LocalTask task;
   Context context;
   int intId;
   int repeatMode;
   String title = "";
   String notes = "";
   private final String TAG = "Alarm reciever";

   @Override
   public void onReceive(Context context, Intent intent) {
      mModel = new DataModel(context.getApplicationContext());
      this.context = context.getApplicationContext();

      Bundle bundle = intent.getBundleExtra(Co.LOCAL_TASK);
      this.task = (LocalTask) bundle.getSerializable(Co.LOCAL_TASK);

      if (task != null) {
         title = task.getTitle();
         notes = task.getNotes() == null ? "" : task.getNotes();
         intId = task.getIntId();
         Calendar c = Calendar.getInstance();
         c.setTimeInMillis(task.getReminder());

         switch (task.getRepeatMode()) {
            case Co.REMINDER_ONE_TIME:
               task.setReminderNoID(0);
               mModel.updateReminder(intId, 0, task.getRepeatMode());
               setAndShowNotification();
               break;

            case Co.REMINDER_DAILY:
               c.add(Calendar.DATE, 1);
               task.setReminderNoID(c.getTimeInMillis());
               mModel.updateReminder(intId, c.getTimeInMillis(), repeatMode);
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
               mModel.updateReminder(intId, c.getTimeInMillis(), repeatMode);
               if (DateHelper.isWeekday(today)) {
                  setAndShowNotification();
               }
               break;

            case Co.REMINDER_WEEKLY:
               c.add(Calendar.DATE, 7);
               task.setReminderNoID(c.getTimeInMillis());
               mModel.updateReminder(intId, c.getTimeInMillis(), repeatMode);
               AlarmHelper.setOrUpdateAlarmForDate(task, c.getTimeInMillis(), context);
               setAndShowNotification();
               break;

            case Co.REMINDER_MONTHLY:
               c.add(Calendar.MONTH, 1);
               task.setReminderNoID(c.getTimeInMillis());
               mModel.updateReminder(intId, c.getTimeInMillis(), repeatMode);
               AlarmHelper.setOrUpdateAlarmForDate(task, c.getTimeInMillis(), context);
               setAndShowNotification();
         }
      }
   }

   void setAndShowNotification() {
      try {
         Intent detailActivityIntent = new Intent(context, MainActivity.class);
         Intent markCompletedIntent = new Intent(context, MarkCompletedReceiver.class);
         Bundle extras = new Bundle();
         extras.putSerializable(Co.LOCAL_TASK, task);
         extras.putString(Co.TASK_STATUS, Co.TASK_COMPLETED);
         markCompletedIntent.putExtra(Co.TASK_ID, task.getId());
         markCompletedIntent.putExtra(Co.TASK_INT_ID, task.getIntId());
         markCompletedIntent.putExtra(Co.TASK_LIST_ID, task.getListId());
         detailActivityIntent.putExtra(Co.LOCAL_TASK, task);
         TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
         stackBuilder.addParentStack(MainActivity.class);
         stackBuilder.addNextIntent(detailActivityIntent);
         PendingIntent resultPendingIntent =
               stackBuilder.getPendingIntent(Co.NOT_ID_SUFIX + task.getIntId(), PendingIntent.FLAG_UPDATE_CURRENT);
         PendingIntent markCompletedPendingIntent = PendingIntent.getBroadcast(context,
               (int) task.getReminderId(), markCompletedIntent, PendingIntent.FLAG_ONE_SHOT);
         NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context);
         notifyBuilder.setSmallIcon(R.mipmap.ic_task_reminder);
         notifyBuilder.setContentTitle(context.getString(R.string.task_reminder_notification_title))
               .setContentText(title +
                     " - " + context.getString(R.string.touch_for_details))

               .setLights(ContextCompat.getColor(context, R.color.colorPrimary), 700, 4000);
         if (getBooleanSharedPreference(Co.VIBRATE_REMINDER_PREF_KEY, false)) {
            notifyBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
         } else {
            notifyBuilder.setVibrate(null);
         }
         if (task.getNotes() != null) {
            notifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText((title + "\n" + notes)));
         }
         String ringtoneUri = getStringSharedPreference(Co.REMINDER_RINGTONE_PREF_KEY, "default");
         if (ringtoneUri.equals("default")) {
            notifyBuilder.setDefaults(Notification.DEFAULT_SOUND);
         } else {
            notifyBuilder.setSound(Uri.parse(ringtoneUri));
         }
         Log.d(TAG, "ringtoneValue: " + ringtoneUri);
         notifyBuilder.addAction(R.mipmap.ic_task_completed, "Completada", markCompletedPendingIntent)
               .setContentIntent(resultPendingIntent)
               .setAutoCancel(true)
               .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
         ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
               notify(Co.NOT_ID_SUFIX + task.getIntId(), notifyBuilder.build());
      } catch (Exception e) {
         e.printStackTrace();
         FirebaseCrash.report(e);
      }
   }

   private String getStringSharedPreference(String key, String defaultValue) {
      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
      return pref.getString(key, defaultValue);
   }

   private boolean getBooleanSharedPreference(String key, boolean defaultValue) {
      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
      return pref.getBoolean(key, defaultValue);
   }
}
