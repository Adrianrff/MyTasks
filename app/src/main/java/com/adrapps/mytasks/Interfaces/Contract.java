package com.adrapps.mytasks.Interfaces;

import android.content.Context;

import com.adrapps.mytasks.LocalTask;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;

public class Contract {

    //-------------------------VIEW OPS----------------------//
    public interface View{

        void showToast(String msg);

        Context getAppContext();

        void showProgress(boolean b);

        Context getContext();

        void requestAuthorization(Exception e);

        void updateAdapterItems(List<LocalTask> localTasks);
    }

    //-------------------------MODEL OPS----------------------//
    public interface Model{

        void updateLists(List<TaskList> lists);

        void updateTasks(List<LocalTask> tasks);

        void addTasksInBatchesFromList(List<Task> tasks, String listId);

        List<LocalTask> getTasksFromList(String listId);

        List<String> getListsTitles();

        List<String> getListsIds();
    }
}
