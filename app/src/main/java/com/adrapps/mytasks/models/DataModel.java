package com.adrapps.mytasks.models;

import android.content.Context;

import com.adrapps.mytasks.R;
import com.adrapps.mytasks.api_calls.AddTask;
import com.adrapps.mytasks.api_calls.CreateList;
import com.adrapps.mytasks.api_calls.DeleteList;
import com.adrapps.mytasks.api_calls.DeleteTask;
import com.adrapps.mytasks.api_calls.EditList;
import com.adrapps.mytasks.api_calls.EditTask;
import com.adrapps.mytasks.api_calls.FirstRefreshAsync;
import com.adrapps.mytasks.api_calls.MoveTask;
import com.adrapps.mytasks.api_calls.UpdateStatus;
import com.adrapps.mytasks.databases.ListsDatabase;
import com.adrapps.mytasks.databases.TasksDatabase;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.helpers.AlarmHelper;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.LinkedHashMap;
import java.util.List;


public class DataModel implements Contract.Model {


   private TaskListPresenter mPresenter;
   private TasksDatabase tasksDb;
   private ListsDatabase listsDb;
   private Context context;

   public DataModel(TaskListPresenter presenter, Context context) {
      this.mPresenter = presenter;
      tasksDb = TasksDatabase.getInstance(context.getApplicationContext());
      listsDb = ListsDatabase.getInstance(context.getApplicationContext());
      this.context = context.getApplicationContext();
   }

   public DataModel(Context context) {
      tasksDb = TasksDatabase.getInstance(context.getApplicationContext());
      listsDb = ListsDatabase.getInstance(context.getApplicationContext());
   }



   //-----------------DATABASE OPERATIONS-----------------//

   @Override
   public void closeDatabases() {
      tasksDb.close();
      listsDb.close();
   }


   @Override
   public List<LocalTask> getTasksFromList(String listId) {
      return tasksDb.getTasksFromList(listId);
   }

   @Override
   public List<LocalTask> getTasksFromList(int listIntId) {
      return tasksDb.getTasksFromList(listIntId);
   }

   @Override
   public void updateTasksFirstTime(List<LocalTask> tasks) {
      tasksDb.updateTasksFirstTime(tasks);
   }


   @Override
   public List<LocalList> createListsDatabase(List<TaskList> lists) {
      return listsDb.createListDatabase(lists);
   }


   @Override
   public void markTasksDeleted(List<LocalTask> tasksFromList) {
      tasksDb.markTasksDeleted(tasksFromList);
   }

   @Override
   public int getTasksNotCompletedFromListCount(int intId) {
      return tasksDb.getTasksNotCompletedFromList(intId);
   }

   @Override
   public void updateTaskStatusInDb(int intId, String newStatus) {
      tasksDb.updateTaskStatus(intId, newStatus);
   }

   @Override
   public LocalTask getTask(int taskIntId) {
      return tasksDb.getTask(taskIntId);
   }

   @Override
   public void updateTaskParentInDb(LocalTask task, String parent) {
      tasksDb.updateTaskParent(task, parent);
   }

   @Override
   public List<String> getListsTitles() {
      return listsDb.getListsTitles();
   }

   @Override
   public List<String> getListsIds() {
      try {
         return listsDb.getListsIds();
      } catch (Exception e) {
         e.printStackTrace();
         listsDb = ListsDatabase.getInstance(context.getApplicationContext());
      }
      return listsDb.getListsIds();
   }

   @Override
   public String getListTitleFromIntId(int listIntId) {
      return listsDb.getListTitleFromIntId(listIntId);
   }

   @Override
   public boolean listExistsInDB(int listIntId) {
      return listsDb.listExists(listIntId);
   }

   @Override
   public int addTaskToDatabase(LocalTask task) {
      return tasksDb.addTaskToDataBase(task);
   }

   @Override
   public void addTaskFirstTimeFromServer(Task task, String listId, int listIntId) {
      tasksDb.addTaskFirstTimeFromServer(task, listId, listIntId);
   }


   @Override
   public long updateReminder(String taskId, long reminder) {
      return tasksDb.updateTaskReminder(taskId, reminder);
   }

   @Override
   public long updateReminder(int intId, long reminder, int repeatMode) {
      return tasksDb.updateTaskReminder(intId, reminder, repeatMode);
   }


//   @Override
//   public void updateStatus(LocalTask localTask, int newStatus) {
//      tasksDb.updateSyncStatus(localTask, newStatus);
//   }

   @Override
   public List<LocalTask> getLocalTasks() {
      return tasksDb.getLocalTasks();
   }

   @Override
   public int updateLocalTask(LocalTask modifiedTask, boolean updateReminders) {
      return tasksDb.updateLocalTask(modifiedTask, updateReminders);
   }

   @Override
   public List<LocalTask> getTaskFromListForAdapter(int listIntId) {
      return tasksDb.getTasksFromListForAdapter(listIntId);
   }

   @Override
   public void updateLocalTask(Task task, String listId) {
      tasksDb.updateExistingTaskFromServerTask(task, listId);
   }

   @Override
   public LocalTask updateNewlyCreatedTask(Task aTask, String listId, int taskIntId) {
      return tasksDb.updateNewlyCreatedTask(aTask, listId, taskIntId);
   }

   @Override
   public void updatePosition(Task task) {
      tasksDb.updatePosition(task);
   }

   @Override
   public void updatePositions(List<Task> tasks) {
      tasksDb.updatePositions(tasks);
   }

   @Override
   public void updateExistingTaskFromLocalTask(LocalTask task) {
      tasksDb.updateExistingTaskFromLocalTask(task);
   }

