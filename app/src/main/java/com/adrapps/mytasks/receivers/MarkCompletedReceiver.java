package com.adrapps.mytasks.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adrapps.mytasks.api_calls.ApiCalls;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;

public class MarkCompletedReceiver extends BroadcastReceiver {


   @Override
   public void onReceive(Context context, Intent intent) {
      LocalTask task = (LocalTask) intent.getBundleExtra(Co.BUNDLED_EXTRA).getSerializable(Co.LOCAL_TASK);
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (task != null) {
         notificationManager.cancel(Co.NOT_ID_SUFIX + task.getIntId());
      }
      Intent i = new Intent(context, ApiCalls.class);
      i.putExtra(Co.BUNDLED_EXTRA, intent.getBundleExtra(Co.BUNDLED_EXTRA));
      context.startService(i);
   }
}
