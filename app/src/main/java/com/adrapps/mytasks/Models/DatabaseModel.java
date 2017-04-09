package com.adrapps.mytasks.Models;

import android.content.Context;

import com.adrapps.mytasks.Databases.ListsDatabase;
import com.adrapps.mytasks.Databases.TasksDataBase;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Presenter.TaskListPresenter;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;

public class DatabaseModel implements Contract.Model {

    private TaskListPresenter mPresenter;
    private TasksDataBase tasksDb;
    private ListsDatabase listsDb;

    public DatabaseModel(TaskListPresenter presenter, Context context) {
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
}
