package com.adrapps.mytasks.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.models.DataModel;
import com.adrapps.mytasks.receivers.AlarmReciever;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class ReminderManager {

   private ReminderManager instance;
   private Context context;
   private DataModel model;

   private ReminderManager(Context context) {
      this.context = context.getApplicationContext();
      this.model = new DataModel(context.getApplicationContext());
   }

   public ReminderManager getInstance(Context context){
      if (instance == null){
         return new ReminderManager(context);
      } else {
         return instance;
      }
   }


   private void recreateAllReminders(){
      final List<LocalTask> tasks = model.getLocalTasks();
      Handler handler = new Handler(Looper.getMainLooper());
      handler.post(new Runnable() {
         @Override
         public void run() {
            for (LocalTask task: tasks) {
               if (task != null) {
                  setOrUpdateReminderForTask(task);
               }
            }
         }
      });

   }

   private void setOrUpdateReminderForTask(LocalTask task){
      if (task.getReminder() != 0 && task.getReminderId() != 0 && !task.getStatus().equals(Co.TASK_COMPLETED)) {
         long reminder = task.getReminder();
         int repeatMode = task.getRepeatMode();
         int intId = task.getIntId();
         int reminderId = (int) task.getReminderId();
         if (DateHelper.isInThePast(reminder)){
            switch (repeatMode){
               case Co.REMINDER_DAILY:
                  reminder = getNextDailyReminder(reminder);
                  model.updateReminder(intId, reminder, repeatMode);
                  break;

               case Co.REMINDER_DAILY_WEEKDAYS:
                  reminder = getNextDailyWeekdayReminder(reminder);
                  model.updateReminder(intId, reminder, repeatMode);
                  task.setReminderNoID(reminder);
                  break;

               case Co.REMINDER_WEEKLY:
                  reminder = getNextWeeklyReminder(reminder);
                  model.updateReminder(intId, reminder, repeatMode);
                  task.setReminderNoID(reminder);
                  break;

               case Co.REMINDER_MONTHLY:
                  reminder = getNextMonthlyReminder(reminder);
                  model.updateReminder(intId, reminder, repeatMode);
                  task.setReminderNoID(reminder);
                  break;

               case Co.REMINDER_ONE_TIME:
                  model.updateReminder(intId,0,Co.REMINDER_ONE_TIME);
                  task.setReminderNoID(reminder);
                  return;
            }
         }
         Intent intent = new Intent(context, AlarmReciever.class);
         Bundle bundle = new Bundle();
         bundle.putSerializable(Co.LOCAL_TASK, task);
         intent.putExtra(Co.LOCAL_TASK, bundle);
         AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent,
               PendingIntent.FLAG_UPDATE_CURRENT);

         switch (repeatMode) {
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
               break;

            case Co.REMINDER_DAILY_WEEKDAYS:
               //NO RESET IN RECEIVER BUT CHECKS IF ITS WEEKDAY
               alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                     reminder, Co.ONE_DAY_LATER, pendingIntent);
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

   private long getNextMonthlyReminder(long reminder) {
      Calendar oldReminder = Calendar.getInstance();
      oldReminder.setTimeInMillis(reminder);
      Calendar newReminder = Calendar.getInstance();
      Calendar now = Calendar.getInstance();
      int hour = oldReminder.get(Calendar.HOUR_OF_DAY);
      int minute = oldReminder.get(Calendar.MINUTE);
      int dayOfMonth = oldReminder.get(Calendar.DAY_OF_MONTH);
      newReminder.set(Calendar.HOUR_OF_DAY, hour);
      newReminder.set(Calendar.MINUTE, minute);
      newReminder.set(Calendar.SECOND, 0);
      newReminder.set(Calendar.DAY_OF_MONTH, dayOfMonth);
      if (newReminder.before(now)){
         newReminder.add(Calendar.MONTH, 1);
      }
      return newReminder.getTimeInMillis();
   }

   private long getNextWeeklyReminder(long reminder) {
      Calendar oldReminder = Calendar.getInstance();
      oldReminder.setTimeInMillis(reminder);
      Calendar newReminder = Calendar.getInstance();
      Calendar now = Calendar.getInstance();
      int hour = oldReminder.get(Calendar.HOUR_OF_DAY);
      int minute = oldReminder.get(Calendar.MINUTE);
      int dayOfWeek = oldReminder.get(Calendar.DAY_OF_WEEK);
      newReminder.set(Calendar.HOUR_OF_DAY, hour);
      newReminder.set(Calendar.MINUTE, minute);
      newReminder.set(Calendar.SECOND, 0);
      if (dayOfWeek != newReminder.get(Calendar.DAY_OF_WEEK)) {
         while (newReminder.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            newReminder.add(Calendar.DATE, 1);
         }
      }
      if (newReminder.before(now)){
         newReminder.add(Calendar.DATE, 7);
      }
      return newReminder.getTimeInMillis();
   }

   private long getNextDailyWeekdayReminder(long reminder) {
      Calendar oldReminder = Calendar.getInstance();
      oldReminder.setTimeInMillis(reminder);
      Calendar newReminder = Calendar.getInstance();
      Calendar now = Calendar.getInstance();
      int hour = oldReminder.get(Calendar.HOUR_OF_DAY);
      int minute = oldReminder.get(Calendar.MINUTE);
      //Set new reminder to today's date but with old reminder hour and minute
      newReminder.set(Calendar.HOUR_OF_DAY, hour);
      newReminder.set(Calendar.MINUTE, minute);
      newReminder.set(Calendar.SECOND, 0);
      if (newReminder.before(now)){
         if (DateHelper.isTomorrowWeekday()) {
            newReminder.add(Calendar.DATE, 1);
         } else {
            newReminder.add(Calendar.DATE, now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ? 3 : 2);
         }
      }
      return newReminder.getTimeInMillis();
   }

   private long getNextDailyReminder(long reminder) {
      Calendar oldReminder = Calendar.getInstance();
      oldReminder.setTimeInMillis(reminder);
      Calendar newReminder = Calendar.getInstance();
      Calendar now = Calendar.getInstance();
      int hour = oldReminder.get(Calendar.HOUR_OF_DAY);
      int minute = oldReminder.get(Calendar.MINUTE);
      newReminder.set(Calendar.HOUR_OF_DAY, hour);
      newReminder.set(Calendar.MINUTE, minute);
      newReminder.set(Calendar.SECOND, 0);
      if (newReminder.before(now)){
         newReminder.add(Calendar.DATE, 1);
      }
      return newReminder.getTimeInMillis();
   }
}
