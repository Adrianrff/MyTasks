package com.adrapps.mytasks.Interfaces;

import android.content.Context;

import com.adrapps.mytasks.LocalTask;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.List;

/**
 * Created by Adrian Flores on 28/3/2017.
 */

public class Contract {

    //-------------------------VIEW OPS----------------------//
    public interface View{

        void showToast(String msg);

        Context getAppContext();

        void showProgress(boolean b);

        Context getContext();

        void requestAuthorization(Exception e);

    }

    //-------------------------MODEL OPS----------------------//
    public interface Model{

        void updateLists(List<TaskList> lists);

        void updateTasks(List<LocalTask> tasks);

        void addTasksInBatchesFromList(List<Task> tasks, String listId);

        List<LocalTask> getTasksFromList(String listId);
    }
}
