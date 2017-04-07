package com.adrapps.mytasks.Presenter;

import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.Domain.LocalTask;
import com.adrapps.mytasks.Models.DatabaseModel;
import com.adrapps.mytasks.R;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import java.lang.ref.WeakReference;
import java.util.List;

public class TaskListPresenter  {

    private WeakReference<Contract.View> mView;
    private Contract.Model mModel;

//------------------CONSTRUCTOR-------------------////
    public TaskListPresenter(Contract.View view) {
        mView = new WeakReference<>(view);
        getDatabaseModel();
    }

    private void getDatabaseModel(){
        mModel = new DatabaseModel(this, getView().getContext());
    }

    private Contract.View getView() throws NullPointerException{
        if ( mView != null )
            return mView.get();
        else
            throw new NullPointerException("View is unavailable");
    }

    //-------------METHODS------------------///

    public List<LocalTask> getTasksFromList(String listId){
        return mModel.getTasksFromList(listId);
    }

    public void showToast(String msg){
        getView().showToast(msg);
    }

    public void showProgress(boolean b){
        getView().showProgress(b);
    }

    public void updateLists(List<TaskList> lists) {
        mModel.updateLists(lists);
    }

    public void updateTasks(List<LocalTask> tasks) {
        mModel.updateTasks(tasks);
    }

    public void addTask(Task task, TaskList taskList) {

    }

   public void showProgressDialog(){
        getView().showProgressDialog();
   }

   public void dismissProgressDialog(){
       getView().dismissProgressDialog();
   }



    public void addTasksInBatchesFromList(List<Task> tasks, String listId) {
        mModel.addTasksInBatchesFromList(tasks,listId);

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

    public void updateAdapterItems(List<LocalTask> localTasks) {
        getView().updateAdapterItems(localTasks);
    }

    public void saveStringSharedPreference(String currentListTitle, String title) {
        getView().saveStringSharedPreference(currentListTitle,title);
    }

    public String getStringSharedPreference(String key){
        return getView().getStringSharedPreference(key);
    }

    public void setToolbarTitle(String title){
        getView().setToolbarTitle(title);
    }

    public void initRecyclerView(List<LocalTask> tasks) {
        getView().initRecyclerView(tasks);
    }

    public void setUpViews(){
        getView().setUpViews();
    }

    public void setTaskListTitles(List<String> titles){
        getView().setTaskListsTitles(titles);
    }

    public void setListsIds(List<String> listIds){
        getView().setListsIds(listIds);
    }

    public void onClick(int id) {
        switch (id){
            case R.id.fab:
                expandNewTaskLayout();
                break;
        }
    }

    public void updateCurrentView() {
        getView().updateCurrentView();
    }

    public String getListTitleFromId(String listId){
        return mModel.getListTitleFromId(listId);
    }

    public void expandNewTaskLayout(){

        getView().expandNewTaskLayout();
    }

    public void collapseNewTaskLayout(){
        getView().collapseNewTaskLayout();
    }
}
