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

import java.util.HashMap;
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
   public void updateLists(List<TaskList> lists) {
      listsDb.updateLists(lists);
   }

   @Override
   public void updateTasksFirstTime(List<LocalTask> tasks) {
      tasksDb.updateTasksFirstTime(tasks);
   }

   @Override
   public List<LocalTask> getTasksFromList(String listId) {
      return tasksDb.getTasksFromList(listId);
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
   public String getListTitleFromId(String listId) {
      return listsDb.getListTitleFromId(listId);
   }

   @Override
   public int addTaskToLocalDatabase(LocalTask task) {
      return (int) tasksDb.addTaskToDataBase(task);
   }

   @Override
   public void addTaskFirstTimeFromServer(Task task, String listId) {
      tasksDb.addTaskFirstTimeFromServer(task, listId);
   }


   @Override
   public long updateReminder(String taskId, long reminder) {
      return tasksDb.updateTaskReminder(taskId, reminder);
   }

   @Override
   public long updateReminder(int intId, long reminder, int repeatMode) {
      return tasksDb.updateTaskReminder(intId, reminder, repeatMode);
   }


   @Override
   public void updateSyncStatus(int synced, int intId) {
      tasksDb.updateSyncStatus(synced, intId);
   }

   @Override
   public List<LocalTask> getLocalTasksFromDB() {
      return tasksDb.getLocalTasks();
   }

   @Override
   public int updateLocalTask(LocalTask modifiedTask, boolean updateReminders) {
      return tasksDb.updateLocalTask(modifiedTask, updateReminders);
   }

   @Override
   public List<LocalTask> getTaskFromListForAdapter(String listId) {
      return tasksDb.getTasksFromListForAdapter(listId);
   }

   @Override
   public void updateLocalTask(Task task, String listId) {
      tasksDb.updateExistingTaskFromServerTask(task, listId);
   }

   @Override
   public LocalTask updateNewlyCreatedTask(Task aTask, String listId, String intId) {
      return tasksDb.updateNewlyCreatedTask(aTask, listId, intId);
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
   public void updateExistingTaskFromLocalTask(LocalTask task, String listId) {
      tasksDb.updateExistingTaskFromLocalTask(task, listId);
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
   public void addList(String listTitle) {
      LocalList list = new LocalList();
      list.setTitle(listTitle);
      list.setIntId(addListToDb(listTitle));
      if (mPresenter.isDeviceOnline()) {
         GoogleAccountCredential credential = mPresenter.getCredential();
         CreateList createList = new CreateList(context, mPresenter, credential);
         createList.execute(list);
      }
   }

   @Override
   public void updateList(LocalList localList) {
      listsDb.updateList(localList);
   }

   @Override
   public void editList(String listId, String title) {
      listsDb.editListTitle(listId, title);
      if (mPresenter.isDeviceOnline()) {
         GoogleAccountCredential credential = mPresenter.getCredential();
         EditList editList = new EditList(context, mPresenter, credential);
         editList.execute(listId, title);
      }

   }

   @Override
   public void updateList(TaskList list) {
      listsDb.updateList(list);
   }

   @Override
   public void deleteList(String listId) {
      if (listsDb.getListsCount() <= 1) {
         mPresenter.showToast(context.getString(R.string.default_delete_list_message));
         return;
      }
      listsDb.deleteList(listId);
      if (mPresenter.isDeviceOnline()) {
         GoogleAccountCredential credential = mPresenter.getCredential();
         DeleteList deleteList = new DeleteList(context, mPresenter, credential);
         deleteList.execute(listId);
      }
   }

   @Override
   public void updateNewTasksInBulk(HashMap<Task, LocalTask> map) {
      tasksDb.updateNewTasksInBulk(map);
   }

   @Override
   public int addListToDb(String listTitle) {
      return listsDb.addList(listTitle);
   }


   //------------------------API OPERATIONS----------------------///

   @Override
   public void deleteTasks(List<LocalTask> tasks) {
      for (int i = 0; i < tasks.size(); i++) {
         LocalTask currentTask = tasks.get(i);
         String currentTaskId = currentTask.getId();
         AlarmHelper.cancelTaskReminder(tasks.get(i), context);
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
   public void updateTaskStatus(int intId, String listId, String newStatus) {
      tasksDb.updateTaskStatus(intId, newStatus);
      if (tasksDb.getTask(intId).getSyncStatus() != 0) {
         tasksDb.updateSyncStatus(Co.EDITED_NOT_SYNCED, intId);
      }
      if (mPresenter != null) {
         if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            UpdateStatus update = new UpdateStatus(context, mPresenter, credential);
            update.execute(mPresenter.getTaskIdByIntId(intId), listId, newStatus);
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
   public int addTask(LocalTask task) {
      int newTaskId = mPresenter.addTaskToDatabase(task);
      task.setIntId(newTaskId);
      if (mPresenter.isDeviceOnline()) {
         AddTask add = new AddTask(context, mPresenter,
               mPresenter.getCredential());
         add.execute(task);
      }
      mPresenter.addTaskToAdapter(task);
      return newTaskId;
   }

//   @Override
//   public void moveTask(LocalTask movedTask, String previousTaskId) {
//      if (mPresenter.isDeviceOnline()) {
//         MoveTask move = new MoveTask(context, mPresenter, mPresenter.getCredential());
//         move.execute(movedTask.getId(), movedTask.getList(), previousTaskId);
//      }
//   }

   @Override
   public void moveTasks(LinkedHashMap<LocalTask, String> moveMap) {
      if (mPresenter.isDeviceOnline()) {
         MoveTask move = new MoveTask(context, mPresenter, mPresenter.getCredential(), moveMap);
         move.execute();
      }
   }

   @Override
   public void editTask(LocalTask task) {
      if (task.getSyncStatus() != Co.NOT_SYNCED && task.getSyncStatus() != Co.EDITED_NOT_SYNCED) {
         task.setSyncStatus(Co.EDITED_NOT_SYNCED);
      }
      mPresenter.updateLocalTask(task, true);
      if (mPresenter.isDeviceOnline()) {
         EditTask edit = new EditTask(context, mPresenter, mPresenter.getCredential(),
               mPresenter.getStringShP(Co.CURRENT_LIST_ID, null));
         edit.execute(task);
      }
   }


}
