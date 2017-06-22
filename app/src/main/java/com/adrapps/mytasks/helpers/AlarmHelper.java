package com.adrapps.mytasks.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.models.DataModel;
import com.adrapps.mytasks.receivers.AlarmReciever;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class AlarmHelper {

   public static void setOrUpdateAlarm(LocalTask task, Context context) {
      if (task != null) {
         if (task.getReminder() != 0 && task.getReminderId() != 0) {
            DataModel mModel = new DataModel(context);
            Intent intent = new Intent(context, AlarmReciever.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Co.LOCAL_TASK, task);
            intent.putExtra(Co.LOCAL_TASK, bundle);
            long reminder = task.getReminder();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) task.getReminderId(), intent,
                  PendingIntent.FLAG_UPDATE_CURRENT);

            switch (task.getRepeatMode()) {

               case Co.REMINDER_ONE_TIME:
                  //NO RESET IN RECEIVER
                  alarmManager.set(AlarmManager.RTC_WAKEUP,
                        reminder, pendingIntent);
                  break;

               case Co.REMINDER_DAILY:
                  //NO RESET IN RECEIVER
                  //WORKING
                  alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        reminder, Co.ONE_DAY_LATER, pendingIntent);
                  mModel.updateReminder(task.getIntId(), reminder, task.getRepeatMode());
                  break;

               case Co.REMINDER_DAILY_WEEKDAYS:
                  //NO RESET IN RECEIVER BUT CHECKS IF ITS WEEKDAY
                  alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        reminder, Co.ONE_DAY_LATER, pendingIntent);
                  mModel.updateReminder(task.getIntId(), reminder, task.getRepeatMode());
                  break;

               case Co.REMINDER_WEEKLY:
                  //RESET IN RECEIVER
                  alarmManager.set(AlarmManager.RTC_WAKEUP,
                        reminder, pendingIntent);
                  break;

               case Co.REMINDER_MONTHLY:
                  //RESET IN RECEIVER
                  alarmManager.set(AlarmManager.RTC_WAKEUP,
                        reminder, pendingIntent);
                  break;
            }
         }
      }
   }

   public static void cancelTaskReminder(LocalTask task, Context context) {
      Intent intent = new Intent(context, AlarmReciever.class);
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
            (int) task.getReminderId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
      alarmManager.cancel(pendingIntent);
      pendingIntent.cancel();
   }

   public static void setOrUpdateAlarmForDate(LocalTask task, long timeOfAlarm, Context context) {
      Intent intent = new Intent(context, AlarmReciever.class);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) task.getReminderId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
      AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(ALARM_SERVICE);
      alarmManager.set(AlarmManager.RTC_WAKEUP,
            timeOfAlarm, pendingIntent);
   }

   public void showToast(Context context, String msg) {
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
   }

   public static void setOrUpdateAlarmFirstTime(LocalTask task, Context context) {
      if (task != null) {
         if (task.getReminder() != 0 && task.getReminderId() != 0) {
            DataModel mModel = new DataModel(context);
            Intent intent = new Intent(context, AlarmReciever.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Co.LOCAL_TASK, task);
            intent.putExtra(Co.LOCAL_TASK, bundle);
            long reminder = task.getReminder();
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) task.getReminderId(), intent,
                  PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar reminderCalendarObject = Calendar.getInstance();
            reminderCalendarObject.setTimeInMillis(reminder);
            Calendar today = Calendar.getInstance();

            switch (task.getRepeatMode()) {

               case Co.REMINDER_ONE_TIME:
                  if (today.getTimeInMillis() > reminder) {
                     break;
                  }
                  alarmManager.set(AlarmManager.RTC_WAKEUP,
                        reminder, pendingIntent);
                  break;

               case Co.REMINDER_DAILY:
                  if (today.getTimeInMillis() > reminder) {
                     reminderCalendarObject.set(Calendar.YEAR, today.get(Calendar.YEAR));
                     reminderCalendarObject.set(Calendar.MONTH, today.get(Calendar.MONTH));
                     reminderCalendarObject.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                     reminderCalendarObject.add(Calendar.DATE, 1);
                     mModel.updateReminder(task.getIntId(), reminderCalendarObject.getTimeInMillis(), task.getRepeatMode());
                  }
                  alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        reminderCalendarObject.getTimeInMillis(), Co.ONE_DAY_LATER, pendingIntent);
                  mModel.updateReminder(task.getIntId(), reminderCalendarObject.getTimeInMillis(), task.getRepeatMode());
                  break;

               case Co.REMINDER_DAILY_WEEKDAYS:
                  if (today.getTimeInMillis() > reminder) {
                     reminderCalendarObject.set(Calendar.YEAR, today.get(Calendar.YEAR));
                     reminderCalendarObject.set(Calendar.MONTH, today.get(Calendar.MONTH));
                     reminderCalendarObject.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
                     reminderCalendarObject.add(Calendar.DATE, 1);
                     mModel.updateReminder(task.getIntId(), reminderCalendarObject.getTimeInMillis(), task.getRepeatMode());
                  }
                  alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        reminderCalendarObject.getTimeInMillis(), Co.ONE_DAY_LATER, pendingIntent);
                  mModel.updateReminder(task.getIntId(), reminder, task.getRepeatMode());
                  break;

               case Co.REMINDER_WEEKLY:
                  //RESET IN RECEIVER
                  if (today.getTimeInMillis() > reminder) {
                     reminderCalendarObject.set(Calendar.YEAR, today.get(Calendar.YEAR));
                     reminderCalendarObject.set(Calendar.MONTH, today.get(Calendar.MONTH));
                     reminderCalendarObject.add(Calendar.DATE, 7);
                     mModel.updateReminder(task.getIntId(), reminderCalendarObject.getTimeInMillis(), task.getRepeatMode());
                  }
                  alarmManager.set(AlarmManager.RTC_WAKEUP,
                        reminderCalendarObject.getTimeInMillis(), pendingIntent);
                  break;

               case Co.REMINDER_MONTHLY:
                  if (today.getTimeInMillis() > reminder) {
                     reminderCalendarObject.set(Calendar.YEAR, today.get(Calendar.YEAR));
                     reminderCalendarObject.set(Calendar.MONTH, today.get(Calendar.MONTH));
                     reminderCalendarObject.add(Calendar.MONTH, 1);
                     mModel.updateReminder(task.getIntId(), reminderCalendarObject.getTimeInMillis(), task.getRepeatMode());
                  }
                  alarmManager.set(AlarmManager.RTC_WAKEUP,
                        reminderCalendarObject.getTimeInMillis(), pendingIntent);
                  break;
            }
         }
      }
   }

   public static void setDefaultRemindersForAllTasks(final Context context) {
      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
      final int defaultReminderHour = pref.getInt(Co.DEFAULT_REMINDER_TIME_PREF_KEY, 8);
      final Calendar now = Calendar.getInstance();
      final DataModel model = new DataModel(context);
      AsyncTask.execute(new Runnable() {
         @Override
         public void run() {
            List<LocalTask> tasks = model.getLocalTasksFromDB();
            for (LocalTask task : tasks) {
               if (!task.getStatus().equals(Co.TASK_COMPLETED)) {
                  if (task.getDue() != 0) {
                     Calendar reminderDate = Calendar.getInstance();
                     reminderDate.setTimeInMillis(task.getDue());
                     if (!reminderDate.before(now)) {
                        reminderDate.set(Calendar.HOUR_OF_DAY, defaultReminderHour);
                        reminderDate.set(Calendar.MINUTE, 0);
                        reminderDate.set(Calendar.SECOND, 0);
                        reminderDate.set(Calendar.MILLISECOND, 0);
                        Intent intent = new Intent(context.getApplicationContext(), AlarmReciever.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Co.LOCAL_TASK, task);
                        intent.putExtra(Co.LOCAL_TASK, bundle);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                              Co.DEFAULT_REMINDER_INDENTIFIER + task.getIntId(), intent,
                              PendingIntent.FLAG_UPDATE_CURRENT);
                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                              reminderDate.getTimeInMillis(), pendingIntent);
                     }
                  }
               }
            }
         }
      });
   }


   //TODO create method for setting or updating default reminder for individual task
   //Triggers should be deleting, creating or changing the dueDate of a task, changing the default time (changes all tasks)
   //marking a task as completed, passing the due date.

   public static void cancelDefaultRemindersForAllTasks(final Context context) {
      final DataModel model = new DataModel(context);

      AsyncTask.execute(new Runnable() {
         @Override
         public void run() {
            List<LocalTask> tasks = model.getLocalTasksFromDB();
            for (LocalTask task : tasks) {
               int id = Co.DEFAULT_REMINDER_INDENTIFIER + task.getIntId();
               if (isAlarmSet(context, id)) {
                  Intent intent = new Intent(context.getApplicationContext(), AlarmReciever.class);
                  AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                  PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                        id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                  alarmManager.cancel(pendingIntent);
                  pendingIntent.cancel();
               }
            }
         }
      });

   }

   private static boolean isAlarmSet(Context context, int id) {
      return (PendingIntent.getBroadcast(context, id,
            new Intent(context, AlarmReciever.class),
            PendingIntent.FLAG_NO_CREATE) != null);
   }
}
