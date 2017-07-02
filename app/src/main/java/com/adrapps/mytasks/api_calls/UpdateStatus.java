package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.GoogleApiHelper;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;

import pub.devrel.easypermissions.EasyPermissions;

public class UpdateStatus extends AsyncTask<LocalTask, Void, Void> {

   private com.google.api.services.tasks.Tasks mService = null;
   private Exception mLastError = null;
   private TaskListPresenter mPresenter;
   Context context;

   public UpdateStatus(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
      this.context = context.getApplicationContext();
      this.mPresenter = presenter;
      mService = GoogleApiHelper.getService(credential);
   }

   @Override
   protected Void doInBackground(LocalTask... params) {

      try {
         changeStatus(params[0], params[0].getStatus());
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


   private void changeStatus(LocalTask localTask, String newStatus) throws IOException {
      if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
         Task task = mService.tasks().get(localTask.getListId(), localTask.getId()).execute();
         if (newStatus.equals(Co.TASK_NEEDS_ACTION)) {
            task.setCompleted(null);
         }
         task.setStatus(newStatus);
         Task updatedServerTask = mService.tasks().update(localTask.getListId(), task.getId(), task).execute();
         if (updatedServerTask != null) {
            localTask.setLocalModify(updatedServerTask.getUpdated().getValue());
            localTask.setSyncStatus(Co.SYNCED);
            mPresenter.updateExistingTaskFromLocalTask(localTask);
         }
      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }
}
