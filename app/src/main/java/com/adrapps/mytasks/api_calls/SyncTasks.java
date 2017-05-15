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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
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

public class SyncTasks extends AsyncTask<Void, Void, Void> {

    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;
    private List<String> listIds = new ArrayList<>();
    private List<LocalTask> localTasks = new ArrayList<>();
    private Task task;
    Context context;

    public SyncTasks(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
        this.context = context;
        this.mPresenter = presenter;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("My Tasks")
                .build();
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

            localListsNotInServer = CompareLists.localListsNotInServer(mPresenter.getLocalLists(), lists);
            serverListNotInDb = CompareLists.serverListsNotInDB(mPresenter.getLocalLists(), lists);

            //Populate listIds field
            for (int i = 0; i < lists.size(); i++) {
                listIds.add(lists.get(i).getId());
            }

            //Update lists database
            if (!lists.isEmpty()) {
                mPresenter.updateLists(lists);
            }

            List<Task> serverTasks;
            List<LocalTask> localTasks;
            List<LocalTask> localTasksNotInServer;
            List<Task> serverTasksNotInDB;
            String currentListId;

            // Loop through list of lists
            for (int i = 0; i < lists.size(); i++) {
                currentListId = lists.get(i).getId();

                //Get server tasks and local tasks from list
                serverTasks = mService.tasks().list(currentListId).execute().getItems();
                localTasks = mPresenter.getTasksFromList(currentListId);
                HashMap<String, LocalTask> localTasksMap = new HashMap<>();
                HashMap<String, Task> serverTasksMap = new HashMap<>();

                if (serverTasks == null && localTasks.isEmpty()) {
                    continue;
                }
                localTasksNotInServer = CompareLists.localTasksNotInServer(localTasks, serverTasks);
                serverTasksNotInDB = CompareLists.serverTasksNotInDB(localTasks, serverTasks);


                //LOCAL TASKS NOT IN SERVER
                if (localTasksNotInServer != null && !localTasksNotInServer.isEmpty()) {
                    for (int k = 0; k < localTasksNotInServer.size(); k++) {
                        LocalTask currentLocalTaskNotInServer = localTasksNotInServer.get(k);
                        if (currentLocalTaskNotInServer.getSyncStatus() != 2) {
                            LocalTask.localTaskToApiTask(currentLocalTaskNotInServer);
                            Task task = mService.tasks().insert
                                    (currentListId, LocalTask.localTaskToApiTask
                                            (currentLocalTaskNotInServer)).execute();
                            LocalTask lTask = mPresenter.updateNewlyCreatedTask(task, currentListId,
                                    String.valueOf(currentLocalTaskNotInServer.getIntId()));
                        } else {
                            mPresenter.deleteTaskFromDatabase(currentLocalTaskNotInServer.getIntId());
                        }
                    }
                }

                //SERVER TASKS NOT IN DB (DELETE EMPTY ONES)
                if (serverTasksNotInDB != null && !serverTasksNotInDB.isEmpty()) {
                    for (int k = 0; k < serverTasksNotInDB.size(); k++) {
                        Task currentServerTaskNotInDB = serverTasksNotInDB.get(k);
                        if (currentServerTaskNotInDB.getTitle().trim().equals("") &&
                                currentServerTaskNotInDB.getDue() == null &&
                                currentServerTaskNotInDB.getNotes() == null) {
                            mService.tasks().delete(currentListId, currentServerTaskNotInDB.getId());
                            continue;
                        }
                        mPresenter.addTaskFirstTimeFromServer(currentServerTaskNotInDB, currentListId);
                    }
                }

                //UPDATED TASKS (POSITIONS NOT UPDATED)
                localTasks = mPresenter.getTasksFromList(currentListId);
                serverTasks = mService.tasks().list(currentListId).execute().getItems();

                //LOCAL TASKS HASH MAP
                if (!localTasks.isEmpty()) {
                    for (int k = 0; k < localTasks.size(); k++) {
                        LocalTask currentTask = localTasks.get(k);
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

                //LOOP THROUGH LOCAL TASKS
                for (int k = 0; k < localTasks.size(); k++) {
                    LocalTask currentLocalTask = localTasks.get(k);
                    Task sameServerTask = serverTasksMap.get(currentLocalTask.getId());
                    if (currentLocalTask.getLocalDeleted() == Co.LOCAL_DELETED) {
                        mService.tasks().delete(currentListId, currentLocalTask.getId()).execute();
                        continue;
                    }
                    if (currentLocalTask.getLocalModify() > sameServerTask.getUpdated().getValue()) {
                        if (currentLocalTask.getSyncStatus() == 1) {
                            Task task = mService.tasks().get(currentListId, currentLocalTask.getId()).execute();
                            task.setTitle(currentLocalTask.getTitle());
                            task.setNotes(currentLocalTask.getNotes() == null ? null : currentLocalTask.getNotes());
                            task.setDue(currentLocalTask.getDue() == 0 ? null :
                                    DateHelper.millisecondsToDateTime(currentLocalTask.getDue()));
                            if (currentLocalTask.getStatus().equals(Co.TASK_COMPLETED)) {
                                task.setCompleted(DateHelper.millisecondsToDateTime(currentLocalTask.getLocalModify()));
                                task.setStatus(Co.TASK_COMPLETED);
                            } else {
                                task.setStatus(Co.TASK_NEEDS_ACTION);
                                task.setCompleted(null);
                            }
                            sameServerTask = mService.tasks().update(currentListId,
                                    currentLocalTask.getId(),
                                    task).execute();
                        }
                        if (currentLocalTask.getMoved() == Co.MOVED) {
                            Tasks.TasksOperations.Move moveOperation = mService.tasks().
                                    move(currentListId, sameServerTask.getId());
                            int localSiblingIntId = currentLocalTask.getSibling();
                            if (localSiblingIntId != 0) {
                                String siblingServerId = mPresenter.getTaskIdByIntId(localSiblingIntId);
                                sameServerTask = moveOperation.setPrevious(siblingServerId).execute();
                            } else {
                                sameServerTask = moveOperation.execute();
                            }
                            mPresenter.updatePosition(sameServerTask);
                        }
                        mPresenter.updateSyncStatus(currentLocalTask.getIntId(), Co.SYNCED);
                        mPresenter.updateLocalTask(sameServerTask, currentListId);
                    } else {
                        mPresenter.updateLocalTask(sameServerTask, currentListId);
                    }
                }

            }
        } else {
            EasyPermissions.requestPermissions(
                    context,context.getString(R.string.contacts_permissions_rationale),
                    Co.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

}