package com.adrapps.mytasks.Presenter;

import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Models.DatabaseModel;
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
        mModel = new DatabaseModel(this, getView().getContext());
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
        getView().saveStringSharedPreference(currentListTitle, title);
    }

    public String getStringSharedPreference(String key) {
        return getView().getStringSharedPreference(key);
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


    public void updateCurrentView() {
        getView().updateCurrentView();
    }

    public String getListTitleFromId(String listId) {
        return mModel.getListTitleFromId(listId);
    }

    public void onClick(int id) {
        switch (id){
            case android.R.id.home:
                getView().showToast(String.valueOf(android.R.id.home));
                getView().pressBack();
                break;
        }
        getView().showToast(String.valueOf(id));
    }

    public void setNavDrawerMenu(List<String> listTitles) {
        getView().setNavDrawerMenu(listTitles);
    }

    public void setAdapterOps(Contract.AdapterOps aOps) {
        getView().setAdapterOps(aOps);
    }

    public void showProgressDialog() {
        getView().showProgressDialog();
    }

    public void showProgress(boolean b) {
        getView().showCircularProgress(b);
    }

    public void navIconToBack(boolean b){
        getView().navIconToBack(b);
    }

}
