package com.adrapps.mytasks.api_calls;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.models.DataModel;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.Arrays;


public class ApiCalls extends IntentService {

   private Tasks mService;
   TaskListPresenter mPresenter;
   DataModel mModel;

   public ApiCalls() {
      super("ApiCalls");
   }

   @Override
   protected void onHandleIntent(@Nullable Intent intent) {
      if (intent != null) {
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
         }
         String newStatus, listId, taskId;
         Bundle extras = intent.getBundleExtra(Co.BUNDLED_EXTRA);
         LocalTask localTask = (LocalTask) extras.getSerializable(Co.LOCAL_TASK);
         newStatus = extras.getString(Co.TASK_STATUS);
         if (localTask != null && newStatus != null) {
            mModel = new DataModel(getApplicationContext());
            listId = localTask.getList();
            taskId = localTask.getId();
            Task task;
            try {
               task = mService.tasks().get(listId, taskId).execute();
               if (newStatus.equals(Co.TASK_NEEDS_ACTION)) {
                  task.setCompleted(null);
               }
               task.setStatus(newStatus);
               mModel.updateTaskStatus(localTask.getIntId(), listId, newStatus);
               mModel.updateSyncStatus(localTask.getIntId(), Co.SYNCED);
               mService.tasks().update(listId, task.getId(), task).execute();
            } catch (IOException e) {
               e.printStackTrace();
               showToast("An error occurred");
            }
         }
      }
   }

   private void showToast(String s) {
      Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
   }
}
