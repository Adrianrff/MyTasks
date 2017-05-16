package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
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
import com.google.api.services.tasks.model.Task;

import java.io.IOException;

import pub.devrel.easypermissions.EasyPermissions;

public class BatchTest extends AsyncTask<Void, Void, Void> {

    private final JsonBatchCallback<Task> callback;
    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;
    Context context;

    public BatchTest(Context context, TaskListPresenter presenter,
                     GoogleAccountCredential credential) {
        this.mPresenter = presenter;
        this.context = context;
        this.callback = new JsonBatchCallback<Task>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {

            }

            @Override
            public void onSuccess(Task task, HttpHeaders responseHeaders) throws IOException {

            }
        };

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
            addTask();
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
        mPresenter.updateCurrentView();
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


    private void addTask() throws IOException {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
            BatchRequest request = mService.batch();
            Task task1 = new Task();
            task1.setTitle("test 1");
            mService.tasks().insert(mPresenter.getStringShP(Co.CURRENT_LIST_ID), task1).queue(request, callback);
            Task task2 = new Task();
            task2.setTitle("test 2");
            mService.tasks().insert(mPresenter.getStringShP(Co.CURRENT_LIST_ID), task2).queue(request, callback);
            Task task3 = new Task();
            task3.setTitle("test 3");
            mService.tasks().insert(mPresenter.getStringShP(Co.CURRENT_LIST_ID), task3).queue(request, callback);
            request.execute();

        } else {
            EasyPermissions.requestPermissions(
                    context, context.getString(R.string.contacts_permissions_rationale),
                    Co.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
}

