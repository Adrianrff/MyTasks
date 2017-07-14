package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.helpers.GoogleApiHelper;
import com.adrapps.mytasks.helpers.ObjectHelper;
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
import com.google.api.services.tasks.model.TaskList;
import com.google.firebase.crash.FirebaseCrash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class SyncTasks extends AsyncTask<Void, Void, Void> {

   private final String TAG = "SyncTasks";
   private final BatchRequest requests;
   private Tasks mService = null;
   private Exception mLastError = null;
   private TaskListPresenter mPresenter;

   Context context;

   public SyncTasks(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
      this.context = context.getApplicationContext();
      this.mPresenter = presenter;
      mService = GoogleApiHelper.getService(credential);
      this.requests = mService.batch();
   }

   @Override
   protected Void doInBackground(Void... params) {
      try {
         syncAll();
      } catch (Exception e) {
         e.printStackTrace();
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
         mPresenter.showSwipeRefreshProgress(false);
         mPresenter.updateCurrentView();
      } else {
         Log.d(TAG, "onPostExecute (SyncTasks): View was finishing. UI related action not executed");
      }
      mPresenter.unlockScreenOrientation();
      context = null;
      mPresenter = null;
   }

   @Override
   protected void onCancelled(Void aVoid) {
      if (!mPresenter.isViewFinishing()) {
         mPresenter.showProgress(false);
         mPresenter.showSwipeRefreshProgress(false);
      }
      if (mLastError != null) {
         if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
            mPresenter.showToast("Google Play Services is not available");
         } else if (mLastError instanceof UserRecoverableAuthIOException) {
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


   private void syncAll() throws IOException {
      if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {

         //GET SERVER LISTS
         List<TaskList> serverLists = mService.tasklists().list().execute().getItems();
         List<LocalList> localLists = mPresenter.getLocalLists();
         HashMap<String, LocalList> localListsMap = ObjectHelper.getLocalListIdMap(localLists);
         List<String> handledListIds = new ArrayList<>();
         JsonBatchCallback<Void> serverListDeleteCallback = new JsonBatchCallback<Void>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
               Log.d(TAG, "onFailure: Failed to delete list: " + e.getMessage());
            }

            @Override
            public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
               Log.d(TAG, "onSuccess: list successfully deleted");
            }
         };
         /*------------------ITERATE THROUGH SERVER LISTS------------------*/
         for (int i = 0; i < serverLists.size(); i++) {
            final TaskList serverList = serverLists.get(i);
            String serverListId = serverLists.get(i).getId();
            long updateServer = serverLists.get(i).getUpdated().getValue();
            int u = ((int) updateServer);
            if (!localListsMap.containsKey(serverListId.trim())) {
               //List is not in database. Create it
               mPresenter.addNewListToDBFromServer(serverList);
               handledListIds.add(serverListId);
            } else {
               //List exists in database
               final LocalList localList = localListsMap.get(serverListId);
               if (localList != null) {
                  //List was last modified locally
                  if (localList.getLocalModify() > serverList.getUpdated().getValue()) {
                     //List is marked deleted. Delete it
                     if (localList.getLocalDeleted() == Co.LOCAL_DELETED) {
                        mPresenter.deleteTasksFromList(localList.getIntId());
                        mPresenter.deleteListFromDB(localList.getIntId());
                        mService.tasklists().delete(serverListId).queue(requests, serverListDeleteCallback);
                        handledListIds.add(serverListId);
                     } else {
                        //List is not marked deleted. Update it in server
                        TaskList serverListToModify = mService.tasklists().get(serverListId).execute();
                        serverListToModify.setTitle(localList.getTitle());
                        mService.tasklists().update(serverListId, serverListToModify).
                              queue(requests, new JsonBatchCallback<TaskList>() {
                                 @Override
                                 public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                                    Log.d(TAG, "onFailure: failed to update list: " + e.getMessage());
                                 }

                                 @Override
                                 public void onSuccess(TaskList taskList, HttpHeaders responseHeaders) throws IOException {
                                    Log.d(TAG, "onSuccess: List " + taskList.getId() + " successfully modified in server");
                                    localList.setLocalModify(serverList.getUpdated().getValue());
                                    mPresenter.updateListInDBFromLocalList(localList);
                                 }
                              });
                        handledListIds.add(serverListId);
                     }

                     //List was last modified in server
                  } else if (serverList.getUpdated().getValue() > localList.getLocalModify()) {

                     //List is marked deleted. Delete it
                     if (localList.getLocalDeleted() == Co.LOCAL_DELETED) {
                        mPresenter.deleteTasksFromList(localList.getIntId());
                        mPresenter.deleteListFromDB(localList.getIntId());
                        mService.tasklists().delete(serverListId).queue(requests, serverListDeleteCallback);
                        handledListIds.add(serverListId);

                        //List is not marked deleted, update it in DB
                     } else {
                        mPresenter.updateListInDBFromServerList(serverList, localList.getIntId());
                        handledListIds.add(serverListId);
                     }
                  } else {
                     //They're the same
                     handledListIds.add(serverListId);
                  }
               }
            }
         }

         /*------------------ITERATE THROUGH LOCAL LISTS------------------*/
         localLists = mPresenter.getLocalLists();

         //ITERATE THROUGH LOCAL LISTS
         for (LocalList list : localLists) {
            if (list.getId() != null && handledListIds.contains(list.getId())) {
               continue;
            }
            final int listIntId = list.getIntId();
            if (list.getId() == null) {
               if (list.getLocalDeleted() != Co.LOCAL_DELETED) {
                  //list was added locally but not synced (and not marked deleted). Add it to the server and update it in db
                  TaskList serverListToAdd = new TaskList();
                  serverListToAdd.setTitle(list.getTitle());
                  mService.tasklists().insert(serverListToAdd).queue(requests, new JsonBatchCallback<TaskList>() {
                     @Override
                     public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                        Log.d(TAG, "onFailure: failed to insert list");
                     }

                     @Override
                     public void onSuccess(TaskList taskList, HttpHeaders responseHeaders) throws IOException {
                        Log.d(TAG, "onSuccess: List " + taskList.getId() + " successfully added to server");
                        mPresenter.updateListInDBFromServerList(taskList, listIntId);
                     }
                  });
               } else {
                  //list is marked deleted (and it is not in server)
                  mPresenter.deleteListFromDB(listIntId);
                  mPresenter.deleteTasksFromList(listIntId);
               }
            } else {
               //List is not in server. Delete it
               if (!handledListIds.contains(list.getId())) {
                  mPresenter.deleteListFromDB(listIntId);
                  mPresenter.deleteTasksFromList(listIntId);
               }
            }
         }

         if (requests.size() > 0) {
            requests.execute();
         }


         /*------------------ITERATE THROUGH SERVER TASKS LIST BY LIST------------------*/
         JsonBatchCallback<Void> taskDeleteCallBack = new JsonBatchCallback<Void>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
               Log.d(TAG, "onFailure: Failed to delete task");
            }

            @Override
            public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
               Log.d(TAG, "onSuccess: Task successfully deleted");
            }
         };

         //Update server and local lists
         localLists = mPresenter.getLocalLists();
         serverLists = mService.tasklists().list().execute().getItems();

         //Check if lists are synced
         if (ObjectHelper.areListsSynced(localLists, serverLists)) {
            List<String> handledTaskIds = new ArrayList<>();
            localListsMap = ObjectHelper.getLocalListIdMap(localLists);

            //Get server and local tasks from list
            for (int i = 0; i < serverLists.size(); i++) {
               final String listId = serverLists.get(i).getId().trim();
               int listIntId = localListsMap.get(listId).getIntId();
               List<Task> serverTasksFromList = mService.tasks().list(listId).execute().getItems();
               List<LocalTask> localTasksFromList = mPresenter.getTasksFromList(listIntId);
               if (serverTasksFromList != null && !serverTasksFromList.isEmpty()) {

                  //Iterate through server tasks
                  for (int j = 0; j < serverTasksFromList.size(); j++) {
                     final Task serverTask = serverTasksFromList.get(j);
                     final String taskId = serverTask.getId().trim();
                     HashMap<String, LocalTask> localTaskMap = ObjectHelper.getLocalTaskIdMap(localTasksFromList);
                     HashMap<String, Task> serverTaskMap = ObjectHelper.getServerTaskIdMap(serverTasksFromList);

                     /*--------------TASKS WITHOUT GOOGLE ID----------------*/
                     if (!localTaskMap.containsKey(taskId.trim())) {
                        //Task is not in database. Create it
                        mPresenter.addTaskFirstTimeFromServer(serverTask, listId, localListsMap.get(listId).getIntId());
                        handledTaskIds.add(taskId);


                      /*--------------TASKS WITH GOOGLE ID----------------*/

                     } else {
                        final LocalTask localTask = localTaskMap.get(taskId);
                        if (localTask != null) {
                           //Task is marked deleted regardless of where it was last modified,
                           // delete it and continue with next task
                           if (localTask.getLocalDeleted() == Co.LOCAL_DELETED) {
                              mPresenter.deleteTaskFromDatabase(localTask.getIntId());
                              mService.tasks().delete(listId, taskId).queue(requests, taskDeleteCallBack);
                              handledTaskIds.add(taskId);
                              continue;
                           }

                           //Task is not marked deleted and was last modified locally
                           //TODO: handle moved tasks
                           if (localTask.getLocalModify() > serverTask.getUpdated().getValue()) {
                              Task taskToUpdate = mService.tasks().get(listId, taskId).execute();
                              taskToUpdate.setTitle(localTask.getTitle());
                              taskToUpdate.setNotes(localTask.getNotes());
                              taskToUpdate.setDue(DateHelper.millisecondsToDateTime(localTask.getDue()));
                              if (localTask.getStatus().equals(Co.TASK_COMPLETED)){
                                 taskToUpdate.setStatus(Co.TASK_COMPLETED);
                                 taskToUpdate.setCompleted(DateHelper.millisecondsToDateTime(localTask.getCompleted()));
                              } else {
                                 taskToUpdate.setStatus(Co.TASK_NEEDS_ACTION);
                                 taskToUpdate.setCompleted(null);
                              }
                              if (localTask.getParent() != null){
                                 mService.tasks().move(listId, taskId).setParent(localTask.getParent()).queue(requests, new JsonBatchCallback<Task>() {
                                    @Override
                                    public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                                       Log.d(TAG, "onFailure: Fail to make subtask");
                                    }

                                    @Override
                                    public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {
                                       Log.d(TAG, "onSuccess: Task " + task.getTitle() +
                                             " successfully made subtask of " + task.getParent());

                                    }
                                 });
                                 taskToUpdate.setParent(localTask.getParent());
                              }
                              mService.tasks().update(listId, taskId, taskToUpdate).queue(requests, new JsonBatchCallback<Task>() {
                                 @Override
                                 public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                                    Log.d(TAG, "onFailure: failed to update task: " + e.getMessage());
                                 }

                                 @Override
                                 public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {
                                    Log.d(TAG, "onSuccess: Task " + taskId + " updated successfully");
                                    localTask.setLocalModify(serverTask.getUpdated().getValue());
                                    mPresenter.updateExistingTaskFromLocalTask(localTask);
                                 }
                              });
                              handledTaskIds.add(taskId);

                              //Task was last modified in server. Update it in database
                           } else if (serverTask.getUpdated().getValue() > localTask.getLocalModify()) {
                              mPresenter.updateLocalTask(serverTask, listId);
                              handledTaskIds.add(taskId);
                           } else {
                              //They're the same
                              handledTaskIds.add(taskId);
                           }
                        }
                     }
                  }
               }

               //Update tasks from list
               localTasksFromList = mPresenter.getTasksFromList(listIntId);

               //Iterate though local tasks
               for (LocalTask task : localTasksFromList) {
                  if (task.getId() != null && handledTaskIds.contains(task.getId())) {
                     continue;
                  }
                  final int taskIntId = task.getIntId();

                  //Task is not yet synced in server (id is null)
                  if (task.getId() == null) {
                     //task is not marked deleted. Add it to the server and update it in db
                     if (task.getLocalDeleted() != Co.LOCAL_DELETED) {
                        Task serverTaskToAdd = LocalTask.localTaskToApiTask(task);
                        mService.tasks().insert(listId, serverTaskToAdd).queue(requests, new JsonBatchCallback<Task>() {
                           @Override
                           public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                              Log.d(TAG, "onFailure: failed to add task to server");
                           }

                           @Override
                           public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {
                              Log.d(TAG, "onSuccess: task " + task.getId() + " added successfully");
                              mPresenter.updateNewlyCreatedTask(task, listId, taskIntId);
                           }
                        });
                     } else {
                        //list is marked deleted (and it is not in server)
                        mPresenter.deleteTaskFromDatabase(taskIntId);
                     }
                  } else {
                     //Task is not in server so it was probably delete in server. Delete it in db
                     if (!handledListIds.contains(task.getId())) {
                        mPresenter.deleteTaskFromDatabase(taskIntId);
                     }
                  }
               }
            }

            if (requests.size() > 0) {
               requests.execute();
            }

         } else {
            try {
               throw new Exception("Lists not synced");
            } catch (Exception e) {
               FirebaseCrash.report(e);
               e.printStackTrace();
            }
         }

      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }

}