package com.adrapps.mytasks.interfaces;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

        void showTaskDeleteUndoSnackBar(String message, SparseArray map);

        void showBottomSheet(LocalTask task, int position, boolean b);

        void refreshFirstTime();

        void refresh();



        //------------DATA SETUP-----------///

        void setNavDrawerMenu(List<String> taskListsTitles);

       void setSwipeRefreshEnabled(boolean b);

       void setListsData();

        void setToolbarTitle(String title);

        void requestAuthorization(Exception e);

        void setCredentials();

        void saveBooleanShP(String key, boolean value);

        void saveStringShP(String key, String value);

        void initRecyclerView(List<LocalTask> tasks);

        void addTaskToAdapter(LocalTask localTask);

        //-------DATA RETRIEVE-------------///

        GoogleAccountCredential getCredential();

        boolean getBooleanShP(String key);

        String getStringShP(String key);

        Context getContext();

        boolean isDeviceOnline();

        void navigateToEditTask(Intent i);

        void updateItem(LocalTask syncedLocalTask);

       void showFab(boolean b);
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

        void updateTaskStatus(int intId, String listId, String newStatus);

        void refreshFirstTime();

        int addTask(LocalTask task);

        void moveTasks(LinkedHashMap<LocalTask, String> moveMap);

        void editTask(LocalTask task);

        long updateReminder(String taskId, long reminder);

        long updateReminder(int intId, long reminder, int repeatMode);

        void updateSyncStatus(int synced, int intId);

        List<LocalTask> getLocalTasksFromDB();

        int updateLocalTask(LocalTask modifiedTask, boolean updateReminders);

        List<LocalTask> getTaskFromListForAdapter(String listId);

        void updateLocalTask(Task task, String listId);

        LocalTask updateNewlyCreatedTask(Task aTask, String listId, String intId);

        void updatePosition(Task task);

        String getTaskIdByIntId(int id);

        void deleteTaskFromDatabase(int intId);

        int getIntIdByTaskId(String taskId);

        void addList(String listTitle);

        void updateList(LocalList localList);

        void editList(String listId, String title);

        void updateList(TaskList list);

        void deleteList(String listId);

        void updateNewTasksInBulk(HashMap<Task, LocalTask> map);

        void deleteTasks(List<LocalTask> tasks);

       void updatePositions(List<Task> tasks);

       void updateExistingTaskFromLocalTask(LocalTask task, String listId);
    }


}
