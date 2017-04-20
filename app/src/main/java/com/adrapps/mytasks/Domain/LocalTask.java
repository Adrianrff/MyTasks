package com.adrapps.mytasks.Domain;

import android.support.annotation.Nullable;

import com.adrapps.mytasks.Helpers.DateHelper;
import com.google.api.services.tasks.model.Task;

import java.io.Serializable;
import java.util.TimeZone;

/**
 * Created by Adrian Flores on 27/3/2017.
 */

public class LocalTask implements Serializable {

    private String id, title, selfLink, parent, position, notes, status, taskList, localSibling;
    private int moved, localDeleted, intId, syncStatus;
    private long serverModify,due,completed, localModify, reminder, reminderId;
    private boolean deleted,hidden;

    public LocalTask() {
    }

    public LocalTask (String taskTitle, long dueDate){
        title = taskTitle;
        this.due = dueDate;
        this.reminder = 0;
        this.reminderId = 0;
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
        this.syncStatus = Co.NOT_SYNCED;
        this.localDeleted = 0;
        this.moved = 0;
    }

    public LocalTask(Task task, String listId) {
        int offSet = TimeZone.getDefault().getRawOffset();
        this.id = task.getId();
        this.title = task.getTitle();
        this.selfLink = task.getSelfLink();
        this.parent = task.getParent();
        this.position = task.getPosition();
        this.notes = task.getNotes();
        this.status = task.getStatus();
        this.taskList = listId;
        this.serverModify = (task.getUpdated() == null) ? 0:task.getUpdated().getValue();
        this.due = (task.getDue() == null) ? 0:task.getDue().getValue();
        this.completed = (task.getCompleted() == null) ? 0:task.getCompleted().getValue();
        this.deleted = (task.getDeleted() == null) ? false:task.getDeleted();
        this.hidden = (task.getHidden() == null) ? false:task.getHidden();
        this.reminder = 0;
        this.reminderId = 0;
        this.syncStatus = Co.NOT_SYNCED;
        this.localDeleted = 0;
        this.moved = 0;
    }

    public static Task localTaskToApiTask (LocalTask lTask){
        Task task = new Task();
        task.setTitle(lTask.title);
        if (lTask.getNotes() != null) {
            task.setNotes(lTask.getNotes());
        }
        if (lTask.getDue() != 0) {
            task.setDue(DateHelper.millisecondsToDateTime(lTask.getDue()));
        }
        return task;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    ///-------------------SETTERS ----------------------//


    public void setMoved(@Nullable String localSibling) {
        this.moved = Co.MOVED;
        this.localSibling = localSibling == null ? null : localSibling;
    }

    public void setMoved(int moved) {
        this.moved = moved;
    }

    public void setLocalModify() {
        this.localModify = System.currentTimeMillis();
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

    public void setReminderNoID(long reminder) {
        this.reminder = reminder;
    }

    public void setReminderId (long reminderId){
        this.reminderId = reminderId;
    }

    public void setTaskList(String taskList) {
        this.taskList = taskList;
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

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
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


    public String getLocalSibling() {
        return localSibling;
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

    public void setLocalSibling(String localSibling) {
        this.localSibling = localSibling;
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

    public String getTaskList() {
        return taskList;
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

    public String getSelfLink() {
        return selfLink;
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

}
