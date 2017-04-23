package com.adrapps.mytasks.Presenter;

import android.content.Intent;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Models.DataModel;
import com.adrapps.mytasks.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.lang.ref.WeakReference;
import java.util.List;

public class TaskListPresenter {


    private WeakReference<Contract.MainActivityViewOps> mView;
    private Contract.Model mModel;

//------------------CONSTRUCTOR-------------------////

    public TaskListPresenter(Contract.MainActivityViewOps mView) {
        this.mView = new WeakReference<>(mView);
        getDatabaseModel();
    }

    private void getDatabaseModel() {
        mModel = new DataModel(this, getView().getContext());
    }

    private Contract.MainActivityViewOps getView() throws NullPointerException {
        if (mView != null)
            return mView.get();
        else
            throw new NullPointerException("MainActivityViewOps is unavailable");
    }



    //-------------VIEW OPERATIONS------------------///

    public void showToast(String msg) {
        getView().showToast(msg);
    }

    public void updateCurrentView() {
        getView().updateCurrentView();
    }
    public void setUpViews() {
        getView().setUpViews();
    }
    public void initRecyclerView(List<LocalTask> tasks) {
        getView().initRecyclerView(tasks);
    }
    public void setToolbarTitle(String title) {
        getView().setToolbarTitle(title);
    }
    public void dismissProgressDialog() {
        getView().dismissProgressDialog();
    }

    public void showUndoSnackBar(String message, int position, LocalTask removedTask) {
        getView().showUndoSnackBar(message, position, removedTask);
    }
    public void showProgress(boolean b) {
        getView().showCircularProgress(b);
    }
    public void showProgressDialog() {
        getView().showProgressDialog();
    }
    public void setNavDrawerMenu(List<String> listTitles) {
        getView().setNavDrawerMenu(listTitles);
    }
    public void refresh(){
        getView().refresh();
    }

    public void showSwipeRefreshProgress(boolean b){
        getView().showSwipeRefreshProgress(b);
    }




    //-----------------DATABASE OPERATIONS------------///
    //-----------------DATABASE OPERATIONS------------///

//    public List<LocalTask> getTasksFromList(String listId,String sort) {
//        return mModel.getTasksFromList(listId, sort);
//    }

    public List<LocalTask> getTasksFromList(String listId) {
        return mModel.getTasksFromList(listId);
    }

    public void updateLists(List<TaskList> lists) {
        setListsInfo(lists);
        mModel.updateLists(lists);
    }

    public long  getTaskReminder(String taskId){
        return mModel.getTaskReminder(taskId);
    }

    public void updateTasks(List<LocalTask> tasks) {
        mModel.updateTasks(tasks);
    }

    public List<String> getListsTitles() {
        return mModel.getListsTitles();
    }

    public List<String> getListsIds() {
        return mModel.getListsIds();
    }

    private void setListsInfo(List<TaskList> lists){
        Co.listIds.clear();
        Co.listTitles.clear();
        for (int i = 0; i < lists.size(); i++){
            Co.listIds.add(lists.get(i).getId());
            Co.listTitles.add(lists.get(i).getTitle());
        }
    }

    public void saveStringSharedPreference(String currentListTitle, String title) {
        getView().saveStringShP(currentListTitle, title);
    }

    public String getStringShP(String key) {
        return getView().getStringShP(key);
    }

    public boolean getBooleanShP(String key) {
        return getView().getBooleanSharedPreference(key);
    }

    public void requestApiPermission(Exception mLastError) {
        getView().requestAuthorization(mLastError);
    }

    public void  setUpData(){
        getView().setUpData();
    }

    public String getListTitleFromId(String listId) {
        return mModel.getListTitleFromId(listId);
    }

    public void onClick(int id) {
        switch (id){
            case R.id.fab:
                getView().showToast(String.valueOf(android.R.id.home));
                break;
        }
    }

    public String getString(int stringId) {
        return getView().getContext().getString(stringId);
    }

