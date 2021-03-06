package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.GoogleApiHelper;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.Tasks;

import java.io.IOException;

import pub.devrel.easypermissions.EasyPermissions;

public class DeleteList extends AsyncTask<String, Void, Void> {

   private Tasks mService = null;
   private Exception mLastError = null;
   private TaskListPresenter mPresenter;
   private String listId;
   private LocalTask localTask;
   private static final String TAG = "DeleteList";

   Context context;

   public DeleteList(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
      this.context = context.getApplicationContext();
      this.mPresenter = presenter;
      mService = GoogleApiHelper.getService(credential);
      localTask = null;
   }

   @Override
   protected Void doInBackground(String... params) {

      try {
         deleteList(params[0]);
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


   private void deleteList(String listId) throws IOException {
      if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
//         List<LocalTask> tasks = mPresenter.getTasksFromList(listId);
//         mPresenter.deleteTasks(tasks);
         mService.tasklists().delete(listId).execute();
         int intId= mPresenter.getListIntIdById(listId);
         mPresenter.deleteListFromDB(intId);
         mPresenter.deleteTasksFromList(intId);
      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }
}
