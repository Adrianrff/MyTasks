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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import static android.content.ContentValues.TAG;

/**
 * Created by adria on 19/06/2017.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

   Context context;
   static GoogleApiClient mGoogleApiClient;


   public static void connect() {
      if (mGoogleApiClient != null) {
         mGoogleApiClient.connect();
      }
   }

   public static void disconnect() {
      if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
         mGoogleApiClient.disconnect();
      }
   }

   public static boolean isConnected() {
      return mGoogleApiClient != null && mGoogleApiClient.isConnected();
   }

   private android.content.Context getContext() {
      return context;
   }

   public static GoogleApiClient getClient(Context context){
      if (mGoogleApiClient == null) {
         Log.d(TAG, "buildGoogleApiClient: Client null, creating");
         GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               .requestEmail()
               .requestProfile()
               .build();
         mGoogleApiClient = new GoogleApiClient.Builder(context)
               .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
               .build();
         return mGoogleApiClient;
      } else {
         Log.d(TAG, "getClient: Client not null: returning");
         return mGoogleApiClient;
      }
   }

   public static void revokeAccess() {
      if (mGoogleApiClient != null){
         if (!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
         }
         Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
               new ResultCallback<Status>() {
                  @Override
                  public void onResult(Status status) {
                     Log.d(TAG, "onResult:" + status.getStatus().toString());
                  }
               });

      }

   }


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
