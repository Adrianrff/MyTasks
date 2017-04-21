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
    public void addTaskToLocalDatabase(LocalTask task) {
        tasksDb.addTask(task);
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
    public void updateSyncStatus(int synced, String taskId) {
        tasksDb.updateSyncStatus(synced, taskId);
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
        return tasksDb.getTask(id);
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
    public List<LocalTask> getTaskFromLlistForAdapter(String listId) {
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
    public void updateNewlyCreatedTask(Task aTask, String listId, String intId) {
        tasksDb.updateNewLyCreatedTask(aTask, listId, intId);
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


    //------------------------API OPERATIONS----------------------///
    @Override
    public void deleteTask(String taskId, String listId) {
        if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            DeleteTask remove = new DeleteTask(mPresenter, credential, listId);
            remove.execute(taskId);
        } else {
            tasksDb.markDeleted(taskId);
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }
    }

    @Override
    public void updateTaskStatus(String taskId, String listId, String newStatus) {
        if (mPresenter.isDeviceOnline()) {
            tasksDb.updateTaskStatus(taskId,newStatus);
            GoogleAccountCredential credential = mPresenter.getCredential();
            UpdateStatus update = new UpdateStatus(mPresenter, credential);
            update.execute(taskId,listId,newStatus);
        }
        else
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));

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
    public void addTask(LocalTask task) {
        mPresenter.addTaskToAdapter(task);
        if (mPresenter.isDeviceOnline()) {
            AddTask add = new AddTask(mPresenter,
                    mPresenter.getCredential(), mPresenter.getStringShP(Co.CURRENT_LIST_ID));
            add.execute(task);
        }
        else {
            task.setLocalModify();
            task.setSyncStatus(Co.NOT_SYNCED);
            mPresenter.addTaskToDatabase(task);
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }
    }

    @Override
    public void moveTask(String[] params) {
        mPresenter.updateSibling(params[0], params[2]);
        if (mPresenter.isDeviceOnline()) {
            MoveTask move = new MoveTask(mPresenter, mPresenter.getCredential());
            move.execute(params);
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
        }else {
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }
    }


}
