package com.adrapps.mytasks.Presenter;


import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.view.View;
import com.adrapps.mytasks.Interfaces.Contract;
import com.adrapps.mytasks.LocalTask;
import com.adrapps.mytasks.Models.DatabaseModel;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import java.lang.ref.WeakReference;
import java.util.List;

public class TaskListPresenter implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

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

    public List<LocalTask> getTasksFromLlist(String listId){
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

    public void addTasksInBatchesFromList(List<Task> tasks, String listId) {
        mModel.addTasksInBatchesFromList(tasks,listId);

    }

    public void requestApiPermission(Exception mLastError) {
        getView().requestAuthorization(mLastError);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onClick(View v) {

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
}
