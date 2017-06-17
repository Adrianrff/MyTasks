package com.adrapps.mytasks.views;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.adrapps.mytasks.R;

/**
 * Created by adria on 17/06/2017.
 */

public class Preferences extends PreferenceFragment {

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
   }
}
