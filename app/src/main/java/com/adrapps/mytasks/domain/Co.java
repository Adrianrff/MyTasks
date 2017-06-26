package com.adrapps.mytasks.domain;

import com.google.api.services.tasks.TasksScopes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Co {

   //API and authorization
   public static final int REQUEST_AUTHORIZATION = 1001;
   public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
   public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
   public static final String NO_ACCOUNT_NAME = "no account name";
   public static final String[] SCOPES = {TasksScopes.TASKS, "https://www.googleapis.com/auth/userinfo.profile"};
   public static final String USER_PIC_URL = "user pic url";
   public static final String USER_NAME = "user name";
   public static final String USER_EMAIL = "accountName";
   public static final String TASK_STATUS = "Task status";
   public static final String BUNDLED_EXTRA = "Bundled extra";
   public static final String NO_API_EDIT = "No api edit";
   public static final String SAVE_ON_BACK_PRESSED_PREF_KEY = "save on back pressed";


   public static String OP_UPDATE_STATUS;

   //Lists
   public static final String CURRENT_LIST_ID = "current list";
   public static final String CURRENT_LIST_TITLE = "current list title";
   public static final int NEW_LIST_MENU_ITEM_ID = 1001;
   public static List<String> listIds = new ArrayList<>();
   public static List<String> listTitles = new ArrayList<>();

   //Flags
   public static final String IS_FIRST_LAUNCH = "is first time";
   public static final String IS_FIRST_INIT = "first init";

   //Tasks
   public static final String LOCAL_TASK = "Local task";
   public static final String TASK_TITLE = "task name";
   public static final String TASK_COMPLETED = "completed";
   public static final String TASK_NEEDS_ACTION = "needsAction";
   public static final String TASK_DUE = "task edited due date";
   public static final int TASK_DATA_REQUEST_CODE = 99;
   public static final String ADAPTER_POSITION = "Adapter position";
   public static final String TASK_EDIT = "Task edit";
   public static final String NEW_TASK = "New task";
   public static final String TASK_ID_ORDERED_LIST = "Task_ordered_list";
   public static final String STATE_SHOWN_TASK = "Task shown in Bottom sheet";
   public static final String STATE_SHOWN_TASK_POSITION = "Task shown in Bottom sheet position";
   public static final String TASK_ID = "task id";
   public static final String TASK_INT_ID = "task int id";
   public static final String TASK_LIST_ID = "task list id";

   //Reminder
   public static final int MORNING_DEFAULT_REMINDER_TIME = 8;
   public static final int AFTERNOON_DEFAULT_REMINDER_TIME = 14;
   public static final int EVENING_DEFAULT_REMINDER_TIME = 19;
   public static final String MORNING_REMINDER_PREF_KEY = "morning_reminder_pref";
   public static final String AFTERNOON_REMINDER_PREF_KEY = "afternoon_reminder_pref";
   public static final String EVENING_REMINDER_PREF_KEY = "evening_reminder_pref";
   public static final String REMINDER_RINGTONE_PREF_KEY = "Reminder ringtone";
   public static final int REMINDER_ONE_TIME = 0;
   public static final int REMINDER_DAILY = 1;
   public static final int REMINDER_DAILY_WEEKDAYS = 2;
   public static final int REMINDER_WEEKLY = 3;
   public static final int REMINDER_MONTHLY = 4;
   public static final int MONDAY = Calendar.MONDAY;
   public static final int TUESDAY = Calendar.TUESDAY;
   public static final int WEDNESDAY = Calendar.WEDNESDAY;
   public static final int THURSDAY = Calendar.THURSDAY;
   public static final int FRIDAY = Calendar.FRIDAY;
   public static final int SATURDAY = Calendar.SATURDAY;
   public static final int SUNDAY = Calendar.SUNDAY;
   public static int REMINDER_CUSTOM_REPEAT = 5;
   public static final long ONE_DAY_LATER = 24 * 60 * 60 * 1000;
   public static final int NOT_ID_SUFIX = 1000;
   public static final String STATE_TASK_REMINDER = "State task reminder";
   public static final String STATE_REPEAT_MODE = "State repeat mode";
   public static final String VIBRATE_REMINDER_PREF_KEY = "reminder vibrate";
   public static final String DEFAULT_REMINDER_PREF_KEY = "default_reminder_pref_key";
   public static final String DEFAULT_REMINDER_TIME_PREF_KEY = "default_reminder_time_pref_key";
   public static final int DEFAULT_REMINDER_IDENTIFIER = 33333;



   //Sync status
   public static final int NOT_SYNCED = 0;
   public static final int EDITED_NOT_SYNCED = 1;
   public static final int SYNCED = 2;
   public static final int NOT_MOVED = 0;
   public static final int IS_FIRST = -2;
   public static final int LOCAL_DELETED = 1;
   public static final int MOVED = 1;
   public static final String TASK_MOVED_TO_FIRST = "first";
   public static final int NOT_DELETED = 0;

   public static final int SNACKBAR_DURATION = 6000;
   public static final String NO_VALUE = "no value";
   public static final String STATE_DUE_DATE = "State due date in millis";

   public static void setListIds(List<String> listIds) {
      Co.listIds = listIds;
   }

   public static void setListTitles(List<String> listTitles) {
      Co.listTitles = listTitles;
   }

   public static boolean IS_MULTISELECT_ENABLED;

}
