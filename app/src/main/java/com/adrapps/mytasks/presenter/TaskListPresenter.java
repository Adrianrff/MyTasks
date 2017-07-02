package com.adrapps.mytasks.presenter;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;

import com.adrapps.mytasks.api_calls.SyncTasks;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.models.DataModel;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;

public class TaskListPresenter implements Serializable {


   private WeakReference<Contract.MainActivityViewOps> mView;
   private Contract.Model mModel;
   private String TAG = "TaskListPresenter";

//------------------CONSTRUCTOR-------------------////

   public TaskListPresenter(Contract.MainActivityViewOps mView) {
      this.mView = new WeakReference<>(mView);
      getDatabaseModel();
   }

   private void getDatabaseModel() {
      mModel = new DataModel(this, getView().getContext());
   }

   public Contract.MainActivityViewOps getView() throws NullPointerException {
      if (mView != null)
         return mView.get();
      else
         throw new NullPointerException("MainActivityViewOps is unavailable");
   }


   //-------------VIEW OPERATIONS------------------///

   public void showToast(String msg) {
      try {
         getView().showToast(msg);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void updateCurrentView() {
      try {
         getView().updateView();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
     
   }

   public boolean isViewFinishing(){
      try {
         boolean isViewFinishing = getView().isFinishing();
         return isViewFinishing;
      } catch (NullPointerException e) {
         return true;
      }
   }

   public void setUpViews() {
      try {
         getView().setUpViews();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void initRecyclerView(List<LocalTask> tasks) {
      try {
         getView().initRecyclerView(tasks);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void dismissProgressDialog() {
      try {
         getView().dismissProgressDialog();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void showUndoSnackBar(String message, SparseArray map) {
      try {
         getView().showDeleteSnackBar(message, map);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void showProgress(boolean b) {
      try {
         getView().showCircularProgress(b);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void showProgressDialog() {
      try {
         getView().showProgressDialog();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void lockScreenOrientation() {
      try {
         getView().lockScreenOrientation();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void unlockScreenOrientation() {
      try {
         getView().unlockScreenOrientation();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void refresh() {
      try {
         if (isDeviceOnline()) {
            SyncTasks syncTasks = new SyncTasks(getView().getContext(), this, getView().getCredential());
            syncTasks.execute();
         } else {
            showSwipeRefreshProgress(false);
         }
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }

   }

   public void showSwipeRefreshProgress(boolean b) {
      try {
         getView().showSwipeRefreshProgress(b);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }


   //-----------------DATABASE OPERATIONS------------///

   public void closeDatabases() {
      mModel.closeDatabases();
   }


   public List<LocalTask> getTasksFromList(String listId) {
      return mModel.getTasksFromList(listId);
   }

   public List<LocalTask> getTasksFromList(int intId) {
      return mModel.getTasksFromList(intId);
   }

   public List<LocalList> createListsDatabase(List<TaskList> lists) {
      setListsInfo(lists);
      return mModel.createListsDatabase(lists);
   }

   public List<LocalTask> getTasksFromListForAdapter(int listIntId) {
      return mModel.getTaskFromListForAdapter(listIntId);
   }

   public void updateTasksFirstTime(List<LocalTask> tasks) {
      mModel.updateTasksFirstTime(tasks);
   }


   public void saveStringSharedPreference(String key, String value) {
      try {
         getView().saveStringShP(key, value);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public String getStringShP(String key, @Nullable String defaultValue) {
      try {
         return getView().getStringShP(key, defaultValue);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
         return null;
      }
   }

   public void requestApiPermission(Exception mLastError) {
      try {
         getView().requestAuthorization(mLastError);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public String getListTitleByIntId(int listIntId) {
      return mModel.getListTitleFromIntId(listIntId);
   }

   public String getString(int stringId) {
      return getView().getContext().getString(stringId);
   }

   public void deleteTaskFromDatabase(int intId) {
      mModel.deleteTaskFromDatabase(intId);
   }

   public GoogleAccountCredential getCredential() {
      try {
         return getView().getCredential();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
         return null;
      }
   }

   public void updateTaskStatusInServer(LocalTask task, String newStatus) {
      mModel.updateTaskStatusInServer(task, newStatus);
   }

   public boolean isDeviceOnline() {
      try {
         return getView().isDeviceOnline();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
         return false;
      }
   }

   public void navigateToEditTask(Intent i) {
      try {
         getView().navigateToEditTask(i);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void refreshFirstTime() {
      mModel.refreshFirstTime();
   }

   public void addTask(LocalTask task) {
      mModel.addTask(task);
   }

   public void showEmptyRecyclerView(boolean b) {
      try {
         getView().showEmptyRecyclerView(b);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void moveTasks(LinkedHashMap<LocalTask, String> moveMap){
      mModel.moveTasks(moveMap);
   }

   public void editTaskInServer(LocalTask task) {
      mModel.editTaskInServer(task);
   }

   public void showBottomSheet(LocalTask task, int position, boolean b) {
      try {
         getView().showBottomSheet(task, position, b);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void saveBooleanShP(String key, boolean b) {
      try {
         getView().saveBooleanShP(key, b);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public int addTaskToDatabase(LocalTask task) {
      return mModel.addTaskToDatabase(task);
   }

   public void addTaskFirstTimeFromServer(Task task, String listId, int listIntId) {
      mModel.addTaskFirstTimeFromServer(task, listId, listIntId);
   }

   public void addTaskToAdapter(LocalTask localTask) {
      try {
         getView().addTaskToAdapter(localTask);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void updateSyncStatus(LocalTask localTask, int syncedStatus) {
      mModel.updateSyncStatus(localTask, syncedStatus);
   }

   public int updateLocalTask(LocalTask modifiedTask, boolean updateReminder) {
      return mModel.updateLocalTask(modifiedTask, updateReminder);
   }

   public void updateLocalTask(Task task, String listId) {
      mModel.updateLocalTask(task, listId);
   }

   public LocalTask updateNewlyCreatedTask(Task aTask, String listId, int taskIntId) {
      return mModel.updateNewlyCreatedTask(aTask, listId, taskIntId);
   }

   public void updatePosition(Task task) {
      mModel.updatePosition(task);
   }

   public String getTaskIdByIntId(int id) {
      return mModel.getTaskIdByIntId(id);
   }

   public int getIntIdByTaskId(String taskId) {
      return mModel.getIntIdByTaskId(taskId);
   }

   public void updateItem(LocalTask syncedLocalTask) {
      try {
         getView().updateItem(syncedLocalTask);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public List<String> getListsTitles() {
      return mModel.getListsTitles();
   }

   public List<String> getLocalListsIds() {
      return mModel.getListsIds();
   }

   private void setListsInfo(List<TaskList> lists) {
      Co.listIds.clear();
      Co.listTitles.clear();
      for (int i = 0; i < lists.size(); i++) {
         Co.listIds.add(lists.get(i).getId());
         Co.listTitles.add(lists.get(i).getTitle());
      }
   }

   public void addNewListToServer(String listTitle, int listIntId) {
      mModel.addNewListToServer(listTitle, listIntId);
   }

   public int addNewListToDB(String title) {
      return mModel.addListNewToDb(title);
   }

   public void updateListInDBFromLocalList(LocalList localList) {
      mModel.updateListInDBFromLocalList(localList);
   }

   public void changeListNameInDB(int listIntId, String title) {
      mModel.changeListNameInDB(listIntId, title);
   }

   public void changeListNameInServer(String listId, String title) {
      mModel.changeListNameInServer(listId, title);
   }

   public void updateListInDBFromServerList(TaskList list, int intId) {
      mModel.updateListInDBFromServerList(list, intId);
   }

   public void deleteListFromServer(String listId) {
      mModel.deleteListFromServer(listId);
   }

   public int getListsCount(){
      return mModel.getListsCount();
   }

   public void deleteListFromDB(int listIntId){
      mModel.deleteListFromDb(listIntId);
   }

   public List<LocalTask> getAllTasks() {
      return mModel.getLocalTasksFromDB();
   }

   public void deleteTasks(List<LocalTask> tasks) {
      mModel.deleteTasks(tasks);
   }

   public void swipeRefreshSetEnabled(boolean b) {
      try {
         getView().setSwipeRefreshEnabled(b);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void showFab(boolean b) {
      try {
         getView().showFab(b);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void updatePositions(List<Task> tasks) {
      mModel.updatePositions(tasks);
   }


   public void updateExistingTaskFromLocalTask(LocalTask task) {
      mModel.updateExistingTaskFromLocalTask(task);
   }

   public boolean getBooleanShP(String key, boolean defaultValue) {
      return getView().getBooleanShP(key, defaultValue);
   }

   public List<LocalList> getLocalLists() {
      return mModel.getLocalLists();
   }


   public int getListIntIdById(String listId) {
      return mModel.getListIntIdById(listId);
   }

   public String getlistIdByIntId(int listIntId) {
      return mModel.getListIdByIntId(listIntId);
   }

   public void addNewListToDBFromServer(TaskList serverList) {
      mModel.addNewListToDBFromServer(serverList);
   }

   public List<Integer> getListsIntIds() {
      return mModel.getListsIntIds();
   }

   public boolean listExistsInDB(int listIntId) {
      return mModel.listExistsInDB(listIntId);
   }

   public void markListDeleted(int listIntId) {
      mModel.markListDeleted(listIntId);
   }

   public void deleteTasksFromList(int listIntId) {
      mModel.deleteTasksFromList(listIntId);
   }


   public void markTasksDeleted(List<LocalTask> tasksFromList) {
      mModel.markTasksDeleted(tasksFromList);
   }

   public int getTasksNotCompletedFromListCount(int intId) {
      return mModel.getTasksNotCompletedFromListCount(intId);
   }

   public void updateTaskStatusInDb(int intId, String newStatus) {
      mModel.updateTaskStatusInDb(intId, newStatus);
   }

   public void updateTaskCounterForDrawer(int listIntId, MenuItem listItem) {
      getView().updateTaskCounterForDrawer(listIntId, listItem);
   }
}
