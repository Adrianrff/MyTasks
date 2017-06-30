package com.adrapps.mytasks.interfaces;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
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

      boolean isDestroyed();

      boolean isFinishing();

      void findViews();

      void setUpViews();

      void showToast(String msg);

      void showCircularProgress(boolean b);

      void showEmptyRecyclerView(boolean b);

      void showNoInternetWarning(boolean b);

      void showProgressDialog();

      void dismissProgressDialog();

      void showSwipeRefreshProgress(boolean b);

      void updateView();

      void showDeleteSnackBar(String message, SparseArray map);

      void showBottomSheet(LocalTask task, int position, boolean b);

      void refreshFirstTime();

      void refresh();


      //------------DATA SETUP-----------///

      void setNavDrawerMenu(List<LocalList> lists);

      void setSwipeRefreshEnabled(boolean b);

      void setListsData();

      void setToolbarTitle(String title);

      void requestAuthorization(Exception e);

      void setCredentials();

      int getIntShP(String key, int defaultValue);

      void saveIntShP(String key, int value);

      void saveBooleanShP(String key, boolean value);

      void saveStringShP(String key, String value);

      void initRecyclerView(List<LocalTask> tasks);

      void addTaskToAdapter(LocalTask localTask);

      //-------DATA RETRIEVE-------------///

      GoogleAccountCredential getCredential();

      boolean getBooleanShP(String key, boolean defaultValue);

      String getStringShP(String key, @Nullable String defaultValue);

      Context getContext();

      boolean isDeviceOnline();

      void navigateToEditTask(Intent i);

      void updateItem(LocalTask syncedLocalTask);

      void showFab(boolean b);

      void lockScreenOrientation();

      void unlockScreenOrientation();
   }


   //-------------------------MODEL OPS----------------------//
   public interface Model {

      void closeDatabases();

      List<LocalList> createListsDatabase(List<TaskList> lists);

      void updateTasksFirstTime(List<LocalTask> tasks);

      List<LocalTask> getTasksFromList(String listId);

      List<String> getListsTitles();

      List<String> getListsIds();

      String getListTitleFromIntId(int listIntId);

      boolean listExistsInDB(int listIntId);

      int addTaskToLocalDatabase(LocalTask task);

      void addTaskFirstTimeFromServer(Task task, String listId);

      int addListNewToDb(String listTitle);

      void updateTaskStatusInServer(int intId, String listId, String newStatus);

      void updateTaskStatusInDB(int intId, String newStatus);

      void refreshFirstTime();

      int addTask(LocalTask task);

      void moveTasks(LinkedHashMap<LocalTask, String> moveMap);

      void editTask(LocalTask task);

      long updateReminder(String taskId, long reminder);

      long updateReminder(int intId, long reminder, int repeatMode);

      void updateSyncStatus(int synced, int intId);

      List<LocalTask> getLocalTasksFromDB();

      int updateLocalTask(LocalTask modifiedTask, boolean updateReminders);

      List<LocalTask> getTaskFromListForAdapter(int listIntId);

      void updateLocalTask(Task task, String listId);

      LocalTask updateNewlyCreatedTask(Task aTask, String listId, int taskIntId);

      void updatePosition(Task task);

      String getTaskIdByIntId(int id);

      void deleteTaskFromDatabase(int intId);

      int getIntIdByTaskId(String taskId);

      void addNewListToServer(String listTitle, int listIntId);

      void updateListInDBFromLocalListAfterServerOp(LocalList localList);

      void changeListNameInDB(int listIntId, String title);

      void changeListNameInServer(String listId, String title);

      void updateListInDBFromServerList(TaskList list, int intId);

      int getListsCount();

      void deleteListFromServer(String listId);

      void deleteListFromDb(int listIntId);

      void updateNewTasksInBulk(HashMap<Task, LocalTask> map);

      void deleteTasks(List<LocalTask> tasks);

      void updatePositions(List<Task> tasks);

      void updateExistingTaskFromLocalTask(LocalTask task, String listId);

      List<LocalList> getLocalLists();

      int getListIntIdById(String listId);

      String getListIdByIntId(int listIntId);

      void addNewListToDBFromServer(TaskList serverList);

      List<Integer> getListsIntIds();

      void markListDeleted(int listIntId);

      void deleteTasksFromList(int listIntId);

      List<LocalTask> getTasksFromList(int intId);
   }


}
