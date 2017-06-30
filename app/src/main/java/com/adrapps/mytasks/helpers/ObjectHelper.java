package com.adrapps.mytasks.helpers;

import android.util.SparseArray;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import java.util.HashMap;
import java.util.List;

/**
 * Created by adria on 26/06/2017.
 */

public class ObjectHelper {

   public static SparseArray<LocalList> getLocalListIntIdMap(List<LocalList> lists){
      SparseArray<LocalList> map = new SparseArray<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getIntId(), lists.get(i));
      }
      return map;
   }

   public static  HashMap<String, LocalList> getLocalListIdMap(List<LocalList> lists){
      HashMap<String, LocalList> map = new HashMap<>();
      String key;
      for (int i = 0; i < lists.size(); i++) {
         if (lists.get(i).getId() != null){
            key = Co.LIST_ID_NULL;
         } else {
            key = lists.get(i).getId().trim();
         }
         map.put(key, lists.get(i));
      }
      return map;
   }



   public static HashMap<String, TaskList> getServerListIdMap(List<TaskList> lists){
      HashMap<String, TaskList> map = new HashMap<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getId().trim(), lists.get(i));
      }
      return map;
   }

   public static HashMap<String, Task> getTaskIdMap(List<Task> lists){
      HashMap<String, Task> map = new HashMap<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getId(), lists.get(i));
      }
      return map;
   }

   public static SparseArray<LocalTask> getTaskIntIdMap(List<LocalTask> lists){
      SparseArray<LocalTask> map = new SparseArray<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getIntId(), lists.get(i));
      }
      return map;
   }


}
