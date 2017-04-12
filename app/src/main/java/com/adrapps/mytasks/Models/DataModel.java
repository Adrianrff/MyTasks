package com.adrapps.mytasks.Models;

import android.content.Context;

import com.adrapps.mytasks.APICalls.RemoveTask;
import com.adrapps.mytasks.APICalls.UpdateStatus;
import com.adrapps.mytasks.Databases.ListsDatabase;
import com.adrapps.mytasks.Databases.TasksDataBase;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
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
        return tasksDb.getTasksFromLlist(listId);
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
    public void addTaskToLocalDatabase(Task task, String listId) {
        tasksDb.addTaskToLocalDatabase(task,listId);
    }

    @Override
    public int deleteTask(String taskId) {
        return tasksDb.deleteTask(taskId);
    }

    @Override
    public void deleteTaskFromApi(String taskId, String listId) {
        GoogleAccountCredential credential = mPresenter.getCredential();
        RemoveTask remove = new RemoveTask(mPresenter,credential,listId);
        remove.execute(taskId);
    }

    @Override
    public void updateTask(String taskId, String listId, String newStatus) {
        tasksDb.updateTaskStatus(taskId,newStatus);
        GoogleAccountCredential credential = mPresenter.getCredential();
        UpdateStatus update = new UpdateStatus(mPresenter, credential);
        update.execute(taskId,listId,newStatus);
    }
}
