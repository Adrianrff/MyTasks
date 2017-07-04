package com.adrapps.mytasks.helpers;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.ListItem;
import com.adrapps.mytasks.domain.LocalTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortTasksHelper {

   public static List<ListItem> sortTasks(List<LocalTask> tasks, int sortBy){
      ArrayList<ListItem> sortedList = new ArrayList<>();

      //get all the different dates of tasks
      List<String> dates = new ArrayList<>();
      String date;
      Collections.sort(tasks, new Comparator<LocalTask>() {
         @Override
         public int compare(LocalTask o1, LocalTask o2) {
            return (int) (o1.getDue() - o2.getDue());
         }
      });
      for (LocalTask task : tasks){
         if (task.getDue() != 0){
            date = DateHelper.millisToDateOnly(task.getDue());
         } else {
            date = "No date";
         }
         if (!dates.contains(date)){
            dates.add(date);

         }
      }
      switch (sortBy){
         case Co.SORT_CREATION:

      }
      return sortedList;
   }
}
