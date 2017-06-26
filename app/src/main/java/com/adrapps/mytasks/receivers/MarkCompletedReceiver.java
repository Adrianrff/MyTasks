package com.adrapps.mytasks.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adrapps.mytasks.api_calls.MarkCompleteFromNotification;
import com.adrapps.mytasks.domain.Co;

public class MarkCompletedReceiver extends BroadcastReceiver {


   @Override
   public void onReceive(Context context, Intent intent) {
//      LocalTask task = (LocalTask) intent.getBundleExtra(Co.BUNDLED_EXTRA).getSerializable(Co.LOCAL_TASK);
      String listId = intent.getStringExtra(Co.TASK_LIST_ID);
      String taskId = intent.getStringExtra(Co.TASK_ID);
      int taskIntId = intent.getIntExtra(Co.TASK_INT_ID, -1);
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (taskIntId >= 0) {
         notificationManager.cancel(Co.NOT_ID_SUFIX + taskIntId);
      }
      Intent i = new Intent(context, MarkCompleteFromNotification.class);
      i.putExtra(Co.TASK_ID, taskId);
      i.putExtra(Co.TASK_INT_ID, taskIntId);
      i.putExtra(Co.TASK_LIST_ID, listId);
      context.startService(i);
   }
}
