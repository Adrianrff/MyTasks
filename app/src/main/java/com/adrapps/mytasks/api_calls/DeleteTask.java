package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class DeleteTask extends AsyncTask<Void, Void, Void> {

   private final JsonBatchCallback<Void> deleteCallback;
   private com.google.api.services.tasks.Tasks mService = null;
   private Exception mLastError = null;
   private TaskListPresenter mPresenter;
   List<LocalTask> tasks;
   Context context;

   public DeleteTask(Context context, TaskListPresenter presenter,
                     GoogleAccountCredential credential, List<LocalTask> tasks) {
      this.context = context;
      this.mPresenter = presenter;
      this.tasks = tasks;
      HttpTransport transport = AndroidHttp.newCompatibleTransport();
      JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
      mService = new com.google.api.services.tasks.Tasks.Builder(
            transport, jsonFactory, credential)
            .setApplicationName("My Tasks")
            .build();

      this.deleteCallback = new JsonBatchCallback<Void>() {
         @Override
         public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
//            Log.d("Deleted", "fail");

         }
         @Override
         public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
//            Log.d("deleted", "success");
         }
      };
   }

   @Override
   protected Void doInBackground(Void... params) {

      try {
         removeTask(tasks);
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
   }

   @Override
   protected void onPostExecute(Void aVoid) {
      mPresenter.showProgress(false);
   }

   @Override
   protected void onCancelled(Void aVoid) {
      mPresenter.dismissProgressDialog();
      mPresenter.showProgress(false);
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
   }


   private void removeTask(List<LocalTask> tasks) throws IOException {
      if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
         BatchRequest requests = mService.batch();
         for (int i = 0; i < tasks.size(); i++) {
            LocalTask currentTask = tasks.get(i);
            if (currentTask.getId() != null) {
               mService.tasks().delete(currentTask.getList(), currentTask.getId()).queue(requests, deleteCallback);
               mPresenter.deleteTaskFromDatabase(mPresenter.getIntIdByTaskId(currentTask.getId()));
            }
         }
         if (requests.size() > 0){
            requests.execute();
         }
      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }
}
