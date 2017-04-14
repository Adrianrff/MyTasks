package com.adrapps.mytasks.APICalls;

import android.os.AsyncTask;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RefreshAllAsync extends AsyncTask<Void, Void, Void> {

    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;
    private List<String> listIds = new ArrayList<>();
    private List<LocalTask> localTasks = new ArrayList<>();

    public RefreshAllAsync(TaskListPresenter presenter, GoogleAccountCredential credential) {
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
        List<String> strings = new ArrayList<>();
        mPresenter.showToast(strings.get(1));
        try {
            refreshAll();
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


    private void refreshAll() throws IOException {
        List<TaskList> lists;
        TaskLists result = mService.tasklists().list()
                .execute();
                lists = result.getItems();
        for (int i = 0; i< lists.size(); i++){
            listIds.add(lists.get(i).getId());
        }
        if (!lists.isEmpty()) {
            mPresenter.updateLists(lists);
        }
        List<Task> tasks;
        for (int i = 0; i < (!lists.isEmpty() ? lists.size() : 0); i++){
            tasks = mService.tasks().list(lists.get(i).getId()).execute().getItems();
            if (tasks != null && !tasks.isEmpty()) {
                for (int j = 0; j < tasks.size(); j++){
                    LocalTask task = new LocalTask(tasks.get(j),listIds.get(i));
                    localTasks.add(task);
                }
            }
        }
        mPresenter.updateTasks(localTasks);

    }
}
