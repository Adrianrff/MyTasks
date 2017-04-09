package com.adrapps.mytasks.Presenter;

import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Models.DatabaseModel;
import com.google.api.services.tasks.model.TaskList;

import java.lang.ref.WeakReference;
import java.util.List;

public class TaskListPresenter {

    private WeakReference<Contract.AdapterOps> aView;
    private WeakReference<Contract.MainActivityViewOps> mView;
    private Contract.Model mModel;

//------------------CONSTRUCTOR-------------------////

    public TaskListPresenter(Contract.MainActivityViewOps mView, Contract.AdapterOps aView) {
        this.aView = new WeakReference<>(aView);
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

    private Contract.AdapterOps getaView() throws NullPointerException {
        if (aView != null)
            return aView.get();
        else
            throw new NullPointerException("AdapterOps is unavailable");
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
        getaView().setListsTitles(titles);
    }

    public void setListsIds(List<String> listIds) {
        getaView().setListsIds(listIds);
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
        getaView().initRecyclerView(tasks);
    }

    public void setUpViews() {
        getView().setUpViews();
    }


    public void updateCurrentView() {
        getaView().updateCurrentView();
    }

    public String getListTitleFromId(String listId) {
        return mModel.getListTitleFromId(listId);
    }

    public void onClick(int id) {

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

}
