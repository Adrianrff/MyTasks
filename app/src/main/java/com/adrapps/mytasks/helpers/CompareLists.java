package com.adrapps.mytasks.helpers;

import com.adrapps.mytasks.domain.LocalTask;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;

public class CompareLists {

   public static List<LocalTask> localTasksNotInServer(List<LocalTask> localTasks, List<Task> serverTasks) {
      List<LocalTask> tasksNotInServer = new ArrayList<>();
      if (serverTasks == null || serverTasks.isEmpty()) {
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
      if (localTasks.isEmpty()) {
         return serverTasks;
      }
      if (serverTasks != null && !serverTasks.isEmpty()) {
         for (int i = 0; i < serverTasks.size(); i++) {
            Task currentServerTask = serverTasks.get(i);
            boolean serverTaskInDB = false;
            for (int j = 0; j < localTasks.size(); j++) {
               LocalTask currentLocalTask = localTasks.get(j);
               if (currentLocalTask.getId() == null) {
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

   public static List<String> serverListsNotInDB(List<String> localLists, List<String> serverLists) {
      List<String> listsNotInDb = new ArrayList<>();
      if (localLists.isEmpty()) {
         return serverLists;
      }
      if (serverLists != null && !serverLists.isEmpty()) {
         for (int i = 0; i < serverLists.size(); i++) {
            String currentServerList = serverLists.get(i);
            boolean serverListsInDb = false;
            for (int j = 0; j < localLists.size(); j++) {
               String currentLocalListId = localLists.get(j);
               if (currentLocalListId == null) {
                  continue;
               }
               if (currentServerList.trim().equals(currentLocalListId.trim())) {
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

   public static List<String> localListsNotInServer(List<String> localLists, List<String> serverLists) {
      List<String> listsNotInServer = new ArrayList<>();
      if (serverLists.isEmpty()) {
         return localLists;
      }
      if (localLists != null && !localLists.isEmpty()) {
         for (int i = 0; i < localLists.size(); i++) {
            String currentLocalListId = localLists.get(i);
            boolean isLocalListInServer = false;
               for (int j = 0; j < serverLists.size(); j++) {
                  String currentServerListId = serverLists.get(j);
                  if (currentServerListId == null) {
                     continue;
                  }
                  if (currentLocalListId.trim().equals(currentServerListId.trim())) {
                     isLocalListInServer = true;
                     continue;
                  }
                  if (j == serverLists.size() - 1 && !isLocalListInServer) {
                     listsNotInServer.add(currentLocalListId);
                  }
               }

         }
         return listsNotInServer;
      } else {
         return new ArrayList<>();
      }
   }


}
