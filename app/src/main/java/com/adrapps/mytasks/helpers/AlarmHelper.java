package com.adrapps.mytasks.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.adrapps.mytasks.AlarmReciever;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.models.DataModel;

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
;

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
                        mModel.updateReminder(task.getIntId(), reminder);
                        break;

                    case Co.REMINDER_DAILY_WEEKDAYS:
                        //NO RESET IN RECEIVER BUT CHECKS IF ITS WEEKDAY
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                reminder, Co.ONE_DAY_LATER, pendingIntent);
                        mModel.updateReminder(task.getIntId(), reminder);
                        break;

                    case Co.REMINDER_SAME_DAY_OF_WEEK:
                        //RESET IN RECEIVER
                        alarmManager.set(AlarmManager.RTC_WAKEUP,
                                reminder, pendingIntent);
                        break;

                    case Co.REMINDER_SAME_DAY_OF_MONTH:
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
}
