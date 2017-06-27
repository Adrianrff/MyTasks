package com.adrapps.mytasks.domain;

import android.support.annotation.NonNull;

import com.adrapps.mytasks.helpers.DateHelper;
import com.google.api.services.tasks.model.Task;

import java.io.Serializable;
import java.util.TimeZone;

public class LocalTask implements Serializable, Comparable {

    private String id, title, parent, position, notes, status, listId;
    private int moved, localDeleted, intId, previousTask, syncStatus, repeatMode,
          adapterPos, repeatDay, listIntId;
    private long serverModify, due, completed, localModify, reminder, reminderId;
    private boolean deleted,hidden;


    public LocalTask() {
    }

    public LocalTask (String taskTitle, long dueDate){
        title = taskTitle;
        this.due = dueDate;
        this.reminder = 0;
        this.reminderId = 0;
        this.repeatMode = 0;
        this.syncStatus = Co.NOT_SYNCED;
        this.localDeleted = 0;
        this.moved = 0;
    }

    public LocalTask (String taskTitle, long dueDate, String notes){
        title = taskTitle;
        this.due = dueDate;
        this.notes = notes;
        this.reminder = 0;
        this.reminderId = 0;
        this.repeatMode = 0;
        this.syncStatus = Co.NOT_SYNCED;
        this.localDeleted = 0;
        this.moved = 0;

    }

    public LocalTask (String taskTitle, long dueDate, String notes, long reminder){
        title = taskTitle;
        this.due = dueDate;
        this.notes = notes;
        this.reminder = reminder;
        this.reminderId = System.currentTimeMillis();
        this.repeatMode = 0;
        this.syncStatus = Co.NOT_SYNCED;
        this.localDeleted = 0;
        this.moved = 0;
    }

    public LocalTask(Task task, String listId) {
        int offSet = TimeZone.getDefault().getRawOffset();
        this.id = task.getId();
        this.title = task.getTitle();
        this.parent = task.getParent();
        this.position = task.getPosition();
        this.notes = task.getNotes();
        this.status = task.getStatus();
        this.listId = listId;
        this.serverModify = (task.getUpdated() == null) ? 0:task.getUpdated().getValue();
        this.due = (task.getDue() == null) ? 0:task.getDue().getValue() - offSet;
        this.completed = (task.getCompleted() == null) ? 0:task.getCompleted().getValue();
        this.deleted = (task.getDeleted() == null) ? false:task.getDeleted();
        this.hidden = (task.getHidden() == null) ? false:task.getHidden();
        this.reminder = 0;
        this.reminderId = 0;
        this.repeatMode = 0;
        this.syncStatus = Co.NOT_SYNCED;
        this.localDeleted = 0;
        this.moved = 0;
    }

    public static Task localTaskToApiTask (LocalTask lTask){
        Task task = new Task();
        task.setTitle(lTask.title);
        if (lTask.getNotes() != null){
            if (!lTask.getNotes().trim().equals("")){
                task.setNotes(lTask.getNotes());
            } else {
                task.setNotes(null);
            }
        } else {
            task.setNotes(null);
        }

        if (lTask.getDue() != 0) {
            task.setDue(DateHelper.millisecondsToDateTime(lTask.getDue()));
        } else {
            task.setDue(null);
        }
        return task;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalTask) {
            LocalTask task = (LocalTask) obj;
            return this.getIntId() == task.getIntId();
        } else {
            return false;
        }
    }




    ///-------------------SETTERS ----------------------//


   public void setListIntId(int listIntId) {
      this.listIntId = listIntId;
   }

   public void setRepeatDay(int repeatDay) {
      this.repeatDay = repeatDay;
   }

   public void setAdapterPos(int adapterPos) {
      this.adapterPos = adapterPos;
   }

   public void setMoved(int moved) {
        this.moved = moved;
    }

    public void setLocalModify() {
        this.localModify = System.currentTimeMillis() + 10000;
    }

    public void setLocalModify(long localModify) {
        this.localModify = localModify;
    }

    public void setLocalDeleted(int localDeleted) {
        this.localDeleted = localDeleted;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public void setReminder(long reminder) {
        this.reminder = reminder;
        this.reminderId = System.currentTimeMillis();
    }


    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }

    public void setReminderNoID(long reminder) {
        this.reminder = reminder;
    }

    public void setReminderId (long reminderId){
        this.reminderId = reminderId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public void setIntId(int intId) {
        this.intId = intId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setServerModify(long serverModify) {
        this.serverModify = serverModify;
    }

    public void setDue(long due) {
        this.due = due;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }



    ///-------------------GETTERS---------------------///


   public int getRepeatDay() {
      return repeatDay;
   }

   public int getAdapterPos() {
      return adapterPos;
   }

   public int getRepeatMode() {
        return repeatMode;
    }

    public int getPreviousTask() {
        return previousTask;
    }

    public int getMoved() {
        return moved;
    }

    public long getLocalModify() {
        return localModify;
    }

    public long getLocalDeleted() {
        return localDeleted;
    }

    public void setPreviousTask(int localSibling) {
        this.previousTask = localSibling;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public long getReminderId() {
        return reminderId;
    }

    public long getReminder() {
        return reminder;
    }

    public String getListId() {
        return listId;
    }

    public int getIntId() {
        return intId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getParent() {
        return parent;
    }

    public String getPosition() {
        return position;
    }

    public String getNotes() {
        return notes;
    }

    public String getStatus() {
        return status;
    }

    public long getServerModify() {
        return serverModify;
    }

    public long getDue() {
        return due;
    }

    public long getCompleted() {
        return completed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isHidden() {
        return hidden;
    }

   public int getListIntId() {
      return listIntId;
   }

   @Override
    public int compareTo(@NonNull Object o) {
        return 0;
    }
}
