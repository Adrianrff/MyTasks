package com.adrapps.mytasks.api_calls;

import android.Manifest;
import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.helpers.GoogleApiHelper;
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

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SignInActivity extends AppCompatActivity
      implements EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

   GoogleAccountCredential mCredential;
   private com.google.api.services.tasks.Tasks mService = null;
   ProgressDialog mProgress;
   private GoogleApiClient mGoogleApiClient;
   private GoogleSignInOptions gso;


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
      setContentView(R.layout.sign_in_activity);
      mCredential = GoogleAccountCredential.usingOAuth2(
            getApplicationContext(), Arrays.asList(Co.SCOPES))
            .setBackOff(new ExponentialBackOff());
      mService = GoogleApiHelper.getService(mCredential);
      SignInButton signInButton = (SignInButton) findViewById(R.id.signIn);
      signInButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            signIn();
         }
      });
      mProgress = new ProgressDialog(this);
      mProgress.setMessage(getString(R.string.request_api_authorization));
      mGoogleApiClient = GoogleApiHelper.getClient(getApplicationContext());
      mGoogleApiClient.connect();
   }

   private void signIn() {
//      Toast.makeText(this, "Sign in called", Toast.LENGTH_SHORT).show();
      if (!isGooglePlayServicesAvailable()) {
//         Toast.makeText(this, "Acquire Google Services (Sign in)", Toast.LENGTH_SHORT).show();
         acquireGooglePlayServices();
      } else if (mCredential.getSelectedAccountName() == null) {
//         Toast.makeText(this, "Account name null, start sign in intent", Toast.LENGTH_SHORT).show();
         Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
         startActivityForResult(signInIntent, 1007);
      } else if (!isDeviceOnline()) {
         Toast.makeText(this, R.string.no_internet_toast, Toast.LENGTH_LONG).show();
      } else {
         EasyPermissions.requestPermissions(
               this, getString(R.string.contacts_permissions_rationale),
               Co.REQUEST_PERMISSION_GET_ACCOUNTS,
               Manifest.permission.GET_ACCOUNTS);
      }
   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      Toast.makeText(this, R.string.google_connection_failed, Toast.LENGTH_LONG).show();
   }

   @Override
   protected void onActivityResult(
         int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (requestCode) {
         case Co.REQUEST_GOOGLE_PLAY_SERVICES:
//            Toast.makeText(this, "onActivityResult No Google services", Toast.LENGTH_SHORT).show();
            if (resultCode != RESULT_OK) {
               Toast.makeText(this, getString(R.string.requires_Google_Services_message),
                     Toast.LENGTH_SHORT).show();
            }
            break;

         case Co.REQUEST_AUTHORIZATION:
            if (resultCode == RESULT_OK) {
//               Toast.makeText(this, "onActivityResult request auth", Toast.LENGTH_SHORT).show();
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
//            Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "onActivityResult 1007", Toast.LENGTH_SHORT).show();
            if (result.isSuccess()) {
//               Toast.makeText(this, "1007 result success", Toast.LENGTH_SHORT).show();
               SharedPreferences prefs =
                     PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
               SharedPreferences.Editor editor = prefs.edit();
               editor.putBoolean(Co.IS_FIRST_LAUNCH, false);
               GoogleSignInAccount acct = result.getSignInAccount();
               if (acct != null) {
//                  Toast.makeText(this, "1007 account not null", Toast.LENGTH_SHORT).show();
                  Account account = acct.getAccount();
                  editor.putString(Co.USER_EMAIL, account != null ? account.name : null);
                  editor.putString(Co.USER_NAME, acct.getDisplayName());
                  mCredential.setSelectedAccount(acct.getAccount());
                  if (acct.getPhotoUrl() != null) {
                     editor.putString(Co.USER_PIC_URL, acct.getPhotoUrl().toString());
                  }
                  editor.apply();
                  signIn();
               } else
               signIn();
            } else {
               Toast.makeText(this, "There was an error while attempting to sign in", Toast.LENGTH_SHORT).show();
            }
      }
   }

   @Override
   protected void onStop() {
      super.onStop();
//      if (mGoogleApiClient != null){
//         if (mGoogleApiClient.isConnected()){
//            mGoogleApiClient.disconnect();
//         }
//      }
   }


   @Override
   public void onRequestPermissionsResult(int requestCode,
                                          @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      EasyPermissions.onRequestPermissionsResult(
            requestCode, permissions, grantResults, this);
//      Toast.makeText(this, "onRequestPerm", Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onPermissionsGranted(int requestCode, List<String> list) {
//      Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
      FirstAPICall firstCall = new FirstAPICall(this, mCredential);
      firstCall.execute();
   }

   @Override
   public void onPermissionsDenied(int requestCode, List<String> list) {
//      Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
      if (EasyPermissions.somePermissionPermanentlyDenied(this, list)) {
         new AppSettingsDialog.Builder(this, getString(R.string.contacts_permissions_rationale)).build().show();
      }
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
//      Toast.makeText(this, "goToTaskListActivity", Toast.LENGTH_SHORT).show();
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

   @Override
   public void onConnected(@Nullable Bundle bundle) {
//      Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onConnectionSuspended(int i) {
//      Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
      Log.d("SignIn", "onConnectionSuspended: suspended");
   }


   private class FirstAPICall extends AsyncTask<Void, Void, List<String>> {

      private com.google.api.services.tasks.Tasks mService = null;
      private Exception mLastError = null;
      Context context;

      private FirstAPICall(Context context, GoogleAccountCredential credential) {
//         Toast.makeText(SignInActivity.this, "FirstApi constructor", Toast.LENGTH_SHORT).show();
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
//         Toast.makeText(SignInActivity.this, "onPreEx called", Toast.LENGTH_SHORT).show();
         mProgress.show();
         int currentOrientation = getResources().getConfiguration().orientation;
         if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
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
         if (android.provider.Settings.System.getInt(getContentResolver(),
               Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
         } else {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
         }
         goToTaskListActivity();
      }

      @Override
      protected void onCancelled(List<String> strings) {
         mProgress.dismiss();
//         Toast.makeText(SignInActivity.this, "onCanceled called", Toast.LENGTH_SHORT).show();
         if (android.provider.Settings.System.getInt(getContentResolver(),
               Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
         } else {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
         }
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
         List<TaskList> serverLists;
         List<String> defaultListInfo = new ArrayList<>();
         TaskLists result = mService.tasklists().list()
               .execute();
         serverLists = result.getItems();
         for (int i = 0; i < serverLists.size(); i++) {
            Co.listIds.clear();
            Co.listTitles.clear();
            Co.listIds.add(serverLists.get(i).getId());
            Co.listTitles.add(serverLists.get(i).getTitle());
         }
         defaultListInfo.add(serverLists.get(0).getId());
         defaultListInfo.add(serverLists.get(0).getTitle());
         return defaultListInfo;
      }
   }

}