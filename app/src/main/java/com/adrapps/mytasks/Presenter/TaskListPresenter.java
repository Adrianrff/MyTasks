package com.adrapps.mytasks.Presenter;

import com.adrapps.mytasks.APICalls.AddTask;
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

    //-------------METHODS------------------///

    public List<LocalTask> getTasksFromList(String listId) {
        return mModel.getTasksFromList(listId);
    }

    public void showToast(String msg) {
        getView().showToast(msg);
    }


    public void updateLists(List<TaskList> lists) {
        mModel.updateLists(lists);
    }

    public void updateTasks(List<LocalTask> tasks) {
        mModel.updateTasks(tasks);
    }


    public void dismissProgressDialog() {
        getView().dismissProgressDialog();
    }

    public void requestApiPermission(Exception mLastError) {
        getView().requestAuthorization(mLastError);
    }

    public List<String> getListsTitles() {
        return mModel.getListsTitles();
    }

    public List<String> getListsIds() {
        return mModel.getListsIds();
    }

    public void setTaskListTitles(List<String> titles) {
        getView().setListsTitles(titles);
    }

    public void setListsIds(List<String> listIds) {
        getView().setListsIds(listIds);
    }

    public void saveStringSharedPreference(String currentListTitle, String title) {
        getView().saveStringShP(currentListTitle, title);
    }

    public String getStringSharedPreference(String key) {
        return getView().getStringShP(key);
    }

    public boolean getBooleanSharedPreference(String key) {
        return getView().getBooleanSharedPreference(key);
    }

    public void setToolbarTitle(String title) {
        getView().setToolbarTitle(title);
    }

    public void initRecyclerView(List<LocalTask> tasks) {
        getView().initRecyclerView(tasks);
    }

    public void setUpViews() {
        getView().setUpViews();
    }

    public void  setUpData(){
        getView().setUpData();
    }

    public void updateCurrentView() {
        getView().updateCurrentView();
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

    public void setNavDrawerMenu(List<String> listTitles) {
        getView().setNavDrawerMenu(listTitles);
    }

    public void showProgressDialog() {
        getView().showProgressDialog();
    }

    public void showProgress(boolean b) {
        getView().showCircularProgress(b);
    }

    public void showUndoSnackBar(String message, int position, LocalTask removedTask) {
        getView().showUndoSnackBar(message, position, removedTask);
    }

    public void addTaskToLocalDataBase(Task task, String listId){
        mModel.addTaskToLocalDatabase(task,listId);
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

    public void deleteTaskFromApi(String taskId, String listId) {
        mModel.deleteTaskFromApi(taskId,listId);
    }

    public GoogleAccountCredential getCredential() {
        getView().setCredentials();
        return getView().getCredential();
    }

    public void addTaskToApi(LocalTask task, String listId){
        AddTask add = new AddTask(this,getView().getCredential(),listId);
        add.execute(task);
    }

    public void updateTaskStatus(String taskId, String listId, String newStatus) {
        mModel.updateTask(taskId, listId, newStatus);
    }

    public void showSwipeRefreshProgress(boolean b){
        getView().showSwipeRefreshProgress(b);
    }
}
