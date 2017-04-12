package com.adrapps.mytasks.Domain;

import com.google.api.services.tasks.TasksScopes;

public class Co {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String NO_ACCOUNT_NAME = "no account name";
    public static final String CURRENT_LIST_ID = "current list";
    public static final String IS_FIRST_TIME = "is first time";
    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = {TasksScopes.TASKS};
    public static final String IS_FIRST_INIT = "first init";
    public static final String CURRENT_LIST_TITLE = "current list title";
    public static final String FIRST_REFRESH = "first refresh";
    public static final String DETAIL_TASK_TITLE = "task name";
    public static final String DETAIL_TASK_NOTE = "task note";
    public static final String DETAIL_TASK_DUE = "task due date";
    public static final String NO_VALUE = "no value";
    public static final String TASK_COMPLETED = "completed";
    public static final String TASK_NEEDS_ACTION = "needsAction";
}
