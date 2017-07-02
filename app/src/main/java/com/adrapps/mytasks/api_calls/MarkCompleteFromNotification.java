package com.adrapps.mytasks.api_calls;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.models.DataModel;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Arrays;


public class MarkCompleteFromNotification extends IntentService {

   public MarkCompleteFromNotification() {
      super("MarkCompleteFromNotification");
   }

   @Override
   protected void onHandleIntent(@Nullable Intent intent) {
      if (intent != null) {
         DataModel mModel;
         Tasks mService;
         String listId = intent.getStringExtra(Co.TASK_LIST_ID);
         String taskId = intent.getStringExtra(Co.TASK_ID);
         int taskIntId = intent.getIntExtra(Co.TASK_INT_ID, -1);
         if (taskIntId > 0) {
            mModel = new DataModel(getApplicationContext());
            LocalTask localTask = mModel.getTask(taskIntId);
            localTask.setStatus(Co.TASK_COMPLETED);
            localTask.setLocalModify();
            mModel.updateTaskStatusInDb(taskIntId, Co.TASK_COMPLETED);
            GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(
                  getApplicationContext(), Arrays.asList(Co.SCOPES))
                  .setBackOff(new ExponentialBackOff());
            SharedPreferences prefs = PreferenceManager
                  .getDefaultSharedPreferences(getApplicationContext());
            String accountName = prefs.getString(Co.USER_EMAIL, null);
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            if (accountName != null && !accountName.equals(Co.NO_ACCOUNT_NAME)) {
               mCredential.setSelectedAccountName(accountName);
            }

            if (mCredential != null) {
               mService = new com.google.api.services.tasks.Tasks.Builder(
                     transport, jsonFactory, mCredential)
                     .setApplicationName("My Tasks")
                     .build();

               if (listId != null && taskId != null && taskIntId > 0) {
                  Task task = null;
                  boolean deviceOnline;
                  ConnectivityManager connMgr =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                  NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                  deviceOnline = (networkInfo != null && networkInfo.isConnected());
                  if (deviceOnline) {
                     try {
                        task = mService.tasks().get(listId, taskId).execute();
                     } catch (Exception e) {
                        FirebaseCrash.report(e);
                     }
                     if (task != null) {
                        task.setStatus(Co.TASK_COMPLETED);
                        Task updatedServerTask = null;
                        try {
                           updatedServerTask = mService.tasks().update(listId, taskId, task).execute();
                        } catch (Exception e) {
                           showToast("An error occurred while performing the action");
                           FirebaseCrash.report(e);
                        }
                        if (updatedServerTask != null) {
                           localTask.setLocalModify(task.getUpdated().getValue());
                        }
                     }
                  }
                  mModel.updateExistingTaskFromLocalTask(localTask);

               } else {
                  try {
                     throw new Exception("Task parameters where invalid" + "\n" +
                           "List ID: " + listId + "\n" +
                           "Task ID: " + taskId + "\n" +
                           "Int ID: " + String.valueOf(taskIntId));
                  } catch (Exception e) {
                     FirebaseCrash.report(e);
                  }
               }
            }
         } else {
            try {
               throw new Exception("Task returning null in MarkCompletedFromNotification");
            } catch (Exception e) {
               e.printStackTrace();
               FirebaseCrash.report(e);
            }

         }

      }
   }

   private void showToast(String s) {
      Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
   }
}
