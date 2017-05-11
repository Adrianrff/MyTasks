package com.adrapps.mytasks.helpers;

import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.ArrayList;
import java.util.List;

public class CompareLists {

    public static List<LocalTask> localTasksNotInServer(List<LocalTask> localTasks, List<Task> serverTasks) {
        List<LocalTask> tasksNotInServer = new ArrayList<>();
        if (serverTasks == null || serverTasks.isEmpty()){
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
                    if (currentLocalTask.getId() == null){
                        continue;
                    }
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

    public static List<TaskList> serverListsNotInDB(List<LocalList> localLists, List<TaskList> serverLists) {
        List<TaskList> listsNotInDb = new ArrayList<>();
        if (localLists.isEmpty()){
            return serverLists;
        }
        if (serverLists != null && !serverLists.isEmpty()) {
            for (int i = 0; i < serverLists.size(); i++) {
                TaskList currentServerList = serverLists.get(i);
                boolean serverListsInDb = false;
                for (int j = 0; j < localLists.size(); j++) {
                    LocalList currentLocalList = localLists.get(j);
                    if (currentLocalList.getId() == null){
                        continue;
                    }
                    if (currentServerList.getId().trim().equals(currentLocalList.getId().trim())) {
                        serverListsInDb = true;
                        continue;
                    }
                    if (j == localLists.size() - 1 && !serverListsInDb) {
                        listsNotInDb.add(currentServerList);
                    }
                }
            }
            return listsNotInDb;
        } else {
            return new ArrayList<>();
        }
    }

    public static List<LocalList> localListsNotInServer(List<LocalList> localLists, List<TaskList> serverLists) {
        List<LocalList> listsNotInServer = new ArrayList<>();
        if (serverLists.isEmpty()){
            return localLists;
        }
        if (localLists != null && !localLists.isEmpty()) {
            for (int i = 0; i < localLists.size(); i++) {
                LocalList currentLocalList = localLists.get(i);
                if (currentLocalList.getId() != null) {
                    boolean isLocalListInServer = false;
                    for (int j = 0; j < serverLists.size(); j++) {
                        TaskList currentServerList = serverLists.get(j);
                        if (currentLocalList.getId().trim().equals(currentServerList.getId().trim())) {
                            isLocalListInServer = true;
                            continue;
                        }
                        if (j == serverLists.size() - 1 && !isLocalListInServer) {
                            listsNotInServer.add(currentLocalList);
                        }
                    }
                } else {
                    listsNotInServer.add(currentLocalList);
                }
            }
            return listsNotInServer;
        } else {
            return new ArrayList<>();
        }
    }


}
