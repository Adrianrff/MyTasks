package com.adrapps.mytasks.Interfaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;

import com.adrapps.mytasks.Domain.LocalTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;

public class Contract {

    //-------------------------VIEW OPS----------------------//
    public interface MainActivityViewOps {


        //-----------VIEWS AND WIDGETS------------////

        void setUpViews();

        void showAndSetUpNewTaskDialog();

        void showToast(String msg);

        void showCircularProgress(boolean b);

        void showEmptyRecyclerView(boolean b);

        void showProgressDialog();

        void dismissProgressDialog();

        void showSwipeRefreshProgress(boolean b);

        void updateCurrentView();

        void showUndoSnackBar(String message, int position, LocalTask task);

        void refreshFirstTime();

        void refresh();



        //------------DATA SETUP-----------///

        void setNavDrawerMenu(List<String> taskListsTitles);

        void setUpData();

        void setToolbarTitle(String title);

        void requestAuthorization(Exception e);

        void setCredentials();

        void saveBooleanShP(String key, boolean value);

        void saveStringShP(String key, String value);

        void initRecyclerView(List<LocalTask> tasks);

        void setReminder(LocalTask task);

        void addTaskToAdapter(LocalTask localTask);

        //-------DATA RETRIEVE-------------///

        GoogleAccountCredential getCredential();

        boolean getBooleanSharedPreference(String key);

        String getStringShP(String key);

        Context getContext();

        boolean isDeviceOnline();

        void navigateToEditTask(Intent intent);
    }


    //-------------------------MODEL OPS----------------------//
    public interface Model{

        void updateLists(List<TaskList> lists);

        void updateTasks(List<LocalTask> tasks);

        List<LocalTask> getTasksFromList(String listId);

        List<String> getListsTitles();

        List<String> getListsIds();

        String getListTitleFromId(String listId);

        void addTaskToLocalDatabase(LocalTask task);

        void addTaskToLocalDatabase(Task task, String listId);

        int deleteTask(String taskId);

        void deleteTask(String taskId, String listId);

        void updateTaskStatus(String taskId, String listId, String newStatus);

        void refreshFirstTime();

        void addTask(LocalTask task);

        void moveTask(String[] params);

        void editTask(LocalTask task);

        long getTaskReminder(String taskId);

        boolean taskExistsInDB(String taskId);

        long updateReminder(String taskId, long reminder);

        void updateSyncStatus(int synced, String taskId);

        long getTaskReminderId(String taskId);

        List<LocalTask> getLocalTasksFromDB();

        LocalTask getTask(String id);

        int updateLocalTask(LocalTask modifiedTask);

        void updateSibling(String taskId, String previousTaskId);

        List<LocalTask> getTaskFromLlistForAdapter(String listId);

        void updateLocalTask(Task task, String listId);
    }


}
