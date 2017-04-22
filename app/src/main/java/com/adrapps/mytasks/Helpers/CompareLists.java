package com.adrapps.mytasks.Helpers;

import com.adrapps.mytasks.Domain.LocalTask;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian Flores on 20/4/2017.
 */

public class CompareLists {

    public static List<LocalTask> localTasksNotInServer(List<LocalTask> localTasks, List<Task> serverTasks) {
        List<LocalTask> tasksNotInServer = new ArrayList<>();
        if (serverTasks.isEmpty()){
            return localTasks;
        }
        if (localTasks != null && !localTasks.isEmpty()) {
            for (int i = 0; i < localTasks.size(); i++) {
                LocalTask currentLocalTask = localTasks.get(i);
                if (currentLocalTask.getId() != null) {
                    boolean localTaskInServer = false;
                    for (int j = 0; j < serverTasks.size(); j++) {
                        Task currentServerTask = serverTasks.get(j);
                        if (currentLocalTask.getId().trim().equals(currentServerTask.getId().trim())) {
                            localTaskInServer = true;
                            continue;
                        }
                        if (j == serverTasks.size() - 1 && !localTaskInServer) {
                            tasksNotInServer.add(currentLocalTask);
                        }
                    }
                } else {
                    tasksNotInServer.add(currentLocalTask);
                }
            }
            return tasksNotInServer;
        } else {
            return new ArrayList<>();
        }
    }

    public static List<Task> serverTasksNotInDB(List<LocalTask> localTasks, List<Task> serverTasks) {
        List<Task> tasksNotInDB = new ArrayList<>();
        if (localTasks.isEmpty()){
            return serverTasks;
        }
        if (serverTasks != null && !serverTasks.isEmpty()) {
            for (int i = 0; i < serverTasks.size(); i++) {
                Task currentServerTask = serverTasks.get(i);
                boolean serverTaskInDB = false;
                for (int j = 0; j < localTasks.size(); j++) {
                    LocalTask currentLocalTask = localTasks.get(j);
                    if (currentServerTask.getId().trim().equals(currentLocalTask.getId().trim())) {
                        serverTaskInDB = true;
                        continue;
                    }
                    if (j == localTasks.size() - 1 && !serverTaskInDB) {
                        tasksNotInDB.add(currentServerTask);
                    }
                }
            }
            return tasksNotInDB;
        } else {
            return new ArrayList<>();
        }
    }

    //Excludes tasks created offline (not synced)
    public static List<LocalTask> commonTasks(List<LocalTask> localTasks, List<Task> serverTasks) {
        if (localTasks != null && serverTasks != null && !localTasks.isEmpty() && !serverTasks.isEmpty()) {
            List<LocalTask> commonTasks = new ArrayList<>();
            for (int i = 0; i < localTasks.size(); i++) {
                LocalTask currentLocalTask = localTasks.get(i);
                if (currentLocalTask.getId() != null) {
                    for (int j = 0; j < serverTasks.size(); j++) {
                        Task currentServerTask = serverTasks.get(j);
                        if (currentLocalTask.getId().trim().equals(currentServerTask.getId().trim())) {
                            commonTasks.add(currentLocalTask);
                        }
                    }
                }
            }
            return commonTasks;
        } else {
            return new ArrayList<>();
        }

    }
}
