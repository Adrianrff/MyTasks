package com.adrapps.mytasks;

import com.google.api.services.tasks.model.Task;

import java.util.TimeZone;

/**
 * Created by Adrian Flores on 27/3/2017.
 */

public class LocalTask {


    private String taskId,title,selfLink,parent,position,notes,status, taskList;
    private int intId;
    private long updated,due,completed;
    private boolean deleted,hidden;
    private int offset = TimeZone.getDefault().getRawOffset();

    public LocalTask() {
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
        this.updated = (task.getUpdated() == null) ? 0:task.getUpdated().getValue() + offset;
        this.due = (task.getDue() == null) ? 0:task.getDue().getValue() + offset;
        this.completed = (task.getCompleted() == null) ? 0:task.getCompleted().getValue() + offset;
        this.deleted = (task.getDeleted() == null) ? false:task.getDeleted();
        this.hidden = (task.getHidden() == null) ? false:task.getHidden();
    }

    public LocalTask(String taskId, String title, String selfLink, String parent,
                     String position, String notes, String status,
                     long updated, long due, long completed, boolean deleted,
                     boolean hidden, String taskList) {
        this.taskId = taskId;
        this.title = title;
        this.selfLink = selfLink;
        this.parent = parent;
        this.position = position;
        this.notes = notes;
        this.status = status;
        this.taskList = taskList;
        this.updated = updated;
        this.due = due;
        this.completed = completed;
        this.deleted = deleted;
        this.hidden = hidden;
    }


    ///-------------------SETTERS ----------------------//


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
