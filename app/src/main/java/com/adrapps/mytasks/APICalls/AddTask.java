package com.adrapps.mytasks.APICalls;

import android.os.AsyncTask;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.model.Task;

import java.io.IOException;

/**
 * Created by Adrian Flores on 10/4/2017.
 */

public class AddTask extends AsyncTask<LocalTask, Void, Void> {

    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;
    private String listId;
    private LocalTask localTask;

    public AddTask(TaskListPresenter presenter, GoogleAccountCredential credential, String listId) {
        this.mPresenter = presenter;
        this.listId = listId;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("My Tasks")
                .build();
        localTask = null;
    }

    @Override
    protected Void doInBackground(LocalTask... params) {

        try {
            addTask(params[0]);
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


    private void addTask(LocalTask lTask) throws IOException {
        Task task = LocalTask.localTaskToApiTask(lTask);
        Task aTask = null;
        try {
            aTask = mService.tasks().insert(listId, task).execute();
            if (aTask != null) {
                localTask = new LocalTask(aTask, lTask.getTaskList());
                if (lTask.getReminder() != 0) localTask.setReminder(lTask.getReminder());
                localTask.setTaskList(lTask.getTaskList());
                localTask.setSyncStatus(Co.SYNCED);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mPresenter.showToast("Task could not be added to the server");
            mPresenter.addTaskToDatabase(lTask);
        }
    }
}
