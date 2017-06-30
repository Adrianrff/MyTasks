package com.adrapps.mytasks.helpers;

import android.util.SparseArray;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ObjectHelper {

   //Map of intId to Local List
   public static SparseArray<LocalList> getLocalListIntIdMap(List<LocalList> lists) {
      SparseArray<LocalList> map = new SparseArray<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getIntId(), lists.get(i));
      }
      return map;
   }

   //Map id to local List
   public static HashMap<String, LocalList> getLocalListIdMap(List<LocalList> lists) {
      HashMap<String, LocalList> map = new HashMap<>();
      String key;
      for (int i = 0; i < lists.size(); i++) {
         if (lists.get(i).getId() == null) {
            key = Co.LIST_ID_NULL;
         } else {
            key = lists.get(i).getId().trim();
         }
         map.put(key, lists.get(i));
      }
      return map;
   }

   public static SparseArray<String> getLocalListIntIdToIdMap(List<LocalList> lists) {
      SparseArray<String> map = new SparseArray<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getIntId(), lists.get(i).getId());
      }
      return map;
   }

   //Map is id to server list
   public static HashMap<String, TaskList> getServerListIdMap(List<TaskList> lists) {
      HashMap<String, TaskList> map = new HashMap<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getId(), lists.get(i));
      }
      return map;
   }

   //Map of id to server task
   public static HashMap<String, Task> getServerTaskIdMap(List<Task> tasks) {
      HashMap<String, Task> map = new HashMap<>();
      for (int i = 0; i < tasks.size(); i++) {
         map.put(tasks.get(i).getId(), tasks.get(i));
      }
      return map;
   }

   //Map of int id to Local Task
   public static SparseArray<LocalTask> getLocalTaskIntIdMap(List<LocalTask> tasks) {
      SparseArray<LocalTask> map = new SparseArray<>();
      for (int i = 0; i < tasks.size(); i++) {
         map.put(tasks.get(i).getIntId(), tasks.get(i));
      }
      return map;
   }

   //Map of id to local task
   public static HashMap<String, LocalTask> getLocalTaskIdMap(List<LocalTask> tasks) {
      HashMap<String, LocalTask> map = new HashMap<>();
      for (int i = 0; i < tasks.size(); i++) {
         map.put(tasks.get(i).getId(), tasks.get(i));
      }
      return map;
   }

   public static SparseArray<String> getLocalTaskIntIdToIdMap(List<LocalTask> tasks) {
      SparseArray<String> map = new SparseArray<>();
      for (int i = 0; i < tasks.size(); i++) {
         map.put(tasks.get(i).getIntId(), tasks.get(i).getId());
      }
      return map;
   }


   public static boolean areListsSynced(List<LocalList> localLists, List<TaskList> serverLists) {
      if (localLists.size() != serverLists.size()) {
         return false;
      }
      List<String> serverIds = new ArrayList<>();
      List<String> localIds = new ArrayList<>();
      for (int i = 0; i < serverLists.size(); i++) {
         serverIds.add(serverLists.get(i).getId());
      }
      for (int i = 0; i < localLists.size(); i++) {
         localIds.add(localLists.get(i).getId());
      }
      for (int i = 0; i < localIds.size(); i++) {
         if (!serverIds.contains(localIds.get(i))) {
            return false;
         }
      }
      return true;
   }
}
