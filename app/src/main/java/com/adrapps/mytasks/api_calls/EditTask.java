package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.helpers.GoogleApiHelper;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;

import pub.devrel.easypermissions.EasyPermissions;

public class EditTask extends AsyncTask<LocalTask, Void, Void> {

   private String listId;
   private com.google.api.services.tasks.Tasks mService = null;
   private Exception mLastError = null;
   private TaskListPresenter mPresenter;
   private static final String TAG = "EditTask";
   Context context;

   public EditTask(Context context, TaskListPresenter presenter, GoogleAccountCredential credential, String listId) {
      this.context = context.getApplicationContext();
      this.listId = listId;
      this.mPresenter = presenter;
      mService = GoogleApiHelper.getService(credential);
   }

   @Override
   protected Void doInBackground(LocalTask... params) {
      try {
         editTask(params[0]);
      } catch (Exception e) {
         mLastError = e;
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
      mPresenter.showProgress(true);
      mPresenter.lockScreenOrientation();

   }

   @Override
   protected void onPostExecute(Void aVoid) {
      if (!mPresenter.isViewFinishing()) {
         mPresenter.showProgress(false);
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
         mPresenter.showProgress(false);
      }
      if (mLastError != null) {
         if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
            mPresenter.showToast(mPresenter.getString(R.string.g_services_not_available));
         } else if (mLastError instanceof UserRecoverableAuthIOException) {
//                mPresenter.showToast("The API has no authorization");
            mPresenter.requestApiPermission(mLastError);

         } else {
            mPresenter.showToast("The following error occurred:\n"
                  + mLastError.getMessage());
            mLastError.printStackTrace();
         }
      } else {
         mPresenter.showToast(mPresenter.getString(R.string.request_canceled));
      }
      mPresenter.unlockScreenOrientation();
      context = null;
      mPresenter = null;
   }


   private void editTask(LocalTask lTask) throws IOException {
      if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
         Task task = mService.tasks().get(listId, lTask.getId()).execute();
         task.setTitle(lTask.getTitle());
         task.setNotes(lTask.getNotes());
         task.setDue(DateHelper.millisecondsToDateTime(lTask.getDue()));
         Task editedServerTask = mService.tasks().update(listId, task.getId(), task).execute();
         if (editedServerTask != null) {
            lTask.setLocalModify(editedServerTask.getUpdated().getValue());
//            lTask.setSyncStatus(Co.SYNCED);
            mPresenter.updateExistingTaskFromLocalTask(lTask);
         }
      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }
}
