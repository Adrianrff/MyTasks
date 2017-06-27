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

   public static List<TaskList> serverListsNotInDB(List<String> localListsIds, List<TaskList> serverLists) {
      List<TaskList> listsNotInDb = new ArrayList<>();
      if (localListsIds.isEmpty()) {
         return serverLists;
      }
      for (int i = 0; i < localListsIds.size(); i++) {
         String item = localListsIds.remove(i).trim();
         localListsIds.add(i, item);
      }
      for (int i = 0; i < serverLists.size(); i++) {
         if (!localListsIds.contains(serverLists.get(i).getId().trim())){
            listsNotInDb.add(serverLists.get(i));
         }
      }
      return listsNotInDb;
//      if (serverLists != null && !serverLists.isEmpty()) {
//         for (int i = 0; i < serverLists.size(); i++) {
//            String currentServerList = serverLists.get(i);
//            boolean serverListsInDb = false;
//            for (int j = 0; j < localListsIds.size(); j++) {
//               String currentLocalListId = localListsIds.get(j);
//               if (currentLocalListId == null) {
//                  continue;
//               }
//               if (currentServerList.trim().equals(currentLocalListId.trim())) {
//                  serverListsInDb = true;
//                  continue;
//               }
//               if (j == localListsIds.size() - 1 && !serverListsInDb) {
//                  listsNotInDb.add(currentServerList);
//               }
//            }
//         }
//         return listsNotInDb;
//      } else {
//         return new ArrayList<>();
//      }
   }

   public static List<LocalList> localListsNotInServer(List<LocalList> localLists, List<String> serverListsIds) {
      List<LocalList> listsNotInServer = new ArrayList<>();
      if (serverListsIds.isEmpty()) {
         return localLists;
      }
      for (int i = 0; i < serverListsIds.size(); i++) {
         String item = serverListsIds.remove(i).trim();
         serverListsIds.add(i, item);
      }
      for (int i = 0; i < localLists.size(); i++) {
         if (!serverListsIds.contains(localLists.get(i).getId().trim())){
            listsNotInServer.add(localLists.get(i));
         }
      }
      return listsNotInServer;

//      if (localLists != null && !localLists.isEmpty()) {
//         for (int i = 0; i < localLists.size(); i++) {
//            LocalList currentLocalList = localLists.get(i);
//            String currentLocalListId = currentLocalList.getId();
//            if (currentLocalListId != null) {
//               boolean isLocalListInServer = false;
//               for (int j = 0; j < serverListsIds.size(); j++) {
//                  TaskList currentServerList = serverListsIds.get(j);
//                  if (currentServerList == null) {
//                     continue;
//                  }
//                  if (currentLocalListId.trim().equals(currentServerList.getId().trim())) {
//                     isLocalListInServer = true;
//                     continue;
//                  }
//                  if (j == serverListsIds.size() - 1 && !isLocalListInServer) {
//                     listsNotInServer.add(currentLocalList);
//                  }
//               }
//            } else {
//               listsNotInServer.add(currentLocalList);
//            }
//         }
//         return listsNotInServer;
//      } else {
//         return new ArrayList<>();
//      }
   }


   public static TaskList getServerListById(List<TaskList> serverLists, String localListId) {
      if (!serverLists.isEmpty()) {
         TaskList sameServerList = null;
         for (int i = 0; i < serverLists.size(); i++) {
           if (serverLists.get(i).getId().trim().equals(localListId)){
              sameServerList = serverLists.get(i);
              break;
            }
         }
         return sameServerList;
      } else {
         return null;
      }
   }

   public static LocalList getLocalListById(List<LocalList> localLists, String serverListId) {
      if (!localLists.isEmpty()) {
         LocalList sameLocalList = null;
         for (int i = 0; i < localLists.size(); i++) {
            if (localLists.get(i).getId().trim().equals(serverListId)){
               sameLocalList = localLists.get(i);
               break;
            }
         }
         return sameLocalList;
      } else {
         return null;
      }
   }
}
