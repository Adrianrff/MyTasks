package com.adrapps.mytasks.models;

import android.content.Context;

import com.adrapps.mytasks.api_calls.AddTask;
import com.adrapps.mytasks.api_calls.CreateList;
import com.adrapps.mytasks.api_calls.DeleteList;
import com.adrapps.mytasks.api_calls.EditList;
import com.adrapps.mytasks.api_calls.EditTask;
import com.adrapps.mytasks.api_calls.FirstRefreshAsync;
import com.adrapps.mytasks.api_calls.DeleteTask;
import com.adrapps.mytasks.api_calls.MoveTask;
import com.adrapps.mytasks.api_calls.UpdateStatus;
import com.adrapps.mytasks.databases.ListsDatabase;
import com.adrapps.mytasks.databases.TasksDataBase;
import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.interfaces.Contract;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.presenter.TaskListPresenter;
import com.adrapps.mytasks.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;


public class DataModel implements Contract.Model {


    private TaskListPresenter mPresenter;
    private TasksDataBase tasksDb;
    private ListsDatabase listsDb;
    private Context context;

    public DataModel(TaskListPresenter presenter, Context context) {
        this.mPresenter = presenter;
        tasksDb = new TasksDataBase(context);
        listsDb = new ListsDatabase(context);
        this.context = context;
    }

    public DataModel(Context context) {
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
        return (int) tasksDb.addTaskToDataBase(task);
    }

    @Override
    public void addTaskFirstTimeFromServer(Task task, String listId) {
        tasksDb.addTaskFirstTimeFromServer(task, listId);
    }

    @Override
    public int deleteTaskFromDatabase(String taskId) {
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
    public long updateReminder(int intId, long reminder) {
        return tasksDb.updateTaskReminder(intId, reminder);
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

    @Override
    public void addList(String listTitle) {
        LocalList list = new LocalList();
        list.setTitle(listTitle);
        list.setIntId(addListToDb(listTitle));
        if (mPresenter.isDeviceOnline()){
            GoogleAccountCredential credential = mPresenter.getCredential();
            CreateList createList = new CreateList(context, mPresenter, credential);
            createList.execute(list);
        }
    }

    @Override
    public void addList(TaskList list) {
        listsDb.addList(list);
    }

    @Override
    public void updateList(LocalList localList) {
        listsDb.updateList(localList);
    }

    @Override
    public void editList(String listId, String title) {
        listsDb.editListTitle(listId, title);
        if (mPresenter.isDeviceOnline()){
            GoogleAccountCredential credential = mPresenter.getCredential();
            EditList editList = new EditList(context, mPresenter, credential);
            editList.execute(listId, title);
        } else {
//            listsDb.updateListSyncStatus(Co.EDITED_NOT_SYNCED);
        }

    }

    @Override
    public void updateList(TaskList list) {
        listsDb.updateList(list);
    }

    @Override
    public void deleteList(String listId) {
        if (listsDb.getListsCount() <= 1){
            mPresenter.showToast(context.getString(R.string.default_delete_list_message));
            return;
        }
        listsDb.deleteList(listId);
        if (mPresenter.isDeviceOnline()){
            GoogleAccountCredential credential = mPresenter.getCredential();
            DeleteList deleteList = new DeleteList(context, mPresenter, credential);
            deleteList.execute(listId);
        }
    }

    @Override
    public List<LocalList> getLocalLists() {
        return listsDb.getLocalLists();
    }

    @Override
    public LocalTask getTask(int intId) {
        return tasksDb.getTask(intId);
    }

    @Override
    public int getTaskReminderRepeatModeByIntId(int intId) {
        return tasksDb.getTaskReminderRepeatModeByIntId(intId);
    }

    @Override
    public int getTaskReminderRepeatMode(String taskId) {
        return tasksDb.getTaskReminderRepeatMode(taskId);
    }

    @Override
    public long getTaskReminderByIntId(int intId) {
        return tasksDb.getTaskReminderByIntId(intId);
    }

    @Override
    public void deleteListFromDB(int intId){
        listsDb.deleteList(intId);
    }

    @Override
    public int addListToDb(String listTitle) {
        return listsDb.addList(listTitle);
    }


    //------------------------API OPERATIONS----------------------///
    @Override
    public void deleteTask(String taskId, String listId) {
        tasksDb.markDeleted(taskId);
        if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            DeleteTask remove = new DeleteTask(context, mPresenter, credential, listId);
            remove.execute(taskId);
        }
    }

    @Override
    public void updateTaskStatus(int intId, String listId, String newStatus) {
        tasksDb.updateTaskStatus(intId,newStatus);
        tasksDb.updateSyncStatus(Co.EDITED_NOT_SYNCED,intId);
        if (mPresenter.isDeviceOnline()) {
            GoogleAccountCredential credential = mPresenter.getCredential();
            UpdateStatus update = new UpdateStatus(context, mPresenter, credential);
            update.execute(mPresenter.getTaskIdByIntId(intId),listId,newStatus);
        }

    }

    @Override
    public void refreshFirstTime() {
        if (mPresenter.isDeviceOnline()) {
            FirstRefreshAsync firstRefresh = new FirstRefreshAsync(context, mPresenter, mPresenter.getCredential());
            firstRefresh.execute();
        }
        else {
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

    @Override
    public void moveTask(LocalTask movedTask, String previousTaskId) {
        if (mPresenter.isDeviceOnline()) {
            MoveTask move = new MoveTask(context, mPresenter, mPresenter.getCredential());
            move.execute(movedTask.getId(), movedTask.getList(), previousTaskId);
        } else {
            mPresenter.showToast(mPresenter.getString(R.string.no_internet_toast));
        }
    }

    @Override
    public void editTask(LocalTask task) {
        task.setSyncStatus(Co.EDITED_NOT_SYNCED);
        mPresenter.updateLocalTask(task);
        if (mPresenter.isDeviceOnline()) {
            EditTask edit = new EditTask(context, mPresenter, mPresenter.getCredential(), mPresenter.getStringShP(Co.CURRENT_LIST_ID));
            edit.execute(task);
        }
    }


}
