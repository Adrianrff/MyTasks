package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

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
   private JsonBatchCallback<Void> deleteCallback;
   private JsonBatchCallback<Task> getServerTaskForUpdateCallback;
   private ArrayList<LocalTask> tasksToDelete;
   private HashMap<Task, LocalTask> tasksToUpdateFirstTime;
   private HashMap<String, LocalTask> localTasksMap;
   private HashMap<String, Task> serverTasksMap;

   Context context;
   private JsonBatchCallback<Task> moveCallback;

   public SyncTasks(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
      this.context = context.getApplicationContext();
      this.mPresenter = presenter;
      mService = GoogleApiHelper.getService(credential);
      this.requests = mService.batch();
      this.tasksToDelete = new ArrayList<>();
      this.tasksToUpdateFirstTime = new HashMap<>();

      this.getServerTaskForUpdateCallback = new JsonBatchCallback<Task>() {
         @Override
         public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
         }

         @Override
         public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {
            LocalTask currentLocalTask = localTasksMap.get(task.getId());
            if (currentLocalTask != null) {
               task.setTitle(currentLocalTask.getTitle());
               task.setNotes(currentLocalTask.getNotes() == null ? null : currentLocalTask.getNotes());
               task.setDue(currentLocalTask.getDue() == 0 ? null :
                     DateHelper.millisecondsToDateTime(currentLocalTask.getDue()));
               if (currentLocalTask.getStatus().equals(Co.TASK_COMPLETED)) {
                  task.setStatus(Co.TASK_COMPLETED);
               } else {
                  task.setStatus(Co.TASK_NEEDS_ACTION);
                  task.setCompleted(null);
               }
               mService.tasks().update(currentLocalTask.getListId(),
                     currentLocalTask.getId(),
                     task).execute();
            }
         }

      };
      this.deleteCallback = new JsonBatchCallback<Void>() {
         @Override
         public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
            Log.d("Deleted", "fail");

         }

         @Override
         public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
            Log.d("deleted", "success");
         }
      };

      this.moveCallback = new JsonBatchCallback<Task>() {
         @Override
         public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {

         }

         @Override
         public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {
            mPresenter.updatePosition(task);
         }
      };
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
      mPresenter.showProgress(false);
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
//         List<String> serverListsIds = new ArrayList<>();
         List<LocalList> localLists = mPresenter.getLocalLists();
         HashMap<String, LocalList> localListsMap = ObjectHelper.getLocalListIdMap(localLists);
         List<String> handledListIds = new ArrayList<>();
