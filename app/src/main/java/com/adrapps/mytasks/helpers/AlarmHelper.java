package com.adrapps.mytasks.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.adrapps.mytasks.AlarmReciever;
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
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) task.getReminderId(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar calendar = Calendar.getInstance();

                switch (task.getRepeatMode()) {

                    case Co.REMINDER_ONE_TIME:
                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                task.getReminder(), pendingIntent);
                        break;

                    case Co.REMINDER_DAILY:
                        Log.d("repeatMode", "Daily");
                            calendar.setTimeInMillis(task.getReminder());
                        if (Calendar.getInstance().getTimeInMillis() > task.getReminder()) {
                            calendar.add(Calendar.DATE, 1);
                        }
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(), Co.ONE_DAY_LATER, pendingIntent);
                        mModel.updateReminder(task.getIntId(), calendar.getTimeInMillis());
                        break;

                    case Co.REMINDER_DAILY_WEEKDAYS:
                        Log.d("repeatMode", "Weekdays");
                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                task.getReminder(), pendingIntent);
                        mModel.updateReminder(task.getIntId(), task.getReminder());
                        break;

                    case Co.REMINDER_SAME_DAY_OF_WEEK:
                        Log.d("repeatMode", "Same day of week");
                        calendar.setTimeInMillis(task.getReminder());
                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(), pendingIntent);
                        break;

                    case Co.REMINDER_SAME_DAY_OF_MONTH:
                        Log.d("repeatMode", "Same day of month");
                        //TODO: set alarm for once a month
                        calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(task.getReminder());
                        if (Calendar.getInstance().getTimeInMillis() > task.getReminder()) {
                            return;
                        }

                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(), pendingIntent);
                        mModel.updateReminder(task.getIntId(), calendar.getTimeInMillis());
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
}
