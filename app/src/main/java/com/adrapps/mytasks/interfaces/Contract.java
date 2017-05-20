package com.adrapps.mytasks.interfaces;

import android.content.Context;
import android.content.Intent;

import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.HashMap;
import java.util.List;

public class Contract {

    //-------------------------VIEW OPS----------------------//
    public interface MainActivityViewOps {


        //-----------VIEWS AND WIDGETS------------////

        void findViews();

        void setUpViews();

        void showToast(String msg);

        void showCircularProgress(boolean b);

        void showEmptyRecyclerView(boolean b);

        void showNoInternetWarning(boolean b);

        void showProgressDialog();

        void dismissProgressDialog();

        void showSwipeRefreshProgress(boolean b);

        void updateCurrentView();

        void showTaskDeleteUndoSnackBar(String message, int position, LocalTask task);

        void showBottomSheet(LocalTask task, int position, boolean b);

        void refreshFirstTime();

        void refresh();



        //------------DATA SETUP-----------///

        void setNavDrawerMenu(List<String> taskListsTitles);

        void setListsData();

        void setToolbarTitle(String title);

        void requestAuthorization(Exception e);

        void setCredentials();

        void saveBooleanShP(String key, boolean value);

        void saveStringShP(String key, String value);

        void initRecyclerView(List<LocalTask> tasks);

//        void setOrUpdateAlarm(LocalTask task);

        void addTaskToAdapter(LocalTask localTask);

        //-------DATA RETRIEVE-------------///

        GoogleAccountCredential getCredential();

        boolean getBooleanShP(String key);

        String getStringShP(String key);

        Context getContext();

        boolean isDeviceOnline();

        void navigateToEditTask(Intent i);

//        void cancelReminder(LocalTask task);

        boolean isReminderSet(int reminderId);

        void updateItem(LocalTask syncedLocalTask);
    }


    //-------------------------MODEL OPS----------------------//
    public interface Model{

        void closeDatabases();

        void updateLists(List<TaskList> lists);

        void updateTasksFirstTime(List<LocalTask> tasks);

        List<LocalTask> getTasksFromList(String listId);

        List<String> getListsTitles();

        List<String> getListsIds();

        String getListTitleFromId(String listId);

        int addTaskToLocalDatabase(LocalTask task);

        void addTaskFirstTimeFromServer(Task task, String listId);

        int addListToDb(String listTitle);

        void deleteTask(String taskId, String listId);

        void updateTaskStatus(int intId, String listId, String newStatus);

        void refreshFirstTime();

        int addTask(LocalTask task);

        void moveTask(LocalTask movedTask, String previousTaskId);

        void editTask(LocalTask task);

        long getTaskReminder(String taskId);

        boolean taskExistsInDB(String taskId);

        long updateReminder(String taskId, long reminder);

        long updateReminder(int intId, long reminder);

        void updateSyncStatus(int synced, int intId);

        long getTaskReminderId(String taskId);

        List<LocalTask> getLocalTasksFromDB();

        LocalTask getTask(String id);

        int updateLocalTask(LocalTask modifiedTask, boolean updateReminders);

        void updateSibling(String taskId, String previousTaskId);

        List<LocalTask> getTaskFromListForAdapter(String listId);

        void updateLocalTask(Task task, String listId);

        void markDeleted(String taskId);

        LocalTask updateNewlyCreatedTask(Task aTask, String listId, String intId);

        void setTemporaryPosition(String taskId, String newTaskTempPos);

        void updateMoved(String id, int moved);

        void updatePosition(Task task);

        void updateSiblingByIntId(int id, int sibling);

        String getTaskIdByIntId(int id);

        void updateMovedByIntId(int intId, int moved);

        void setTemporaryPositionByIntId(int intId, String newTaskTempPos);

        void deleteTaskFromDatabase(int intId);

        int getIntIdByTaskId(String taskId);

        void addList(String listTitle);

        void addList(TaskList list);

        void updateList(LocalList localList);

        void editList(String listId, String title);

        void updateList(TaskList list);

        void deleteList(String listId);

        List<LocalList> getLocalLists();

        LocalTask getTask(int intId);

        int getTaskReminderRepeatModeByIntId(int intId);

        int getTaskReminderRepeatMode(String taskId);

        long getTaskReminderByIntId(int intId);

        void updateNewTasksInBulk(HashMap<Task, LocalTask> map);
    }


}
