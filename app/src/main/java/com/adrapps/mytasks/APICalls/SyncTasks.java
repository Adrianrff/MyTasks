package com.adrapps.mytasks.APICalls;

import android.os.AsyncTask;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Helpers.CompareLists;
import com.adrapps.mytasks.Helpers.DateHelper;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
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
import com.google.firebase.crash.FirebaseCrash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class SyncTasks extends AsyncTask<Void, Void, Void> {

    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;
    private List<String> listIds = new ArrayList<>();
    private List<LocalTask> localTasks = new ArrayList<>();
    private Task task;

    public SyncTasks(TaskListPresenter presenter, GoogleAccountCredential credential) {
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
            mLastError = e;
            cancel(true);
            FirebaseCrash.report(e);
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
        long offSet = TimeZone.getDefault().getRawOffset();

        //Get task lists
        List<TaskList> lists = mService.tasklists().list()
                .execute().getItems();

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
                    if (currentLocalTaskNotInServer.getSyncStatus() == 0) {
                        LocalTask.localTaskToApiTask(currentLocalTaskNotInServer);
                        Task task = mService.tasks().insert
                                (currentListId, LocalTask.localTaskToApiTask
                                        (currentLocalTaskNotInServer)).execute();
                        LocalTask lTask = mPresenter.updateNewlyCreatedTask(task, currentListId,
                                String.valueOf(currentLocalTaskNotInServer.getIntId()));
                    } else {
                        mPresenter.deleteTask(currentLocalTaskNotInServer.getId());
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
                if (currentLocalTask.getLocalDeleted() == Co.LOCAL_DELETED){
                    mService.tasks().delete(currentListId, currentLocalTask.getId()).execute();
                    continue;
                }
                if (currentLocalTask.getLocalModify() > sameServerTask.getUpdated().getValue()) {
                    if (currentLocalTask.getSyncStatus() == 1) {
                        Task task = mService.tasks().get(currentListId, currentLocalTask.getId()).execute();
                        task.setTitle(currentLocalTask.getTitle());
                        task.setNotes(currentLocalTask.getNotes() == null ? null : currentLocalTask.getNotes());
                        task.setDue(currentLocalTask.getDue()==0 ? null :
                        DateHelper.millisecondsToDateTime(currentLocalTask.getDue()));
                        if (currentLocalTask.getStatus().equals(Co.TASK_COMPLETED)) {
                            task.setCompleted(DateHelper.millisecondsToDateTime(currentLocalTask.getLocalModify()));
                            task.setStatus(Co.TASK_COMPLETED);
                        } else {
                            task.setCompleted(null);
                        }
                        mService.tasks().update(currentListId,
                                currentLocalTask.getId(),
                                task).execute();
                    }
                    if (currentLocalTask.getMoved() == Co.MOVED) {
                        Tasks.TasksOperations.Move moveOperation = mService.tasks().
                                move(currentListId, sameServerTask.getId());
                        int localSiblingIntId = currentLocalTask.getSibling();
                        if (localSiblingIntId != 0){
                            String siblingServerId = mPresenter.getTaskIdByIntId(localSiblingIntId);
                            sameServerTask = moveOperation.setPrevious(siblingServerId).execute();
                        } else {
                            sameServerTask = moveOperation.execute();
                        }
                        mPresenter.updatePosition(sameServerTask);
                    }
                    mPresenter.updateSyncStatus(currentLocalTask.getIntId(), Co.SYNCED);
                } else {
                    mPresenter.updateLocalTask(sameServerTask, currentListId);
                }
            }


            //---------------------------------------------------------

//            commonTasks.addAll(CompareLists.commonTasks(localTasks, serverTasks));
//
//            for (int k = 0; k < commonTasks.size(); k++) {
//                LocalTask task = commonTasks.get(k);
//                Task sTask = mService.tasks().get(currentListId, task.getId()).execute();
//                if (sTask.getTitle().trim().equals("") && sTask.getDue() == null && sTask.getNotes() == null) {
//                    mService.tasks().delete(currentListId, sTask.getId()).execute();
//                    continue;
//                }
//                if (task.getLocalDeleted() == Co.LOCAL_DELETED) {
//                    mService.tasks().delete(currentListId, task.getId()).execute();
//                } else {
//                    if (sTask.getUpdated().getValue() < task.getLocalModify()) {
//                        if (task.getSyncStatus() == 1) {
//                            if (task.getMoved() == 1) {
//                                if (task.getParent() != null) {
//                                    //move in server into parent
//                                } else {
//                                    Tasks.TasksOperations.Move move = mService.tasks().
//                                            move(currentListId, task.getId());
//                                    if (task.getSibling() != 0) {
//                                        move.setPrevious(mPresenter.getTaskIdByIntId(task.getSibling())).execute();
//                                    } else {
//                                        move.execute();
//                                    }
//                                    mPresenter.updateMoved(task.getId(), Co.NOT_MOVED);
//
//                                }
//                            }
//
//                            sTask.setTitle(task.getTitle());
//                            if (task.getDue() != 0) {
//                                sTask.setDue(DateHelper.millisecondsToDateTime(task.getDue()));
//                            }
//                            sTask.setNotes(task.getNotes());
//                            mService.tasks().update(currentListId, task.getId(), sTask).execute();
//                            mPresenter.updateSyncStatus(task.getId(), Co.SYNCED);
//                        }
//                    } else {
//                        mPresenter.updateLocalTask(sTask, currentListId);
//                        Tasks.TasksOperations.Move move = mService.tasks().
//                                move(currentListId, task.getId());
//                        if (task.getSibling() != Co.MOVED_TO_FIRST) {
//                            String localSibling = mPresenter.getTaskIdByIntId(task.getSibling());
//                            if (localSibling != null) {
//                                move.setPrevious(localSibling).execute();
//                            }
//                        } else {
//                            move.execute();
//                        }
//                    }
//                }
//            }

//            if (serverTasks != null && !serverTasks.isEmpty()) {
//                for (int j = 0; j < serverTasks.size(); j++) {
//                    currentServerTask = serverTasks.get(j);
//                    currentLocalTask = mPresenter.getTaskByTaskId(currentServerTask.getId());
//
//                    //if task exists in database
//                    if (currentLocalTask != null) {
//
//
//                        //If local task is marked deleted, delete task from server
//                        if (currentLocalTask.getLocalDeleted() == Co.LOCAL_DELETED) {
//                            mService.tasks().delete(currentListId, currentServerTask.getId()).execute();
//                            mPresenter.deleteTask(currentLocalTask.getId());
//                        } else {
//
//                            //Last updated in server
//                            long serverTaskUpdated = currentServerTask.getUpdated().getValue();
//                            if (currentServerTask.getUpdated().getValue() >
//                                    currentLocalTask.getLocalModify()) {
//                                modifiedLocalTask = new LocalTask(currentServerTask, currentListId);
//                                modifiedLocalTask.setReminderNoID(currentLocalTask.getReminder());
//                                modifiedLocalTask.setReminderId(currentLocalTask.getReminderId());
//                                modifiedLocalTask.setSyncStatus(Co.SYNCED);
//                                mPresenter.updateLocalTask(modifiedLocalTask);
//
//                                //Last updated locally
//                            } else {
//                                if (currentLocalTask.getMoved() == Co.MOVED) {
//                                    //Task was moved
//
//                                    //Was moved inside another task
//                                    if (currentLocalTask.getParent() != null) {
//                                        currentServerTask = mService.tasks().move(currentListId, currentServerTask.getId())
//                                                .setParent(currentLocalTask.getParent()).execute();
//                                        //Was moved below a sibling
//                                    } else {
//                                        Tasks.TasksOperations.Move move = mService.tasks().
//                                                move(currentListId, currentServerTask.getId());
//
//                                        //Sibling not null. Move below it
//                                        if (currentLocalTask.getSibling() != null) {
//                                            currentServerTask = move.setPrevious(currentLocalTask.getSibling()).execute();
//
//                                            //Sibling null. Move to first
//                                        } else {
//                                            currentServerTask = move.execute();
//                                        }
//                                    }
//                                    mPresenter.updatePosition(currentServerTask);
//                                    mPresenter.updateMoved(currentLocalTask.getId(), Co.NOT_MOVED);
//                                    mPresenter.updateSibling(currentLocalTask.getId(), null);
//                                }
//
//                                if (currentLocalTask.getSyncStatus() != 2) {
//                                    //Task wasn't moved
//                                    currentServerTask.setTitle(currentLocalTask.getTitle());
//                                    if (currentLocalTask.getDue() != 0) {
//                                        currentServerTask.setDue(DateHelper
//                                                .millisecondsToDateTime(currentLocalTask.getDue()));
//                                    } else {
//                                        currentServerTask.setDue(null);
//                                    }
//                                    currentServerTask.setNotes(currentLocalTask.getNotes());
//                                    mService.tasks().update(currentListId, currentServerTask.getId(),
//                                            currentServerTask).execute();
//                                    mPresenter.updateSyncStatus(currentLocalTask.getId(), Co.SYNCED);
//                                }
//
//
//                            }
//                        }
//
//                        //Task doesn't exist in database. Create it
//                    } else {
//                        if (currentServerTask.getTitle().equals("") &&
//                                currentServerTask.getDue() == null &&
//                                currentServerTask.getNotes() == null) {
//                            try {
//                                mService.tasks().delete(currentListId,
//                                        currentServerTask.getId()).execute();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            continue;
//                        }
//                        LocalTask localTask = new LocalTask(currentServerTask, currentListId);
//                        localTask.setSyncStatus(Co.SYNCED);
//                        localTask.setLocalModify();
//                        mPresenter.addTaskToDatabase(localTask);
//                    }
//                }
//                List<LocalTask> tasksNotInServer = CompareLists.localTasksNotInServer(localTasks, serverTasks);
//                LocalTask lTask = null;
//                if (tasksNotInServer != null && !tasksNotInServer.isEmpty()) {
//                    for (int k = 0; k < tasksNotInServer.size(); k++) {
//                        lTask = tasksNotInServer.get(k);
//                        if (lTask.getSyncStatus() != 0) {
//                            mPresenter.deleteTask(lTask.getId());
//                        } else {
//                            Task task = LocalTask.localTaskToApiTask(tasksNotInServer.get(k));
//                            Task aTask = mService.tasks().insert(lTask.getTaskList(), task).execute();
//                            mPresenter.updateNewlyCreatedTask(aTask, lTask.getTaskList(),
//                                    String.valueOf(lTask.getIntId()));
//                        }
//                    }
//                }
//
//            }
        }
    }
}
