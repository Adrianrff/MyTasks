package com.adrapps.mytasks.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.adrapps.mytasks.receivers.AlarmReciever;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.models.DataModel;

import java.util.Calendar;

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

    public static void cancelReminder(LocalTask task, Context context) {
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
                        if (today.getTimeInMillis() > reminder){
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
}
