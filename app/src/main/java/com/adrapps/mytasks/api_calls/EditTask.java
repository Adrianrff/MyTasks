package com.adrapps.mytasks.api_calls;

import android.os.AsyncTask;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.DateHelper;
import com.adrapps.mytasks.presenter.TaskListPresenter;
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

public class EditTask extends AsyncTask<LocalTask, Void, Void> {

    private String listId;
    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;

    public EditTask(TaskListPresenter presenter, GoogleAccountCredential credential, String listId) {
        this.listId = listId;
        this.mPresenter = presenter;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("My Tasks")
                .build();
    }

    @Override
    protected Void doInBackground(LocalTask... params) {
        try {
            editTask(params[0]);
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


    private void editTask(LocalTask lTask) throws IOException {
        Task task = mService.tasks().get(listId,lTask.getId()).execute();
        task.setTitle(lTask.getTitle());
        task.setNotes(lTask.getNotes());
        if (lTask.getDue() != 0) {
            task.setDue(DateHelper.millisecondsToDateTime(lTask.getDue()));
        } else {
            task.setDue(null);
        }
        mService.tasks().update(listId, task.getId(), task).execute();
        mPresenter.updateSyncStatus(lTask.getIntId(), Co.SYNCED);
    }
}