   @Override
   public List<LocalList> getLocalLists() {
      return listsDb.getLocalLists();
   }

   @Override
   public int getListIntIdById(String listId) {
      return listsDb.getListIntIdById(listId);
   }

   @Override
   public String getListIdByIntId(int listIntId) {
      return listsDb.getListIdByIntId(listIntId);
   }

   @Override
   public void addNewListToDBFromServer(TaskList serverList) {
      listsDb.addListFromServer(serverList);
   }

   @Override
   public List<Integer> getListsIntIds() {
      return listsDb.getListsIntIds();
   }

   @Override
   public void markListDeleted(int listIntId) {
      if (listsDb.getListsCount() <= 1) {
         mPresenter.showToast(context.getString(R.string.default_delete_list_message));
         return;
      }
      listsDb.markListDeleted(listIntId);
   }

   @Override
   public void deleteTasksFromList(int listIntId) {
      tasksDb.deleteTaskFromList(listIntId);
   }

   @Override
   public String getTaskIdByIntId(int id) {
      return tasksDb.getTaskIdByIntId(id);
   }

   @Override
   public void deleteTaskFromDatabase(int intId) {
      tasksDb.deleteTask(intId);
   }

   @Override
   public int getIntIdByTaskId(String taskId) {
      return tasksDb.getIntIdByTaskId(taskId);
   }

   @Override
   public void addNewListToServer(String listTitle, int listIntId) {

      if (mPresenter.isDeviceOnline()) {
         LocalList list = new LocalList();
         list.setTitle(listTitle);
         list.setIntId(listIntId);
         GoogleAccountCredential credential = mPresenter.getCredential();
         CreateList createList = new CreateList(context, mPresenter, credential);
         createList.execute(list);
      }
   }

   @Override
   public void updateListInDBFromLocalList(LocalList localList) {
      listsDb.updateListFromLocalList(localList);
   }

   @Override
   public void changeListNameInDB(int listIntId, String title) {
      listsDb.editListTitle(listIntId, title);
   }

   @Override
   public void changeListNameInServer(String listId, String title) {
      if (mPresenter.isDeviceOnline()) {
         GoogleAccountCredential credential = mPresenter.getCredential();
         EditList editList = new EditList(context, mPresenter, credential);
         editList.execute(listId, title);
      }
   }

   @Override
   public void updateListInDBFromServerList(TaskList list, int intId) {
      listsDb.updateListInDBFromServerList(list, intId);
   }

   @Override
   public int getListsCount() {
      return listsDb.getListsCount();
   }

   @Override
   public void deleteListFromServer(String listId) {
      if (mPresenter.isDeviceOnline()) {
         GoogleAccountCredential credential = mPresenter.getCredential();
         DeleteList deleteList = new DeleteList(context, mPresenter, credential);
         deleteList.execute(listId);
      }
   }

   @Override
   public void deleteListFromDb(int listIntId) {
      if (listsDb.getListsCount() <= 1) {
         mPresenter.showToast(context.getString(R.string.default_delete_list_message));
         return;
      }
      listsDb.deleteList(listIntId);
   }

   @Override
   public int addListNewToDb(String listTitle) {
      return listsDb.addListFirstTime(listTitle);
   }


   //------------------------API OPERATIONS----------------------///

   @Override
   public void deleteTasks(List<LocalTask> tasks) {
      for (int i = 0; i < tasks.size(); i++) {
         LocalTask currentTask = tasks.get(i);
         String currentTaskId = currentTask.getId();
         AlarmHelper.cancelTaskReminder(tasks.get(i), context);
         AlarmHelper.cancelDefaultRemindersForTasks(context, tasks);
         if (currentTaskId == null || currentTaskId.trim().isEmpty()) {
            tasks.remove(i);
            deleteTaskFromDatabase(currentTask.getIntId());
         } else {
            tasksDb.markDeleted(currentTaskId);
         }
         if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            DeleteTask remove = new DeleteTask(context, mPresenter, credential, tasks);
            remove.execute();
         }
      }
   }


   @Override
   public void updateTaskStatusInServer(LocalTask task, String newStatus) {
      if (mPresenter != null) {
         if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            UpdateStatus update = new UpdateStatus(context, mPresenter, credential);
            update.execute(task);
         }
      }

   }


   @Override
   public void refreshFirstTime() {
      if (mPresenter.isDeviceOnline()) {
         FirstRefreshAsync firstRefresh = new FirstRefreshAsync(context, mPresenter, mPresenter.getCredential());
         firstRefresh.execute();
      } else {
         mPresenter.showSwipeRefreshProgress(false);
      }
   }

   @Override
   public void addTask(LocalTask task) {
      if (mPresenter.isDeviceOnline()) {
         AddTask add = new AddTask(context, mPresenter,
               mPresenter.getCredential());
         add.execute(task);
      }
   }

   @Override
   public void moveTasks(LinkedHashMap<LocalTask, String> moveMap) {
      if (mPresenter.isDeviceOnline()) {
         MoveTask move = new MoveTask(context, mPresenter, mPresenter.getCredential(), moveMap);
         move.execute();
      }
   }

   @Override
   public void editTaskInServer(LocalTask task) {
//      if (task.getSyncStatus() == Co.SYNCED) {
//         task.setSyncStatus(Co.EDITED_NOT_SYNCED);
//      }
//      mPresenter.updateLocalTask(task, true);
      if (mPresenter.isDeviceOnline()) {
         EditTask edit = new EditTask(context, mPresenter, mPresenter.getCredential(),
               mPresenter.getStringShP(Co.CURRENT_LIST_ID, null));
         edit.execute(task);
      }
   }


}
