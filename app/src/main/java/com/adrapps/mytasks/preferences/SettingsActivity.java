package com.adrapps.mytasks.preferences;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.domain.Co;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
   /**
    * A preference value change listener that updates the preference's summary
    * to reflect its new value.
    */
   private static final String TAG = "SettingsActivity";
   private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object value) {
         String stringValue = value.toString();
         String key = preference.getKey();
         if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                  index >= 0
                        ? listPreference.getEntries()[index]
                        : null);

         } else if (preference instanceof RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            if (TextUtils.isEmpty(stringValue)) {
               // Empty values correspond to 'silent' (no ringtone).
               preference.setSummary(R.string.pref_ringtone_silent);

            } else {
               Ringtone ringtone = RingtoneManager.getRingtone(
                     preference.getContext(), Uri.parse(stringValue));

               if (ringtone == null) {
                  // Clear the summary if there was a lookup error.
                  preference.setSummary(null);
               } else {
                  // Set the summary to reflect the new ringtone display
                  // name.
                  String name = ringtone.getTitle(preference.getContext());
                  preference.setSummary(name);
                  Log.d(TAG, "onPreferenceChange: " + stringValue);
               }
            }

         } else if (preference instanceof NumberPickerPreference) {
            int hour = (int) value;
            if (hour >= 12) {
               if (hour != 12) {
                  hour = hour - 12;
               }
               preference.setSummary(String.valueOf(hour) + " p.m");

            } else {
               if (hour == 0) {
                  preference.setSummary(String.valueOf(12) + " a.m");
               } else {
                  preference.setSummary(String.valueOf(hour) + " a.m");
               }
            }

         } else if (preference instanceof SwitchPreference){
            if ((boolean) value){
               preference.setSummary(preference.getContext().getString(R.string.save_task_on_back_pressed_summary));
            } else {
               preference.setSummary(preference.getContext().getString(R.string.dont_save_task_on_back_pressed_summary));
            }

         } else {
            preference.setSummary(stringValue);
         }

         return true;
      }
   };

   /**
    * Helper method to determine if the device has an extra-large screen. For
    * example, 10" tablets are extra-large.
    */
   private static boolean isXLargeTablet(Context context) {
      return (context.getResources().getConfiguration().screenLayout
            & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
   }

   /**
    * Binds a preference's summary to its value. More specifically, when the
    * preference's value is changed, its summary (line of text below the
    * preference title) is updated to reflect the value. The summary is also
    * immediately updated upon calling this method. The exact display format is
    * dependent on the type of preference.
    *
    * @see #sBindPreferenceSummaryToValueListener
    */
   private static void bindPreferenceSummaryToValue(Preference preference) {
      // Set the listener to watch for value changes.
      preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

      // Trigger the listener immediately with the preference's
      // current value.
      if (preference instanceof SwitchPreference) {
         sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
               PreferenceManager
                     .getDefaultSharedPreferences(preference.getContext())
                     .getBoolean(preference.getKey(), false));
      } else if (preference instanceof NumberPickerPreference) {
         sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
               PreferenceManager
                     .getDefaultSharedPreferences(preference.getContext())
                     .getInt(preference.getKey(), 0));

      } else {
         sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
               PreferenceManager
                     .getDefaultSharedPreferences(preference.getContext())
                     .getString(preference.getKey(), ""));
      }
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getFragmentManager().beginTransaction().replace(android.R.id.content,
            new Preferences()).commit();
      setupActionBar();
   }

   /**
    * Set up the {@link android.app.ActionBar}, if the API is available.
    */
   private void setupActionBar() {
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
         // Show the Up button in the action bar.
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setTitle(getString(R.string.settings_title));
      }
   }

   @Override
   public boolean onMenuItemSelected(int featureId, MenuItem item) {
      int id = item.getItemId();
      if (id == android.R.id.home) {
         finish();
         return true;
      }
      return super.onMenuItemSelected(featureId, item);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean onIsMultiPane() {
      return isXLargeTablet(this);
   }

//   /**
//    * {@inheritDoc}
//    */
//   @Override
//   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//   public void onBuildHeaders(List<Header> target) {
//      loadHeadersFromResource(R.xml.pref_headers, target);
//   }

   /**
    * This method stops fragment injection in malicious applications.
    * Make sure to deny any unknown fragments here.
    */
   protected boolean isValidFragment(String fragmentName) {
      return PreferenceFragment.class.getName().equals(fragmentName)
            || Preferences.class.getName().equals(fragmentName);
   }

   /**
    * This fragment shows general preferences only. It is used when the
    * activity is showing a two-pane settings UI.
    */
   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   public static class Preferences extends PreferenceFragment {

      private String accountName;

      @Override
      public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
         setHasOptionsMenu(true);
         Preference accountPref = findPreference("account_pref");
         accountPref.setSummary(accountName);
//         SwitchPreference saveOnBackPref = (SwitchPreference) findPreference(Co.SAVE_ON_BACK_PRESSED);
//         if (saveOnBackPref.isChecked()){
//            saveOnBackPref.setSummary(getString(R.string.dont_save_task_on_back_pressed_summary));
//         } else {
//            saveOnBackPref.setSummary(getString(R.string.save_task_on_back_pressed_summary));
//         }
         bindPreferenceSummaryToValue(findPreference(Co.MORNING_ALARM_KEY));
         bindPreferenceSummaryToValue(findPreference(Co.AFTERNOON_ALARM_KEY));
         bindPreferenceSummaryToValue(findPreference(Co.EVENING_ALARM_KEY));
         bindPreferenceSummaryToValue(findPreference(Co.SAVE_ON_BACK_PRESSED));
         bindPreferenceSummaryToValue(findPreference(Co.REMINDER_RINGTONE));
      }

      @Override
      public void onAttach(Context context) {
         super.onAttach(context);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
         accountName = prefs.getString(Co.USER_EMAIL, Co.NO_ACCOUNT_NAME);
      }

      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();
         if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
         }
         return super.onOptionsItemSelected(item);
      }
   }

