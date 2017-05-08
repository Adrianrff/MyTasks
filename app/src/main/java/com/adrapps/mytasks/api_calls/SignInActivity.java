package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.views.MainActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pub.devrel.easypermissions.EasyPermissions;

public class SignInActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleAccountCredential mCredential;
    private com.google.api.services.tasks.Tasks mService = null;
    ProgressDialog mProgress;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sign_in_activity);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(Co.SCOPES))
                .setBackOff(new ExponentialBackOff());
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.tasks.Tasks.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName(getString(R.string.app_name))
                .build();
        SignInButton signInButton = (SignInButton) findViewById(R.id.signIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.request_api_authorization));
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, 1007);
        } else if (!isDeviceOnline()) {
            Toast.makeText(this, R.string.no_internet_toast, Toast.LENGTH_LONG).show();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account in order to authorize access to" +
                            "the Google Tasks API. Google does this through the" +
                            " contacts permissions. We will not store or use your contacts thuogh",
                    Co.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Co.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, getString(R.string.requires_Google_Services_message),
                            Toast.LENGTH_SHORT).show();
                }
                break;
//            case Co.REQUEST_ACCOUNT_PICKER:
//                if (resultCode == RESULT_OK && data != null &&
//                        data.getExtras() != null) {
//                    String accountName =
//                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//                    if (accountName != null) {
//                        SharedPreferences prefs =
//                                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                        SharedPreferences.Editor editor = prefs.edit();
//                        editor.putString(Co.USER_EMAIL, accountName);
//                        editor.apply();
//                        mCredential.setSelectedAccountName(accountName);
//                        signIn();
//                    }
//                }
//                break;
            case Co.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Co.IS_FIRST_LAUNCH, false);
                    editor.apply();
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
                break;

            case 1007:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    // Signed in successfully, show authenticated UI.
                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(Co.IS_FIRST_LAUNCH, false);
                    GoogleSignInAccount acct = result.getSignInAccount();
                    if (acct != null) {
                        Account account = acct.getAccount();
                        editor.putString(Co.USER_EMAIL, account != null ? account.name : null);
                        editor.putString(Co.USER_NAME, acct.getDisplayName());
                        mCredential.setSelectedAccount(acct.getAccount());
                        if (acct.getPhotoUrl()!= null) {
                            editor.putString(Co.USER_PIC_URL, acct.getPhotoUrl().toString());
                        }
                    }
                    editor.apply();
                    signIn();
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        FirstAPICall firstCall = new FirstAPICall(this, mCredential);
        firstCall.execute();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                SignInActivity.this,
                connectionStatusCode,
                Co.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private void goToTaskListActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(Co.IS_FIRST_INIT, true);
        startActivity(i);
        finish();
    }

    @Override
    protected void onPause() {
        mProgress.dismiss();
        super.onPause();
    }


    private class FirstAPICall extends AsyncTask<Void, Void, List<String>> {

        private com.google.api.services.tasks.Tasks mService = null;
        private Exception mLastError = null;
        Context context;

        private FirstAPICall(Context context, GoogleAccountCredential credential) {
            this.context = context;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.tasks.Tasks.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Tasks API Android Quickstart")
                    .build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {

            List<String> listInfo;
            try {
                listInfo = firstCall();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
//                FirebaseCrash.report(e);
                return null;
            }
            return listInfo;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> defaultListInfo) {
            mProgress.dismiss();
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Co.IS_FIRST_LAUNCH, false);
            editor.putString(Co.CURRENT_LIST_TITLE, defaultListInfo.get(1));
            editor.putString(Co.CURRENT_LIST_ID, defaultListInfo.get(0));
            editor.putBoolean(Co.IS_FIRST_INIT, true);
            editor.apply();
            goToTaskListActivity();
        }

        @Override
        protected void onCancelled(List<String> strings) {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Toast.makeText(context, R.string.Google_Services_not_available_toast, Toast.LENGTH_LONG).show();
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Co.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(context, getString(R.string.error_toast)
                            + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                    mLastError.printStackTrace();
                }
            } else {
                Toast.makeText(context, R.string.request_canceled, Toast.LENGTH_LONG).show();
            }
        }

        private List<String> firstCall() throws IOException {
            List<TaskList> lists;
            List<String> defaultListInfo = new ArrayList<>();
            TaskLists result = mService.tasklists().list()
                    .execute();
            lists = result.getItems();
            for (int i = 0; i < lists.size(); i++) {
                Co.listIds.clear();
                Co.listTitles.clear();
                Co.listIds.add(lists.get(i).getId());
                Co.listTitles.add(lists.get(i).getTitle());
            }
            defaultListInfo.add(lists.get(0).getId());
            defaultListInfo.add(lists.get(0).getTitle());
            return defaultListInfo;
        }
    }

}