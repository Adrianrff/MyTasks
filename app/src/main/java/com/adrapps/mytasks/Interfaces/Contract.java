package com.adrapps.mytasks.Interfaces;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;

import com.adrapps.mytasks.Domain.LocalTask;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;

public class Contract {

    //-------------------------VIEW OPS----------------------//
    public interface MainActivityViewOps {


        void showToast(String msg);

        Context getAppContext();

        void showProgress(boolean b);

        Context getContext();

        void requestAuthorization(Exception e);

        String getStringSharedPreference(String key);

        boolean getBooleanSharedPreference(String key);

        void saveStringSharedPreference(String key, String value);

        void setToolbarTitle(String title);

        void setUpViews();

        void setUpData();

        void setNavDrawerMenu();

        void showProgressDialog();

        void dismissProgressDialog();

        void setAdapterOps(AdapterOps aOps);

    }


    //-------------------------MODEL OPS----------------------//
    public interface Model{

        void updateLists(List<TaskList> lists);

        void updateTasks(List<LocalTask> tasks);

        void addTasksInBatchesFromList(List<Task> tasks, String listId);

        List<LocalTask> getTasksFromList(String listId);

        List<String> getListsTitles();

        List<String> getListsIds();

        String getListTitleFromId(String listId);

    }

    //------------------FRAGMENT OPS---------------------//

    public interface AdapterOps {

        void updateCurrentView();

        void setListsTitles(List<String> titles);

        void setListsIds(List<String> listIds);

        void updateAdapterItems(List<LocalTask> localTasks);

        void initRecyclerView(List<LocalTask> tasks);


    }
}
