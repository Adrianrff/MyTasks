package com.adrapps.mytasks.api_calls;

        import android.Manifest;
        import android.content.Context;
        import android.os.AsyncTask;

        import com.adrapps.mytasks.R;
        import com.adrapps.mytasks.domain.Co;
        import com.adrapps.mytasks.domain.LocalList;
        import com.adrapps.mytasks.domain.LocalTask;
        import com.adrapps.mytasks.presenter.TaskListPresenter;
        import com.google.api.client.extensions.android.http.AndroidHttp;
        import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
        import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
        import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
        import com.google.api.client.http.HttpTransport;
        import com.google.api.client.json.JsonFactory;
        import com.google.api.client.json.jackson2.JacksonFactory;
        import com.google.api.services.tasks.model.TaskList;

        import java.io.IOException;

        import pub.devrel.easypermissions.EasyPermissions;

public class CreateList extends AsyncTask<LocalList, Void, Void> {

    private com.google.api.services.tasks.Tasks mService = null;
    private Exception mLastError = null;
    private TaskListPresenter mPresenter;
    private String listId;
    private LocalTask localTask;
    Context context;

    public CreateList(Context context, TaskListPresenter presenter, GoogleAccountCredential credential) {
        this.context = context;
        this.mPresenter = presenter;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("My Tasks")
                .build();
        localTask = null;
    }

    @Override
    protected Void doInBackground(LocalList... params) {

        try {
            createList(params[0]);
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


    private void createList(LocalList localList) throws IOException {

        if (EasyPermissions.hasPermissions(context, Manifest.permission.GET_ACCOUNTS)) {
            TaskList list = new TaskList();
            list.setTitle(localList.getTitle());
            list = mService.tasklists().insert(list).execute();
            localList.setId(list.getId());
            localList.setLocalUpdated(System.currentTimeMillis());
            localList.setServerUpdated(list.getUpdated().getValue());
            mPresenter.updateList(localList);
        } else {
            EasyPermissions.requestPermissions(
                    context, context.getString(R.string.contacts_permissions_rationale),
                    Co.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
}
