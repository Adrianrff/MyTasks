package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.helpers.GoogleApiHelper;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class FirstRefreshAsync extends AsyncTask<Void, Void, Void> {

   private com.google.api.services.tasks.Tasks mService = null;
   private Exception mLastError = null;
   private TaskListPresenter mPresenter;
   private List<LocalTask> localTasks = new ArrayList<>();
   private List<TaskList> serverLists = new ArrayList<>();
   private static final String TAG = "FirstRefresh";
   Context context;


   public FirstRefreshAsync(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
      this.mPresenter = presenter;
      this.context = context.getApplicationContext();
      mService = GoogleApiHelper.getService(credential);
   }

   @Override
   protected Void doInBackground(Void... params) {
      try {
         firstRefresh();
      } catch (Exception e) {
         mLastError = e;
         e.printStackTrace();
         cancel(true);
//            FirebaseCrash.report(e);
         return null;
      }
      return null;
   }

   @Override
   protected void onProgressUpdate(Void... values) {
      super.onProgressUpdate(values);
   }


   @Override
   protected void onPreExecute() {
      mPresenter.showProgressDialog();
      mPresenter.showProgress(true);
      mPresenter.lockScreenOrientation();
   }

   @Override
   protected void onPostExecute(Void aVoid) {
      mPresenter.saveBooleanShP(Co.IS_FIRST_INIT, false);
//      mPresenter.saveStringSharedPreference(Co.CURRENT_LIST_TITLE, serverLists.get(0).getTitle());
      if (!mPresenter.isViewFinishing()) {
         mPresenter.dismissProgressDialog();
         mPresenter.showProgress(false);
         mPresenter.setUpViews();
         mPresenter.initRecyclerView(mPresenter.getTasksFromList(serverLists.get(0).getId()));
         mPresenter.updateCurrentView();
      } else {
         Log.d(TAG, "onPostExecute: View was finishing. UI related action not executed");
      }
      mPresenter.unlockScreenOrientation();
      context = null;
      mPresenter = null;


   }

   @Override
   protected void onCancelled(Void aVoid) {
      if (!mPresenter.isViewFinishing()) {
         mPresenter.dismissProgressDialog();
         mPresenter.showProgress(false);
      }
      if (mLastError != null) {
         if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
            mPresenter.showToast("Google Play Services is not available");
         } else if (mLastError instanceof UserRecoverableAuthIOException) {
//                mPresenter.showToast("The API has no authorization");
            mPresenter.requestApiPermission(mLastError);
         } else {
            mPresenter.showToast("The following error occurred:\n"
                  + mLastError.getMessage());
            mLastError.printStackTrace();
         }
      } else {
         mPresenter.showToast("Request cancelled.");
      }
      mPresenter.unlockScreenOrientation();
      context = null;
      mPresenter = null;
   }

   private void firstRefresh() throws IOException {
      if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {

         TaskLists result = mService.tasklists().list()
               .execute();
         serverLists = result.getItems();
         List<LocalList> localLists = mPresenter.createListsDatabase(serverLists);
         List<Task> tasks;
         if (serverLists != null && !serverLists.isEmpty()) {
            for (int i = 0; i < serverLists.size(); i++) {
               tasks = mService.tasks().list(serverLists.get(i).getId()).execute().getItems();
               if (tasks != null && !tasks.isEmpty()) {
                  for (int j = 0; j < tasks.size(); j++) {
                     Task currentTask = tasks.get(j);
                     if (currentTask.getTitle().trim().equals("") &&
                           currentTask.getDue() == null &&
                           currentTask.getNotes() == null) {
                        mService.tasks().delete(serverLists.get(i).getId(), currentTask.getId()).execute();
                        continue;
                     }
                     LocalTask task = new LocalTask(currentTask, Co.listIds.get(i));
                     task.setSyncStatus(Co.SYNCED);
                     task.setLocalModify(currentTask.getUpdated().getValue());
                     task.setListIntId(localLists.get(i).getIntId());
                     localTasks.add(task);
                  }
               }
            }
         }
         //FIXME Lists not being shown in the right order on different devices
         mPresenter.updateTasksFirstTime(localTasks);
         localTasks = mPresenter.getAllTasks();
         for (LocalTask task : localTasks) {
            if (task.getReminder() != 0)
               AlarmHelper.setOrUpdateAlarmFirstTime(task, context);
         }
      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }
}
