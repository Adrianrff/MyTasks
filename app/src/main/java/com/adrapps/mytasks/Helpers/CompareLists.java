package com.adrapps.mytasks.Helpers;

import com.adrapps.mytasks.Domain.LocalTask;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrian Flores on 20/4/2017.
 */

public class CompareLists {

    public static List<LocalTask> localTasksNotInServer (List<LocalTask> localTasks, List<Task> serverTasks){
        List<LocalTask> tasksNotInServer = new ArrayList<>();
        for (int i = 0; i < localTasks.size(); i++){
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
    }
}
