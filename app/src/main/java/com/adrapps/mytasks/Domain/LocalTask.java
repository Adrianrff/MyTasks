package com.adrapps.mytasks.Domain;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.adrapps.mytasks.Helpers.DateHelper;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.io.Serializable;
import java.util.TimeZone;

/**
 * Created by Adrian Flores on 27/3/2017.
 */

public class LocalTask implements Serializable {

    private String taskId;
    private String title;
    private String selfLink;
    private String parent;
    private String position;
    private String notes;
    private String status;
    private String taskList;
    private int intId;
    private int syncStatus;
    private long reminder;
    private long reminderId;
    private long updated,due,completed;
    private boolean deleted,hidden;
    private int offSet = TimeZone.getDefault().getRawOffset();

    public LocalTask() {
    }

    public LocalTask (String taskTitle, long dueDate){
        title = taskTitle;
        this.due = dueDate;
        this.reminder = 0;
        this.reminder = 0;
        this.syncStatus = Co.NOT_SYNCED;
    }

    public LocalTask (String taskTitle, long dueDate, String notes){
        title = taskTitle;
        this.due = dueDate;
        this.notes = notes;
        this.reminder = 0;
        this.reminderId = 0;
        this.syncStatus = Co.NOT_SYNCED;
    }

    public LocalTask (String taskTitle, long dueDate, String notes, long reminder){
        title = taskTitle;
        this.due = dueDate;
        this.notes = notes;
        this.reminder = reminder;
        this.reminderId = System.currentTimeMillis();
        this.syncStatus = Co.NOT_SYNCED;
    }

    public LocalTask(Task task, String listId) {
        this.taskId = task.getId();
        this.title = task.getTitle();
        this.selfLink = task.getSelfLink();
        this.parent = task.getParent();
        this.position = task.getPosition();
        this.notes = task.getNotes();
        this.status = task.getStatus();
        this.taskList = listId;
        this.updated = (task.getUpdated() == null) ? 0:task.getUpdated().getValue();
        this.due = (task.getDue() == null) ? 0:task.getDue().getValue() - offSet;
        this.completed = (task.getCompleted() == null) ? 0:task.getCompleted().getValue();
        this.deleted = (task.getDeleted() == null) ? false:task.getDeleted();
        this.hidden = (task.getHidden() == null) ? false:task.getHidden();
        this.reminder = 0;
        this.reminderId = 0;
        this.syncStatus = Co.NOT_SYNCED;
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

    ///-------------------SETTERS ----------------------//


    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public void setReminder(long reminder) {
        this.reminder = reminder;
        this.reminderId = System.currentTimeMillis();
    }

    public void setReminderDontSetID(long reminder) {
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

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public void setUpdated(long updated) {
        this.updated = updated;
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

    public String getTaskId() {
        return taskId;
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

    public long getUpdated() {
        return updated;
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
