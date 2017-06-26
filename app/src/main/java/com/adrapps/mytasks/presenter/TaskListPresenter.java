package com.adrapps.mytasks.presenter;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

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
import java.util.HashMap;
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
         getView().updateCurrentView();
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
     
   }

   public boolean isViewDestroyed(){
      try {
         boolean isViewDestroyed = getView().isDestroyed();
         return isViewDestroyed;
      } catch (NullPointerException e) {
         Log.d(TAG, "isViewDestroyed: View was null");
         return true;
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

   public void updateLists(List<TaskList> lists) {
      setListsInfo(lists);
      mModel.updateLists(lists);
   }

   public List<LocalTask> getTasksFromListForAdapter(String listId) {
      return mModel.getTaskFromListForAdapter(listId);
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

   public String getListTitleById(String listId) {
      return mModel.getListTitleFromId(listId);
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

   public void updateTaskStatusInServer(int intId, String listId, String newStatus) {
      mModel.updateTaskStatusInServer(intId, listId, newStatus);
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

   public int addTask(LocalTask task) {
      return mModel.addTask(task);
   }

   public void updateNewTasksInBulk(HashMap<Task, LocalTask> map) {
      mModel.updateNewTasksInBulk(map);
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

   public void editTask(LocalTask task) {
      mModel.editTask(task);
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
      return mModel.addTaskToLocalDatabase(task);
   }

   public void addTaskFirstTimeFromServer(Task task, String listId) {
      mModel.addTaskFirstTimeFromServer(task, listId);
   }

   public void addTaskToAdapter(LocalTask localTask) {
      try {
         getView().addTaskToAdapter(localTask);
      } catch (NullPointerException e) {
         Log.d(TAG, "Presenter method: view was null");
      }
   }

   public void updateSyncStatus(int intId, int synced) {
      mModel.updateSyncStatus(synced, intId);
   }

   public int updateLocalTask(LocalTask modifiedTask, boolean updateReminder) {
      return mModel.updateLocalTask(modifiedTask, updateReminder);
   }

   public void updateLocalTask(Task task, String listId) {
      mModel.updateLocalTask(task, listId);
   }

   public LocalTask updateNewlyCreatedTask(Task aTask, String listId, String intId) {
      return mModel.updateNewlyCreatedTask(aTask, listId, intId);
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

   public List<String> getListsIds() {
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

   public void addList(String listTitle) {
      mModel.addList(listTitle);
   }

   public void updateListInDBFromLocalListAfterServerOp(LocalList localList) {
      mModel.updateListInDBFromLocalListAfterServerOp(localList);
   }

   public void editList(String listId, String title) {
      mModel.editList(listId, title);
   }

   public void updateListInDBFromServerList(TaskList list) {
      mModel.updateList(list);
   }

   public void deleteList(String listId) {
      mModel.deleteList(listId);
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


   public void updateExistingTaskFromLocalTask(LocalTask task, String listId) {
      mModel.updateExistingTaskFromLocalTask(task, listId);
   }

   public boolean getBooleanShP(String key, boolean defaultValue) {
      return getView().getBooleanShP(key, defaultValue);
   }
}
