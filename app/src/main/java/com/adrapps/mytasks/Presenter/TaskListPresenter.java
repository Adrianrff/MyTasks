package com.adrapps.mytasks.Presenter;

import android.content.Intent;

import com.adrapps.mytasks.APICalls.AddTask;
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

    public List<LocalTask> getTasksFromList(String listId) {
        return mModel.getTasksFromList(listId);
    }

    public void updateLists(List<TaskList> lists) {
        setListsInfo(lists);
        mModel.updateLists(lists);
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

    public void setListsInfo(List<TaskList> lists){
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
        if (getView().isDeviceOnline()) {
            AddTask add = new AddTask(this, getView().getCredential(), listId);
            add.execute(task);
        }
        else
            showToast(getString(R.string.no_internet_toast));

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

    public void addTaskToApi(LocalTask task){
        mModel.addTaskToApi(task);
    }
}
