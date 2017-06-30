package com.adrapps.mytasks.domain;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class LocalList implements Serializable, Comparable {

   private String title, id;
   private int intId, localDeleted, syncStatus;
   private long localModify;


   public LocalList() {

   }

   //GETTERS


   public int getLocalDeleted() {
      return localDeleted;
   }

   public int getSyncStatus() {
      return syncStatus;
   }

   public String getTitle() {
      return title;
   }

   public String getId() {
      return id;
   }

   public int getIntId() {
      return intId;
   }

   public long getLocalModify() {
      return localModify;
   }


   //SETTERS


   public void setLocalDeleted(int localDeleted) {
      this.localDeleted = localDeleted;
   }

   public void setSyncStatus(int syncStatus) {
      this.syncStatus = syncStatus;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setIntId(int intId) {
      this.intId = intId;
   }

   public void setLocalModify(long local_updated) {
      this.localModify = local_updated;
   }

   @Override
   public int compareTo(@NonNull Object o) {
      return 0;
   }
}