    public void deleteTask(String taskId) {
        int rowDeleted =  mModel.deleteTask(taskId);
        if (rowDeleted < 0) {
           showToast(getView().getContext().getString(R.string.task_not_found));
        }
    }

    public void deleteTask(String taskId, String listId) {
        mModel.deleteTask(taskId,listId);
    }

    public GoogleAccountCredential getCredential() {
        getView().setCredentials();
        return getView().getCredential();
    }

    public void updateTaskStatus(String taskId, String listId, String newStatus) {
        mModel.updateTaskStatus(taskId, listId, newStatus);
    }

    public boolean isDeviceOnline() {
        return getView().isDeviceOnline();
    }

    public void navigateToEditTask(Intent i) {
        getView().navigateToEditTask(i);
    }

    public void refreshFirstTime() {
        mModel.refreshFirstTime();
    }

    public int addTask(LocalTask task){
        return mModel.addTask(task);
    }

    public void showEmptyRecyclerView(boolean b) {
        getView().showEmptyRecyclerView(b);
    }

    public void moveTask(LocalTask movedTask, String previousTaskId) {
        mModel.moveTask(movedTask, previousTaskId);
    }

    public void editTask(LocalTask task) {
        mModel.editTask(task);
    }

    public long updateReminder(String taskId, long reminder){
        return mModel.updateReminder(taskId, reminder);
    }

    public void saveBooleanShP(String key, boolean b) {
        getView().saveBooleanShP(key, b);
    }

    public boolean taskExistsInDB(String taskId) {
        return mModel.taskExistsInDB(taskId);
    }

    public int addTaskToDatabase(LocalTask task) {
        return mModel.addTaskToLocalDatabase(task);
    }

    public void addTaskFirstTimeFromServer(Task task, String listId) {
        mModel.addTaskFirstTimeFromServer(task, listId);
    }

    public void addTaskToAdapter(LocalTask localTask) {
        getView().addTaskToAdapter(localTask);
    }

    public void updateSyncStatus(String taskId, int synced) {
        mModel.updateSyncStatus(synced, taskId);
    }

    public long getTaskReminderId(String taskId) {
        return mModel.getTaskReminderId(taskId);
    }

    public List<LocalTask> getLocalTasksFromDB() {
        return mModel.getLocalTasksFromDB();
    }

    public LocalTask getTask(String id) {
        return mModel.getTask(id);
    }

    public int updateLocalTask(LocalTask modifiedTask) {
        return mModel.updateLocalTask(modifiedTask);
    }

    public void updateSibling(String taskId, String previousTaskId) {
        mModel.updateSibling(taskId, previousTaskId);
    }

    public List<LocalTask> getTasksFromListForAdapter(String listId) {
        return mModel.getTaskFromListForAdapter(listId);
    }

    public void updateLocalTask(Task task, String listId) {
        mModel.updateLocalTask(task, listId);
    }

    public void markDeleted(String taskId) {
        mModel.markDeleted(taskId);
    }

    public LocalTask updateNewlyCreatedTask(Task aTask, String listId, String intId) {
        return mModel.updateNewlyCreatedTask(aTask, listId, intId);
    }

    public void setTemporaryPosition(String taskId, String newTaskTempPos) {
        mModel.setTemporaryPosition(taskId, newTaskTempPos);
    }

    public void updateMoved(String id, int moved) {
        mModel.updateMoved(id, moved);
    }

    public void updatePosition(Task task) {
        mModel.updatePosition(task);
    }

    public void updateSiblingByIntId(int id, int sibling) {
        mModel.updateSiblingByIntId(id,sibling);
    }

    public String getTaskIdByIntId(int id) {
        return mModel.getTaskIdByIntId(id);
    }

    public void updateMovedByIntId(int intId, int moved) {
        mModel.updateMovedByIntId(intId, moved);
    }

    public void setTemporaryPositionByIntId(int intId, String newTaskTempPos) {
        mModel.setTemporaryPositionByIntId(intId, newTaskTempPos);
    }

    public void deleteTaskFromDataBase(int intId) {
        mModel.deleteTaskFromDataBase(intId);
    }
}
