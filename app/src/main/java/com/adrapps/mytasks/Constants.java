package com.adrapps.mytasks;

import com.google.api.services.tasks.TasksScopes;

public class Constants {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String NO_ACCOUNT_NAME = "no account name";
    public static final String CURRENT_LIST = "current list";
    public static final String IS_FIRST_TIME = "is first time";
    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = {TasksScopes.TASKS};
    public static final String IS_FIRST_INIT = "first init";
}