//   /**
//    * This fragment shows notification preferences only. It is used when the
//    * activity is showing a two-pane settings UI.
//    */
//   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//   public static class NotificationPreferenceFragment extends PreferenceFragment {
//      @Override
//      public void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         addPreferencesFromResource(R.xml.pref_notification);
//         setHasOptionsMenu(true);
//
//         // Bind the summaries of EditText/List/Dialog/Ringtone preferences
//         // to their values. When their values change, their summaries are
//         // updated to reflect the new value, per the Android Design
//         // guidelines.
//         bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
//      }
//
//      @Override
//      public boolean onOptionsItemSelected(MenuItem item) {
//         int id = item.getItemId();
//         if (id == android.R.id.home) {
//            startActivity(new Intent(getActivity(), SettingsActivity.class));
//            return true;
//         }
//         return super.onOptionsItemSelected(item);
//      }
//   }
//
//   /**
//    * This fragment shows data and sync preferences only. It is used when the
//    * activity is showing a two-pane settings UI.
//    */
//   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//   public static class DataSyncPreferenceFragment extends PreferenceFragment {
//      @Override
//      public void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         addPreferencesFromResource(R.xml.pref_data_sync);
//         setHasOptionsMenu(true);
//
//         // Bind the summaries of EditText/List/Dialog/Ringtone preferences
//         // to their values. When their values change, their summaries are
//         // updated to reflect the new value, per the Android Design
//         // guidelines.
//         bindPreferenceSummaryToValue(findPreference("sync_frequency"));
//      }
//
//      @Override
//      public boolean onOptionsItemSelected(MenuItem item) {
//         int id = item.getItemId();
//         if (id == android.R.id.home) {
//            startActivity(new Intent(getActivity(), SettingsActivity.class));
//            return true;
//         }
//         return super.onOptionsItemSelected(item);
//      }
//   }
}
