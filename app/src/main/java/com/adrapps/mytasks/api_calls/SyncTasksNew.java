package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.CompareLists;
import com.adrapps.mytasks.helpers.DateHelper;
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
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class SyncTasksNew extends AsyncTask<Void, Void, Void> {

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

    public SyncTasksNew(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
        this.context = context;
        this.mPresenter = presenter;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new Tasks.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("My Tasks")
                .build();
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
//                        task.setCompleted(DateHelper.millisecondsToDateTime(currentLocalTask.getLocalModify()));
                        task.setStatus(Co.TASK_COMPLETED);
                    } else {
//                        task.setStatus(Co.TASK_NEEDS_ACTION);
                        task.setCompleted(null);
                    }
                    mService.tasks().update(currentLocalTask.getList(),
                            currentLocalTask.getId(),
                            task).execute();
                }
            }

        };
        this.deleteCallback = new JsonBatchCallback<Void>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {

            }

            @Override
            public void onSuccess(Void aVoid, HttpHeaders responseHeaders) throws IOException {
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
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mPresenter.showProgress(false);
        mPresenter.showSwipeRefreshProgress(false);
        mPresenter.updateCurrentView();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        mPresenter.dismissProgressDialog();
        mPresenter.showSwipeRefreshProgress(false);
        mPresenter.showProgress(false);
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
    }


    private void syncAll() throws IOException {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {

            List<LocalList> localListsNotInServer = new ArrayList<>();
            List<TaskList> serverListNotInDb = new ArrayList<>();

            //Get server lists
            List<TaskList> lists = mService.tasklists().list()
                    .execute().getItems();

//            localListsNotInServer = CompareLists.localListsNotInServer(mPresenter.getLocalLists(), lists);
//            serverListNotInDb = CompareLists.serverListsNotInDB(mPresenter.getLocalLists(), lists);

            //Populate listIds field
            //Update lists database
            if (!lists.isEmpty()) {
                mPresenter.updateLists(lists);
            }

            List<Task> serverTasks;
            List<LocalTask> localTasks;
            List<LocalTask> localTasksNotInServer = new ArrayList<>();
            List<Task> serverTasksNotInDB;
            String currentListId;

            // Loop through list of lists
            for (int i = 0; i < lists.size(); i++) {
                currentListId = lists.get(i).getId();

                //Get server tasks and local tasks from list
                serverTasks = mService.tasks().list(currentListId).execute().getItems();
                localTasks = mPresenter.getTasksFromList(currentListId);
                localTasksMap = new HashMap<>();
                serverTasksMap = new HashMap<>();

                if (serverTasks == null && localTasks.isEmpty()) {
                    continue;
                }

                //LOCAL TASKS HASH MAP
                if (!localTasks.isEmpty()) {
                    for (int k = 0; k < localTasks.size(); k++) {
                        LocalTask currentTask = localTasks.get(k);
                        if (currentTask.getId() == null || currentTask.getId().isEmpty()){
                            continue;
                        }
                        localTasksMap.put(currentTask.getId(), currentTask);
                    }
                }

                //SERVER TASK HASH MAP
                if (serverTasks != null && !serverTasks.isEmpty()) {
                    for (int k = 0; k < serverTasks.size(); k++) {
                        Task currentTask = serverTasks.get(k);
                        serverTasksMap.put(currentTask.getId(), currentTask);
                    }
                }

                for (LocalTask localTask : localTasks) {

                    //ADD NEW AND UNSYNCED TASKS TO SERVER
                    if (localTask.getSyncStatus() == 0 || localTask.getId() == null) {
                        Task task = LocalTask.localTaskToApiTask(localTask);
//                        mService.tasks().insert(currentListId, task).queue(requests, insertCallback);
                        Task serverTask = mService.tasks().insert(currentListId, task).execute();
//                        LocalTask lTask = mPresenter.updateNewlyCreatedTask(serverTask,
//                                currentListId, String.valueOf(localTask.getIntId()));
                        tasksToUpdateFirstTime.put(serverTask, localTask);
                    }


                    //DELETE TASKS MARKED AS LOCALLY DELETED
                    if (localTask.getLocalDeleted() == Co.LOCAL_DELETED) {
                        tasksToDelete.add(localTask);
                        String taskId = localTask.getId();
                        if (serverTasks != null) {
                            if (taskId != null) {
                                if (serverTasksMap.containsKey(taskId)) {
                                    mService.tasks().delete(taskId, currentListId).queue(requests, deleteCallback);
                                    //DELETE FROM LIST - TASK WILL BE DELETED WHEN EXECUTING REQUEST
                                    serverTasks.remove(serverTasksMap.get(taskId));
                                    serverTasksMap.remove(taskId);
                                    mPresenter.deleteTaskFromDatabase(localTask.getIntId());
                                } if (localTasksMap.containsKey(taskId)){
                                    localTasksMap.remove(taskId);
                                }
                            }
                        }
                    }
                }

                //REMOVE DELETED TASKS FROM LIST
                if (!tasksToDelete.isEmpty()) {
                    localTasks.removeAll(tasksToDelete);
                }

                //REMOVE NEW UNSYNCED TASKS FROM LIST AND ADD IN BULK TO DB
                if (!tasksToUpdateFirstTime.isEmpty()) {
                    localTasks.removeAll(tasksToUpdateFirstTime.values());
                    mPresenter.updateNewTasksInBulk(tasksToUpdateFirstTime);
                }

                //AT THIS POINT ALL SERVER TASKS NOT IN DB SHOULD BE ADDED TO IT, BECAUSE THE DELETED TASKS
                //SHOULD HAVE ALREADY BEEN REMOVED FROM THE SERVER TASKS LIST
                serverTasksNotInDB = CompareLists.serverTasksNotInDB(localTasks, serverTasks);
                if (serverTasksNotInDB != null && !serverTasksNotInDB.isEmpty()) {
                    for (Task serverTask : serverTasksNotInDB) {
                        if (serverTask.getTitle().trim().equals("") &&
                                serverTask.getDue() == null &&
                                serverTask.getNotes() == null) {
                            mService.tasks().delete(currentListId, serverTask.getId()).queue(requests, deleteCallback);
                        }
                        mPresenter.addTaskFirstTimeFromServer(serverTask, currentListId);
                    }
                }

                //AT THIS POINT LOCAL TASKS NOT IN SERVER SHOULD BE DELETED BECAUSE NEW TASKS HAVE
                //ALREADY BEEN REMOVED FROM THE LOCAL TASKS LIST. THIS MEANS THAT THESE TASKS WHERE
                //DELETED FROM THE SERVER
                localTasksNotInServer = CompareLists.localTasksNotInServer(localTasks, serverTasks);
                if (localTasksNotInServer != null && !localTasksNotInServer.isEmpty()) {
                    for (LocalTask task : localTasksNotInServer) {
                        mPresenter.deleteTaskFromDatabase(task.getIntId());
                        localTasks.remove(task);
                        if (serverTasks != null && serverTasksMap.containsKey(task.getId())) {
                            serverTasks.remove(serverTasksMap.get(task.getId()));
                        }
                    }
                }

////                DELETE LOCAL TASKS NOT IN SERVER WITH SYNC STATUS != 0
//                if (localTasksNotInServer != null && !localTasksNotInServer.isEmpty()) {
//                    for (LocalTask localTask : localTasksNotInServer) {
//
//                        if (localTask.getSyncStatus() == Co.NOT_SYNCED &&
//                                localTask.getLocalDeleted() != Co.LOCAL_DELETED) {
//                            Task task = LocalTask.localTaskToApiTask(localTask);
//                            mService.tasks().insert(currentListId, task).queue(requests, insertCallback);
//                            tasksToUpdateFirstTime.put(task, localTask);
//                            LocalTask lTask = mPresenter.updateNewlyCreatedTask(task, currentListId,
//                                    String.valueOf(localTask.getIntId()));
//                        } else {
//                            mPresenter.deleteTaskFromDatabase(localTask.getIntId());
//                        }
//                    }
//                }

                //SERVER TASKS NOT IN DB (DELETE EMPTY ONES ADD THE OTHERS TO DB)
//                if (!serverTasksNotInDB.isEmpty()) {
//                    for (int k = 0; k < serverTasksNotInDB.size(); k++) {
//                        Task currentServerTaskNotInDB = serverTasksNotInDB.get(k);
//                        if (currentServerTaskNotInDB.getTitle().trim().equals("") &&
//                                currentServerTaskNotInDB.getDue() == null &&
//                                currentServerTaskNotInDB.getNotes() == null) {
//                            mService.tasks().delete(currentListId, currentServerTaskNotInDB.getId()).queue(requests, deleteCallback);
//                            continue;
//                        }
//                        mPresenter.addTaskFirstTimeFromServer(currentServerTaskNotInDB, currentListId);
//                    }
//                }
//
//                //UPDATED TASKS (POSITIONS NOT UPDATED)
//                localTasks = mPresenter.getTasksFromList(currentListId);
//                serverTasks = mService.tasks().list(currentListId).execute().getItems();


                //LOOP THROUGH LOCAL TASKS
                for (int k = 0; k < localTasks.size(); k++) {
                    LocalTask currentLocalTask = localTasks.get(k);
                    Task sameServerTask = serverTasksMap.get(currentLocalTask.getId());
                    String taskId = currentLocalTask.getId();
                    boolean taskChanged = false;
                    if (currentLocalTask.getLocalModify() > sameServerTask.getUpdated().getValue()) {
                        if (currentLocalTask.getSyncStatus() == Co.EDITED_NOT_SYNCED) {
                            mService.tasks().get(currentListId, taskId).queue(requests, getServerTaskForUpdateCallback);
                            taskChanged = true;
//                            Task task = mService.tasks().get(currentListId, currentLocalTask.getId()).queue(requests, getServerTaskForUpdateCallback);
//                            task.setTitle(currentLocalTask.getTitle());
//                            task.setNotes(currentLocalTask.getNotes() == null ? null : currentLocalTask.getNotes());
//                            task.setDue(currentLocalTask.getDue() == 0 ? null :
//                                    DateHelper.millisecondsToDateTime(currentLocalTask.getDue()));
//                            if (currentLocalTask.getStatus().equals(Co.TASK_COMPLETED)) {
//                                task.setCompleted(DateHelper.millisecondsToDateTime(currentLocalTask.getLocalModify()));
//                                task.setStatus(Co.TASK_COMPLETED);
//                            } else {
//                                task.setStatus(Co.TASK_NEEDS_ACTION);
//                                task.setCompleted(null);
//                            }
//                            sameServerTask = mService.tasks().update(currentListId,
//                                    currentLocalTask.getId(),
//                                    task).execute();
                        }
                        if (currentLocalTask.getMoved() == Co.MOVED) {
                            Tasks.TasksOperations.Move moveOperation = mService.tasks().
                                    move(currentListId, taskId);
                            int localSiblingIntId = currentLocalTask.getSibling();
                            if (localSiblingIntId != 0) {
                                String siblingServerId = mPresenter.getTaskIdByIntId(localSiblingIntId);
                                moveOperation.setPrevious(siblingServerId).queue(requests, moveCallback);
                            } else {
                                moveOperation.queue(requests, moveCallback);
                            }
                            taskChanged = true;
                        }
                        if (taskChanged) {
                            mPresenter.updateSyncStatus(currentLocalTask.getIntId(), Co.SYNCED);
                            localTasksMap.put(taskId, currentLocalTask);
                        }
                    } else {
                        if (currentLocalTask.getServerModify() < sameServerTask.getUpdated().getValue()) {
                            mPresenter.updateLocalTask(sameServerTask, currentListId);
                        }
                    }
                }
                if (requests.size() != 0) {
                    requests.execute();
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