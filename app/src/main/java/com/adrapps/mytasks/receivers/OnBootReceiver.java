package com.adrapps.mytasks.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.models.DataModel;

import java.util.List;

public class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DataModel mModel = new DataModel(context);
        List<LocalTask>  tasks = mModel.getLocalTasks();
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
       if (prefs.getBoolean(Co.DEFAULT_REMINDER_PREF_KEY, true)) {
          AlarmHelper.setDefaultRemindersForAllTasks(context);
       }
       AlarmHelper.setOrUpdateAllReminders(context);
        mModel = null;
    }
}
