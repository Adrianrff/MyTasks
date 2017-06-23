package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.GoogleApiHelper;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class MoveTask extends AsyncTask<Void, Void, Void> {

   private final LinkedHashMap<LocalTask, String> moveMap;
   private com.google.api.services.tasks.Tasks mService = null;
   private Exception mLastError = null;
   private TaskListPresenter mPresenter;
   Context context;
   private JsonBatchCallback<Task> callBack;

   public MoveTask(Context context, TaskListPresenter presenter,
                   GoogleAccountCredential credential, LinkedHashMap<LocalTask, String> moveMap) {
      this.context = context.getApplicationContext();
      this.mPresenter = presenter;
      this.moveMap = moveMap;
      mService = GoogleApiHelper.getService(credential);
      callBack = new JsonBatchCallback<Task>() {
         @Override
         public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {

         }

         @Override
         public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {
         }
      };
   }

   @Override
   protected Void doInBackground(Void... params) {

      try {
         moveTask(moveMap);
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
      mPresenter.showProgress(false);
      mPresenter.unlockScreenOrientation();
      context = null;
      mPresenter = null;
   }

   @Override
   protected void onCancelled(Void aVoid) {
      mPresenter.dismissProgressDialog();
      mPresenter.showProgress(false);
      if (mLastError != null) {
         if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
            mPresenter.showToast(mPresenter.getString(R.string.g_services_not_available));
         } else if (mLastError instanceof UserRecoverableAuthIOException) {
            mPresenter.showToast("The API has no authorization");
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

   private void moveTask(LinkedHashMap<LocalTask, String> moveMap) throws IOException {
      if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
         Iterator it = moveMap.entrySet().iterator();
         BatchRequest request = mService.batch();
         String prevTaskId;
         String listId = null;
         LocalTask movedTask;
         while (it.hasNext()) {
            prevTaskId = null;
            movedTask = null;
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getKey() instanceof LocalTask) {
               movedTask = (LocalTask) pair.getKey();
            }
            if (pair.getValue() instanceof String) {
               prevTaskId = (String) pair.getValue();
            }
            if (movedTask != null) {
               listId = movedTask.getList();
               if (movedTask.getId() == null) {
                  Task task = mService.tasks().insert(movedTask.getList(),
                        LocalTask.localTaskToApiTask(movedTask)).execute();
                  mPresenter.updateNewlyCreatedTask(task, movedTask.getList(),
                        String.valueOf(movedTask.getIntId()));
                  movedTask.setId(task.getId());
               }
               Tasks.TasksOperations.Move move = mService.tasks().move(movedTask.getList(),
                     movedTask.getId());
               if (prevTaskId != null) {
                  move.setPrevious(prevTaskId);
               }
               move.queue(request, callBack);

            }
            it.remove();
         }
         if (request.size() > 0) {
            request.execute();
         }
         List<Task> tasks;
         if (listId != null) {
            tasks = mService.tasks().list(listId).execute().getItems();
            mPresenter.updatePositions(tasks);
         }
      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }
}
