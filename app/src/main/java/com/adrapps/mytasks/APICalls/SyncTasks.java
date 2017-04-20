package com.adrapps.mytasks.APICalls;

import android.os.AsyncTask;
import android.util.Log;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
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

import java.io.IOException;
import java.util.ArrayList;
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

        List<Task> serverTasks = new ArrayList<>();
        List<LocalTask> localTasks = mPresenter.getLocalTasksFromDB();
        Task currentServerTask;
        LocalTask currentLocalTask, modifiedLocalTask;
        String currentListId;

        // Loop through list of lists
        for (int i = 0; i < lists.size(); i++) {

            currentListId = lists.get(i).getId();
            //Get server tasks and local tasks from list
            serverTasks.addAll(mService.tasks().list(currentListId).execute().getItems());
//            localTasks = mPresenter.getTasksFromList(lists.get(i).getId());

            if (!serverTasks.isEmpty()) {
                for (int j = 0; j < serverTasks.size(); j++  ) {
                    currentServerTask = serverTasks.get(j);
                    currentLocalTask = mPresenter.getTask(currentServerTask.getId());

                    //if task exists in database
                    if (currentLocalTask != null) {


                        //If local task is marked deleted, delete task from server
                        if (currentLocalTask.getLocalDeleted() == Co.LOCAL_DELETED) {
                            Log.d("Task mark deleted","deleted");
                            mService.tasks().delete(currentListId, currentServerTask.getId()).execute();
                            mPresenter.deleteTask(currentLocalTask.getId());
                        } else {

                            //Last updated in server
                            if (currentServerTask.getUpdated().getValue() - offSet >
                                    currentLocalTask.getLocalModify()) {
                                modifiedLocalTask = new LocalTask(currentServerTask, currentListId);
                                modifiedLocalTask.setLocalModify();
                                modifiedLocalTask.setReminderNoID(currentLocalTask.getReminder());
                                modifiedLocalTask.setReminderId(currentLocalTask.getReminderId());
                                modifiedLocalTask.setSyncStatus(Co.SYNCED);
                                mPresenter.updateLocalTask(modifiedLocalTask);

                                //Last updated locally
                            } else {

                                //Task wasn't moved
                                if (currentLocalTask.getMoved() != Co.MOVED) {
                                    currentServerTask.setTitle(currentLocalTask.getTitle());
                                    if (currentLocalTask.getDue() != 0) {
                                        currentServerTask.setDue(DateHelper
                                                .millisecondsToDateTime(currentLocalTask.getDue()));
                                    } else {
                                        currentServerTask.setDue(null);
                                    }
                                    currentServerTask.setNotes(currentLocalTask.getNotes());
                                    mService.tasks().update(currentListId, currentServerTask.getId(),
                                            currentServerTask).execute();

                                    //Task was moved
                                } else {

                                    //Was moved inside another task
                                    if (currentLocalTask.getParent() != null) {
                                        mService.tasks().move(currentListId, currentServerTask.getId())
                                                .setParent(currentLocalTask.getParent()).execute();

                                        //Was moved below a sibling
                                    } else {
                                        Tasks.TasksOperations.Move move = mService.tasks().
                                                move(currentListId, currentServerTask.getId());

                                        //Sibling not null. Move below it
                                        if (currentLocalTask.getLocalSibling() != null) {
                                            move.setPrevious(currentLocalTask.getLocalSibling()).execute();

                                            //Sibling null. Move to first
                                        } else {
                                            move.execute();
                                        }

                                    }
                                }

                            }
                        }

                        //Task doesn't exist in database. Create it
                    } else {
                        LocalTask localTask = new LocalTask(currentServerTask, currentListId);
                        mPresenter.addTaskToDatabase(localTask);
                    }
                }


            }



        }

        //Check if a local task is not in the server tasks and
        // add it to the server if there're any
        for (int i = 0; i < localTasks.size(); i++){
            if (localTasks.get(i).getSyncStatus() == 0){
                LocalTask unsyncedTask = localTasks.get(i);
                Task task = LocalTask.localTaskToApiTask(unsyncedTask);
                Task aTask = mService.tasks().insert(unsyncedTask.getTaskList(),task).execute();
                mPresenter.updateSyncStatus(Co.SYNCED, unsyncedTask.getId());
                mPresenter.updateLocalTask(aTask, unsyncedTask.getTaskList());

            }
        }


    }
}
