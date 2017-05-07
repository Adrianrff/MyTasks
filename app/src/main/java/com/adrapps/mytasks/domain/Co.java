package com.adrapps.mytasks.domain;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;

public class Co {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String NO_ACCOUNT_NAME = "no account name";
    public static final String CURRENT_LIST_ID = "current list";
    public static final String IS_FIRST_LAUNCH = "is first time";
    public static final String[] SCOPES = {TasksScopes.TASKS, "https://www.googleapis.com/auth/userinfo.profile"};
    public static final String IS_FIRST_INIT = "first init";
    public static final String CURRENT_LIST_TITLE = "current list title";
    public static final String TASK_TITLE = "task name";
    public static final String NO_VALUE = "no value";
    public static final String TASK_COMPLETED = "completed";
    public static final String TASK_NEEDS_ACTION = "needsAction";
    public static final String TASK_DUE = "task edited due date";
    public static final int TASK_DATA_REQUEST_CODE = 99;
    public static final int MORNING_ALARM_HOUR = 8;
    public static final int AFTERNOON_ALARM_HOUR = 14;
    public static final int EVENING_ALARM_HOUR = 18;
    public static final String LOCAL_TASK = "Local task";
    public static final String ADAPTER_POSITION = "Adapter position";
    public static final String TASK_EDIT = "Task edit";
    public static final String NEW_TASK = "New task";
    public static final int NOT_SYNCED = 0;
    public static final int EDITED_NOT_SYNCED = 1;
    public static final int SYNCED = 2;
    public static final int NOT_MOVED = 0;
    public static final int MOVED_TO_FIRST = -2;
    public static final String EVERY_DAY_OF_MONTH = "Every day of month";
    public static final int NEW_LIST_MENU_ITEM_ID = 1001;

    public static List<String> listIds = new ArrayList<>();
    public static List<String> listTitles = new ArrayList<>();
    public static int LOCAL_DELETED = 1;
    public static int MOVED = 1;

    public static Task task;
    public static final int SNACKBAR_DURATION = 6000;
    public static String ORDER_TYPE = "order type";
    public static final String TASK_MOVED_TO_FIRST = "first";
    public static final String TASK_REMINDER = "task reminder";
    public static final String TASK_ID = "task id";
    public static int NOT_DELETED = 0;
    public static final String USER_PIC_URL = "user pic url";
    public static final String USER_NAME = "user name";
    public static final String USER_EMAIL = "accountName";

    public static GoogleAccountCredential mCredential;


    public static void setListIds(List<String> listIds) {
        Co.listIds = listIds;
    }

    public static void setListTitles(List<String> listTitles) {
        Co.listTitles = listTitles;
    }


}
