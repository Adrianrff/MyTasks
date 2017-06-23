package com.adrapps.mytasks.helpers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import static android.content.ContentValues.TAG;

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

   private static GoogleApiClient mGoogleApiClient;
   private static com.google.api.services.tasks.Tasks mService;
   private static HttpTransport mTransport;
   private static JsonFactory mJsonFactory;

   public static GoogleApiClient getClient(Context context){
      if (mGoogleApiClient == null) {
         Log.d(TAG, "buildGoogleApiClient: Client null, creating");
         GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               .requestEmail()
               .requestProfile()
               .build();
         mGoogleApiClient = new GoogleApiClient.Builder(context.getApplicationContext())
               .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
               .build();
         return mGoogleApiClient;
      } else {
         Log.d(TAG, "getClient: Client not null: returning");
         return mGoogleApiClient;
      }
   }

   public static com.google.api.services.tasks.Tasks getService(GoogleAccountCredential mCredential){
      mTransport = getHttpTransport();
      mJsonFactory = getJacksonFactory();
      if (mService == null){
         mService = new com.google.api.services.tasks.Tasks.Builder(
               mTransport, mJsonFactory, mCredential)
               .setApplicationName("My Tasks")
               .build();
      }
      return mService;
   }

   private static HttpTransport getHttpTransport(){
      if (mTransport == null){
         mTransport = AndroidHttp.newCompatibleTransport();
      }
      return mTransport;
   }

   private static JsonFactory getJacksonFactory(){
      if (mJsonFactory == null){
         mJsonFactory = JacksonFactory.getDefaultInstance();
      }
      return mJsonFactory;
   }

//   public static void revokeAccess() {
//      if (mGoogleApiClient != null){
//         if (!mGoogleApiClient.isConnected()){
//            mGoogleApiClient.connect();
//         }
//         Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
//               new ResultCallback<Status>() {
//                  @Override
//                  public void onResult(Status status) {
//                     Log.d(TAG, "onResult:" + status.getStatus().toString());
//                  }
//               });
//
//      }
//
//   }


   @Override
   public void onConnected(@Nullable Bundle bundle) {

   }

   @Override
   public void onConnectionSuspended(int i) {

   }

   @Override
   public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

   }
}
