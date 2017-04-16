package com.adrapps.mytasks.Domain;

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
    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = {TasksScopes.TASKS};
    public static final String IS_FIRST_INIT = "first init";
    public static final String CURRENT_LIST_TITLE = "current list title";
    public static final String FIRST_REFRESH = "first refresh";
    public static final String TASK_TITLE = "task name";
    public static final String DETAIL_TASK_NOTE = "task note";
    public static final String DETAIL_TASK_DUE = "task due date";
    public static final String NO_VALUE = "no value";
    public static final String TASK_COMPLETED = "completed";
    public static final String TASK_NEEDS_ACTION = "needsAction";
    public static final String TASK_EDITED_TITLE = "task edited title";
    public static final String TASK_EDITED_NOTE = "task edited note";
    public static final String TASK_DUE = "task edited due date";
    public static final int TASK_DATA_REQUEST_CODE = 99;
    public static final String DETAIL_TASK_ID = "task id" ;
    public static final String DETAIL_TASK_LIST_ID = "task list";
    public static final String ORDER_POSITION_ASC = "Order by position asc";
    public static final String ORDER_POSITION_DESC = "Order by position desc";
    public static final String ORDER_DUE_DATE_ASC = "Order by due date asc";
    public static final String ORDER_DUE_DATE_DESC = "Order by due date desc";
    public static final String ORDER_UPDATED_ASC = "Order by updated asc";
    public static final String ORDER_UPDATED_DESC = "Order by updated desc";
    public static List<String> listIds = new ArrayList<>();
    public static List<String> listTitles = new ArrayList<>();

    public static Task task;
    public static final int SNACKBAR_DURATION = 6000;
    public static String ORDER_TYPE = "order type";
    public static final String TASK_MOVED_TO_FIRST = "first";
    public static final String TASK_REMINDER = "task reminder";


    public static void setListIds(List<String> listIds) {
        Co.listIds = listIds;
    }

    public static void setListTitles(List<String> listTitles) {
        Co.listTitles = listTitles;
    }
}