//         for (int i = 0; i < serverLists.size(); i++) {
//            serverListsIds.add(serverLists.get(i).getId());
//         }

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
            if (!localListsMap.containsKey(serverListId.trim())) {
               //List is not in database. Create it
               mPresenter.addNewListToDBFromServer(serverList);
            } else {
               //List exists in database
               final LocalList localList = localListsMap.get(serverListId);
               if (localList != null) {
                  //List was last modified locally
                  if (localList.getLocalModify() > serverList.getUpdated().getValue()) {
                     //List is marked deleted. Delete it
                     if (localList.getLocalDeleted() == Co.LOCAL_DELETED){
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
                     if (localList.getLocalDeleted() == Co.LOCAL_DELETED){
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
            if (list.getId() != null && handledListIds.contains(list.getId())){
               continue;
            }
            final int listIntId = list.getIntId();
            if (list.getId() == null){
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
               if (!handledListIds.contains(list.getId())){
                  mPresenter.deleteListFromDB(listIntId);
                  mPresenter.deleteTasksFromList(listIntId);
               }
            }
         }

         if (requests.size() > 0){
            requests.execute();
         }

         localLists = mPresenter.getLocalLists();

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

         List<Task> serverTasks;
         List<LocalTask> localTasks;
         localTasks = mPresenter.getAllTasks();
         List<String> handledTaskIds = new ArrayList<>();
         SparseArray<String> localListIntIdToIdMap = ObjectHelper.getLocalListIntIdToIdMap(localLists);
         SparseArray<String> localTaskIntIdToIdMap = ObjectHelper.getLocalTaskIntIdToIdMap(localTasks);
         HashMap<String, LocalTask> localTaskMap = ObjectHelper.getLocalTaskIdMap(localTasks);
         localListsMap = ObjectHelper.getLocalListIdMap(localLists);

         serverLists = mService.tasklists().list().execute().getItems();

         //Check if lists are synced
         if (ObjectHelper.areListsSynced(localLists, serverLists)){

            //Iterate through server tasks
            for (int i = 0; i < serverLists.size(); i++) {
               final String listId = serverLists.get(i).getId().trim();
               serverTasks = mService.tasks().list(listId).execute().getItems();
               for (int j = 0; j < serverTasks.size(); j++) {
                  final Task serverTask = serverTasks.get(j);
                  final String taskId = serverTask.getId().trim();
                  if (!localTaskMap.containsKey(taskId.trim())) {
                     //Task is not in database. Create it
                     mPresenter.addTaskFirstTimeFromServer(serverTask, listId, localListsMap.get(listId).getIntId());
                     handledTaskIds.add(taskId);
                  } else {
                     final LocalTask localTask = localTaskMap.get(taskId);
                     if (localTask != null){
                        //Task was last modified locally
                        if (localTask.getLocalModify() > serverTask.getUpdated().getValue()){
                           //Task is marked deleted. Delete it
                           if (localTask.getLocalDeleted() == Co.LOCAL_DELETED){
                              mPresenter.deleteTaskFromDatabase(localTask.getIntId());
                              mService.tasks().delete(taskId,listId).queue(requests, taskDeleteCallBack);
                              handledTaskIds.add(taskId);
                           } else {
                              //task is not marked deleted. Update in server
                              Task taskToUpdate = mService.tasks().get(taskId, listId).execute();
                              mService.tasks().update(listId, taskId, taskToUpdate).queue(requests, new JsonBatchCallback<Task>() {
                                 @Override
                                 public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
                                    Log.d(TAG, "onFailure: failed to update task: " + e.getMessage());
                                 }

                                 @Override
                                 public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {
                                    Log.d(TAG, "onSuccess: Task " + taskId + " updated successfully");
                                    localTask.setLocalModify(serverTask.getUpdated().getValue());
                                    mPresenter.updateExistingTaskFromLocalTask(localTask, listId);
                                 }
                              });
                              handledTaskIds.add(taskId);
                           }
                        } else if (serverTask.getUpdated().getValue() > localTask.getLocalModify()){
                           //Task is marked deleted. Delete it
                           if (localTask.getLocalDeleted() == Co.LOCAL_DELETED){
                              mPresenter.deleteTaskFromDatabase(localTask.getIntId());
                              mService.tasks().delete(taskId,listId).queue(requests, taskDeleteCallBack);
                              handledTaskIds.add(taskId);
                              //Task is not marked deleted, update it in DB
                           } else {
                              mPresenter.updateLocalTask(serverTask, listId);
                              handledTaskIds.add(taskId);
                           }
                        } else {
                           //They're the same
                           handledTaskIds.add(taskId);
                        }
                     }
                  }

               }
               localTasks = mPresenter.getAllTasks();
               //Iterate though local tasks
               for (LocalTask task : localTasks) {
                  if (task.getId() != null && handledTaskIds.contains(task.getId())){
                     continue;
                  }
                  final int taskIntId = task.getIntId();
                  if (task.getId() == null){
                     if (task.getLocalDeleted() != Co.LOCAL_DELETED){
                        //task was added locally but not synced (and not marked deleted). Add it to the server and update it in db
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
                     //Task is not in server. Delete it
                     if (!handledListIds.contains(task.getId())){
                        mPresenter.deleteTaskFromDatabase(taskIntId);
                     }
                  }
               }
            }

            if (requests.size() > 0){
               requests.execute();
            }


         } else {
            Log.d(TAG, "syncAll: LISTS ARE NOT SYNCED!!!");
         }



//         //GET LOCAL LISTS
////         List<LocalList> localLists = mPresenter.getLocalLists();
//         List<String> initialLocalListsIds = new ArrayList<>();
//         for (int i = 0; i < localLists.size(); i++) {
//            initialLocalListsIds.add(localLists.get(i).getId());
//         }
//
//         HashMap<String, TaskList> serverListMap = ObjectHelper.getServerListIdMap(serverLists);
//
//         List<LocalList> deletedLists = new ArrayList<>();
//         for (int i = 0; i < localLists.size(); i++) {
//            LocalList currentLocalList = localLists.get(i);
//            if (localLists.get(i).getLocalDeleted() == Co.LOCAL_DELETED) {
//               if (currentLocalList.getId() != null) {
//                  try {
//                     if (serverListsIds.contains(currentLocalList.getId())) {
//                        //This deletes the tasks in the list, too
//                        mService.tasklists().delete(currentLocalList.getId()).execute();
//                     }
//                  } catch (Exception e) {
//                     e.printStackTrace();
//                  }
//                  mPresenter.deleteTasksFromList(currentLocalList.getIntId());
//                  mPresenter.deleteListFromDB(currentLocalList.getIntId());
//               }
//               deletedLists.add(currentLocalList);
//               serverLists.remove(serverListMap.get(localLists.get(i).getId()));
//            } else if (currentLocalList.getId() == null && currentLocalList.getSyncStatus() == Co.NOT_SYNCED) {
//               final TaskList list = new TaskList();
//               list.setTitle(currentLocalList.getTitle());
//               try {
//                  list.setId(mService.tasklists().insert(list).execute().getId());
//                  List<LocalTask> newTasks = mPresenter.getTasksFromList(currentLocalList.getIntId());
//                  if (!newTasks.isEmpty()) {
//                     for (int j = 0; j < newTasks.size(); j++) {
//                        if (list.getId() != null) {
//                           final LocalTask currentLocalTask = newTasks.get(j);
//                           currentLocalTask.setListId(list.getId());
//                           Task task = LocalTask.localTaskToApiTask(newTasks.get(j));
//                           task = mService.tasks().insert(list.getId(), task).execute();
//                           mPresenter.updateNewlyCreatedTask(task, list.getId(), currentLocalTask.getIntId());
//                        }
//                     }
//                  }
//               } catch (Exception e) {
//                  e.printStackTrace();
//               }
//
//            }
//         }
//         if (!deletedLists.isEmpty()) {
//            localLists.removeAll(deletedLists);
//         }
////         HashMap<String, LocalList> localListsMap = ObjectHelper.getLocalListIdMap(localLists);
//
//
//         List<LocalList> localListsNotInServer = CompareLists.localListsNotInServer(localLists, serverListsIds);
//         List<TaskList> serverListNotInDb = CompareLists.serverListsNotInDB(initialLocalListsIds, serverLists);
//
//         if (!localListsNotInServer.isEmpty()) {
//            for (int i = 0; i < localListsNotInServer.size(); i++) {
//               LocalList currentList = localListsNotInServer.get(i);
//               if (currentList.getSyncStatus() == Co.SYNCED) {
//                  mPresenter.deleteListFromDB(currentList.getIntId());
//               } else {
//                  TaskList list = new TaskList();
//                  list.setTitle(currentList.getTitle());
//                  try {
//                     list = mService.tasklists().insert(list).execute();
//                     mPresenter.updateListInDBFromServerList(list, currentList.getIntId());
//                  } catch (IOException e) {
//                     e.printStackTrace();
//                  }
//               }
//               localLists.remove(currentList);
//            }
//         }
//
//         if (!serverListNotInDb.isEmpty()) {
//            for (int i = 0; i < serverListNotInDb.size(); i++) {
//               final TaskList currentServerList = serverListNotInDb.get(i);
//               mPresenter.addNewListToDBFromServer(currentServerList);
//               List<Task> addedTasks = mService.tasks().list(currentServerList.getId()).execute().getItems();
//               for (int j = 0; j < addedTasks.size(); j++) {
//                  mPresenter.addTaskFirstTimeFromServer(addedTasks.get(j), currentServerList.getId());
//               }
//               serverLists.remove(currentServerList);
//            }
//         }
//
//         for (int i = 0; i < localLists.size(); i++) {
//            LocalList currentLocalList = localLists.get(i);
//         }


         //TASKS
//         List<Task> serverTasks;
//         List<LocalTask> localTasks;
//         List<LocalTask> localTasksNotInServer;
//         List<Task> serverTasksNotInDB;
//         String currentListId;
//
//         // Loop through list of serverLists
//         for (int i = 0; i < serverLists.size(); i++) {
//            currentListId = serverLists.get(i).getId();
//
//            //Get server tasks and local tasks from list
//            serverTasks = mService.tasks().list(currentListId).execute().getItems();
//            localTasks = mPresenter.getTasksFromList(currentListId);
//            localTasksMap = new HashMap<>();
//            serverTasksMap = new HashMap<>();
//
//            if (serverTasks == null && localTasks.isEmpty()) {
//               continue;
//            }
//            //LOCAL TASKS HASH MAP
//            if (!localTasks.isEmpty()) {
//               for (int k = 0; k < localTasks.size(); k++) {
//                  LocalTask currentTask = localTasks.get(k);
//                  if (currentTask.getId() == null || currentTask.getId().isEmpty()) {
//                     continue;
//                  }
//                  localTasksMap.put(currentTask.getId(), currentTask);
//               }
//            }
//
//            //SERVER TASK HASH MAP
//            if (serverTasks != null && !serverTasks.isEmpty()) {
//               for (int k = 0; k < serverTasks.size(); k++) {
//                  Task currentTask = serverTasks.get(k);
//                  if (currentTask.getTitle().trim().equals("") &&
//                        currentTask.getDue() == null &&
//                        currentTask.getNotes() == null) {
//                     mService.tasks().delete(currentListId, currentTask.getId()).queue(requests, serverListDeleteCallback);
//                     serverTasks.remove(currentTask);
//                  } else {
//                     serverTasksMap.put(currentTask.getId(), currentTask);
//                  }
//
//               }
//            }
//
//            //ADD NEW AND UNSYNCED TASKS TO SERVER
//            for (int j = 0; j < localTasks.size(); j++) {
//               LocalTask localTask = localTasks.get(j);
//               if (localTask.getSyncStatus() == 0 || localTask.getId() == null) {
//                  Task task = LocalTask.localTaskToApiTask(localTask);
//                  Task serverTask = mService.tasks().insert(currentListId, task).execute();
//                  tasksToUpdateFirstTime.put(serverTask, localTask);
//               }
//
//               //DELETE TASKS MARKED AS LOCALLY DELETED
//               if (localTask.getLocalDeleted() == Co.LOCAL_DELETED) {
//                  tasksToDelete.add(localTask);
//                  String taskId = localTask.getId();
//                  if (serverTasks != null) {
//                     if (taskId != null) {
//                        if (serverTasksMap.containsKey(taskId)) {
//                           mService.tasks().delete(currentListId, taskId).queue(requests, serverListDeleteCallback);
//                           //DELETE FROM LIST - TASK WILL BE DELETED WHEN EXECUTING REQUEST
//                           serverTasks.remove(serverTasksMap.get(taskId));
//                           serverTasksMap.remove(taskId);
//                           mPresenter.deleteTaskFromDatabase(localTask.getIntId());
//                        }
//                        if (localTasksMap.containsKey(taskId)) {
//                           localTasksMap.remove(taskId);
//                        }
//                     }
//                  }
//               }
//            }
//            //REMOVE DELETED TASKS FROM LIST (DONE HERE TO AVOID CONCURRENCY EXCEPTIONS
//            //BECAUSE OF MODIFYING THE LISTS WHILE ITERATING THROUGH IT)
//            if (!tasksToDelete.isEmpty()) {
//               localTasks.removeAll(tasksToDelete);
//            }
//            //REMOVE NEW UNSYNCED TASKS FROM LIST AND ADD IN BULK TO DB (DONE HERE TO AVOID
//            //CONCURRENCY EXCEPTIONS BECAUSE OF MODIFYING THE LISTS WHILE ITERATING THROUGH IT)
//            if (!tasksToUpdateFirstTime.isEmpty()) {
//               localTasks.removeAll(tasksToUpdateFirstTime.values());
//               mPresenter.updateNewTasksInBulk(tasksToUpdateFirstTime);
//            }
//
//            //AT THIS POINT ALL SERVER TASKS NOT IN DB SHOULD BE ADDED TO IT, BECAUSE THE DELETED TASKS
//            //SHOULD HAVE ALREADY BEEN REMOVED FROM THE SERVER TASKS LIST
//            serverTasksNotInDB = CompareLists.serverTasksNotInDB(localTasks, serverTasks);
//            if (serverTasksNotInDB != null && !serverTasksNotInDB.isEmpty()) {
//               for (int j = 0; j < serverTasksNotInDB.size(); j++) {
//                  Task serverTask = serverTasksNotInDB.get(j);
//                  mPresenter.addTaskFirstTimeFromServer(serverTask, currentListId);
//                  if (serverTasks != null) {
//                     serverTasks.remove(serverTask);
//                     serverTasksMap.remove(serverTask.getId());
//                  }
//
//               }
//            }
//
//            //AT THIS POINT LOCAL TASKS NOT IN SERVER SHOULD BE DELETED BECAUSE NEW TASKS HAVE
//            //ALREADY BEEN REMOVED FROM THE LOCAL TASKS LIST. THIS MEANS THAT THESE TASKS WHERE
//            //DELETED FROM THE SERVER
//            localTasksNotInServer = CompareLists.localTasksNotInServer(localTasks, serverTasks);
//            if (localTasksNotInServer != null && !localTasksNotInServer.isEmpty()) {
//               for (int j = 0; j < localTasksNotInServer.size(); j++) {
//                  LocalTask task = localTasksNotInServer.get(j);
//                  mPresenter.deleteTaskFromDatabase(task.getIntId());
//                  localTasks.remove(task);
//                  if (serverTasks != null && !serverTasks.isEmpty() && serverTasksMap.containsKey(task.getId())) {
//                     serverTasks.remove(serverTasksMap.get(task.getId()));
//                     serverTasksMap.remove(task.getId());
//                  }
//               }
//            }
//
//            //LOOP THROUGH LOCAL TASKS
//            for (int k = 0; k < localTasks.size(); k++) {
//               LocalTask currentLocalTask = localTasks.get(k);
//               Task sameServerTask = serverTasksMap.get(currentLocalTask.getId());
//               String taskId = currentLocalTask.getId();
//               boolean taskChanged = false;
//               if (currentLocalTask.getLocalModify() > sameServerTask.getUpdated().getValue()) {
//                  if (currentLocalTask.getSyncStatus() == Co.EDITED_NOT_SYNCED) {
//                     mService.tasks().get(currentListId, taskId).queue(requests, getServerTaskForUpdateCallback);
//                     taskChanged = true;
//                  }
//                  if (currentLocalTask.getMoved() == Co.MOVED) {
//                     Tasks.TasksOperations.Move moveOperation = mService.tasks().
//                           move(currentListId, taskId);
//                     int localSiblingIntId = currentLocalTask.getPreviousTask();
//                     if (localSiblingIntId != 0) {
//                        String siblingServerId = mPresenter.getTaskIdByIntId(localSiblingIntId);
//                        moveOperation.setPrevious(siblingServerId).queue(requests, moveCallback);
//                     } else {
//                        moveOperation.queue(requests, moveCallback);
//                     }
//                     taskChanged = true;
//                  } else {
//                     mPresenter.updatePosition(sameServerTask);
//                  }
//                  if (taskChanged) {
//                     mPresenter.updateSyncStatus(currentLocalTask.getIntId(), Co.SYNCED);
//                     localTasksMap.put(taskId, currentLocalTask);
//                  }
//               } else {
//                  if (currentLocalTask.getLocalModify() < sameServerTask.getUpdated().getValue()) {
//                     mPresenter.updateLocalTask(sameServerTask, currentListId);
//                  }
//               }
//
//            }
//            if (requests.size() != 0) {
//               requests.execute();
//            }
//         }
      } else {
         EasyPermissions.requestPermissions(
               context, context.getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }

}