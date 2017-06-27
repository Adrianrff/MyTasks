package com.adrapps.mytasks.helpers;

import android.util.SparseArray;

import com.adrapps.mytasks.domain.LocalList;
import com.adrapps.mytasks.domain.LocalTask;

import java.util.List;

/**
 * Created by adria on 26/06/2017.
 */

public class ObjectHelper {

   public static SparseArray<LocalList> getListMap(List<LocalList> lists){
      SparseArray<LocalList> map = new SparseArray<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getIntId(), lists.get(i));
      }
      return map;
   }

   public static SparseArray<LocalTask> getTaskMap(List<LocalTask> lists){
      SparseArray<LocalTask> map = new SparseArray<>();
      for (int i = 0; i < lists.size(); i++) {
         map.put(lists.get(i).getIntId(), lists.get(i));
      }
      return map;
   }
}
