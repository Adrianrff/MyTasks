package com.adrapps.mytasks.APICalls;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.firebase.crash.FirebaseCrash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirstRefreshAsync extends AsyncTask<Void, Void, Void> {

    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;
    private List<LocalTask> localTasks = new ArrayList<>();
    private List<TaskList> lists = new ArrayList<>();


    public FirstRefreshAsync(TaskListPresenter presenter, GoogleAccountCredential credential) {
        this.mPresenter = presenter;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Tasks API Android Quickstart")
                .build();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            firstRefresh();
        } catch (Exception e) {
            mLastError = e;
            e.printStackTrace();
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
    protected void onPreExecute() {mPresenter.showProgressDialog();
        mPresenter.showProgress(true);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mPresenter.saveBooleanShP(Co.IS_FIRST_INIT,false);
        mPresenter.dismissProgressDialog();
        mPresenter.showProgress(false);
        mPresenter.saveStringSharedPreference(Co.CURRENT_LIST_TITLE,lists.get(0).getTitle());
        mPresenter.setUpViews();
        mPresenter.initRecyclerView(mPresenter.getTasksFromList(lists.get(0).getId()));
        mPresenter.updateCurrentView();


    }

    @Override
    protected void onCancelled(Void aVoid) {
        mPresenter.dismissProgressDialog();
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

    private void firstRefresh() throws IOException {

        TaskLists result = mService.tasklists().list()
                .execute();
                lists = result.getItems();
        if (!lists.isEmpty()) {
            mPresenter.updateLists(lists);
        }
        List<Task> tasks;

        if (lists != null) {
            if (!lists.isEmpty()) {
                for (int i = 0; i < lists.size(); i++){
                    tasks = mService.tasks().list(lists.get(i).getId()).execute().getItems();
                    if (tasks != null) {
                        if (!tasks.isEmpty()) {
                            for (int j = 0; j < tasks.size(); j++){
                                Task currentTask = tasks.get(j);
                                if (currentTask.getTitle().trim().equals("") &&
                                        currentTask.getDue() == null &&
                                        currentTask.getNotes() == null){
                                    mService.tasks().delete(lists.get(i).getId(), currentTask.getId()).execute();
                                }
                                LocalTask task = new LocalTask(tasks.get(j),Co.listIds.get(i));
                                task.setSyncStatus(Co.SYNCED);
                                task.setLocalModify();
                                localTasks.add(task);
                            }
                        }
                    }
                }
            }
        }
        mPresenter.updateTasks(localTasks);

    }
}
