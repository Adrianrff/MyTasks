package com.adrapps.mytasks.Models;

import android.content.Context;

import com.adrapps.mytasks.APICalls.AddTask;
import com.adrapps.mytasks.APICalls.EditTask;
import com.adrapps.mytasks.APICalls.FirstRefreshAsync;
import com.adrapps.mytasks.APICalls.DeleteTask;
import com.adrapps.mytasks.APICalls.MoveTask;
import com.adrapps.mytasks.APICalls.UpdateStatus;
import com.adrapps.mytasks.Databases.ListsDatabase;
import com.adrapps.mytasks.Databases.TasksDataBase;
import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.adrapps.mytasks.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;

public class DataModel implements Contract.Model {


    private TaskListPresenter mPresenter;
    private TasksDataBase tasksDb;
    private ListsDatabase listsDb;

    public DataModel(TaskListPresenter presenter, Context context) {
        this.mPresenter = presenter;
        tasksDb = new TasksDataBase(context);
        listsDb = new ListsDatabase(context);
    }

    //-----------------DATABASE OPERATIONS-----------------//

    @Override
    public void updateLists(List<TaskList> lists) {
        listsDb.updateLists(lists);
    }

    @Override
    public void updateTasks(List<LocalTask> tasks) {
        tasksDb.updateTasks(tasks);
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
        return listsDb.getListsIds();
    }

    @Override
    public String getListTitleFromId(String listId) {
        return listsDb.getListTitleFromId(listId);
    }

    @Override
    public int addTaskToLocalDatabase(LocalTask task) {
        return (int) tasksDb.addTask(task);
    }

    @Override
    public void addTaskFirstTimeFromServer(Task task, String listId) {
        tasksDb.addTaskFirstTimeFromServer(task, listId);
    }

    @Override
    public int deleteTask(String taskId) {
        return tasksDb.deleteTask(taskId);
    }

    @Override
    public long getTaskReminder(String taskId) {
        return tasksDb.getTaskReminder(taskId);
    }

    @Override
    public boolean taskExistsInDB(String taskId) {
        return tasksDb.taskExistsInDB(taskId);
    }

    @Override
    public long updateReminder(String taskId, long reminder) {
        return tasksDb.updateTaskReminder(taskId, reminder);
    }

    @Override
    public void updateSyncStatus(int synced, int intId) {
        tasksDb.updateSyncStatus(synced, intId);
    }

    @Override
    public long getTaskReminderId(String taskId) {
        return tasksDb.getTaskReminderId(taskId);
    }

    @Override
    public List<LocalTask> getLocalTasksFromDB() {
        return tasksDb.getLocalTasks();
    }

    @Override
    public LocalTask getTask(String id) {
        return tasksDb.getTaskByTaskId(id);
    }

    @Override
    public int updateLocalTask(LocalTask modifiedTask) {
        return tasksDb.updateLocalTask(modifiedTask);
    }

    @Override
    public void updateSibling(String taskId, String previousTaskId) {
        tasksDb.updateLocalSibling(taskId,previousTaskId);
    }

    @Override
    public List<LocalTask> getTaskFromListForAdapter(String listId) {
        return tasksDb.getTasksFromListForAdapter(listId);
    }

    @Override
    public void updateLocalTask(Task task, String listId) {
        tasksDb.updateTask(task, listId);
    }

    @Override
    public void markDeleted(String taskId) {
        tasksDb.markDeleted(taskId);
    }

    @Override
    public LocalTask updateNewlyCreatedTask(Task aTask, String listId, String intId) {
        return tasksDb.updateNewlyCreatedTask(aTask, listId, intId);
    }

    @Override
    public void setTemporaryPosition(String taskId, String newTaskTempPos) {
        tasksDb.setTemporaryPosition(taskId, newTaskTempPos);
    }

    @Override
    public void updateMoved(String id, int moved) {
        tasksDb.updateMoved(moved, id);
    }

    @Override
    public void updatePosition(Task task) {
        tasksDb.updatePosition(task);
    }

    @Override
    public void updateSiblingByIntId(int id, int sibling) {
        tasksDb.updateSibling(id, sibling);
    }

    @Override
    public String getTaskIdByIntId(int id) {
        return tasksDb.getTaskIdByIntId(id);
    }

    @Override
    public void updateMovedByIntId(int intId, int moved) {
        tasksDb.updateMovedByIntId(intId, moved);
    }

    @Override
    public void setTemporaryPositionByIntId(int intId, String newTaskTempPos) {
        tasksDb.setTemporaryPositionByIntId(intId, newTaskTempPos);
    }

    @Override
    public void deleteTaskFromDataBase(int intId) {
        tasksDb.deleteTask(intId);
    }

    @Override
    public int getIntIdByTaskId(String taskId) {
        return tasksDb.getIntIdByTaskId(taskId);
    }


    //------------------------API OPERATIONS----------------------///
    @Override
    public void deleteTask(String intId, String listId) {
        if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            DeleteTask remove = new DeleteTask(mPresenter, credential, listId);
            remove.execute(intId);
        } else {
            tasksDb.markDeleted(intId);
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }
    }

    @Override
    public void updateTaskStatus(int intId, String listId, String newStatus) {
        tasksDb.updateTaskStatus(intId,newStatus);
        tasksDb.updateSyncStatus(Co.EDITED_NOT_SYNCED,intId);
        if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            UpdateStatus update = new UpdateStatus(mPresenter, credential);
            update.execute(mPresenter.getTaskIdByIntId(intId),listId,newStatus);
        }
        else {
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }

    }

    @Override
    public void refreshFirstTime() {
        if (mPresenter.isDeviceOnline()) {
            FirstRefreshAsync firstRefresh = new FirstRefreshAsync(mPresenter, mPresenter.getCredential());
            firstRefresh.execute();
        }
        else {
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
            mPresenter.showSwipeRefreshProgress(false);
        }
    }

    @Override
    public int addTask(LocalTask task) {
        int newTaskId = mPresenter.addTaskToDatabase(task);
        if (mPresenter.isDeviceOnline()) {
            AddTask add = new AddTask(mPresenter,
                    mPresenter.getCredential(), mPresenter.getStringShP(Co.CURRENT_LIST_ID));
            add.execute(task);
        }
        else {
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }
        task.setIntId(newTaskId);
        mPresenter.addTaskToAdapter(task);
        return newTaskId;
    }

    @Override
    public void moveTask(LocalTask movedTask, String previousTaskId) {
        if (mPresenter.isDeviceOnline()) {
            MoveTask move = new MoveTask(mPresenter, mPresenter.getCredential());
            move.execute(movedTask.getId(), movedTask.getTaskList(), previousTaskId);
        } else {
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }
    }

    @Override
    public void editTask(LocalTask task) {
        mPresenter.updateLocalTask(task);
        task.setSyncStatus(Co.EDITED_NOT_SYNCED);
        if (mPresenter.isDeviceOnline()) {
            EditTask edit = new EditTask(mPresenter, mPresenter.getCredential(), mPresenter.getStringShP(Co.CURRENT_LIST_ID));
            edit.execute(task);
        }
    }


}
