package com.adrapps.mytasks.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adrapps.mytasks.Domain.Co;
import com.adrapps.mytasks.Domain.LocalTask;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class TasksDataBase extends SQLiteOpenHelper {

    //---------DATABASE AND TABLE NAMES----------//
    private static final String DATABASE_NAME = "TasksDataBase.db";
    private static final String TABLE_NAME = "TasksTable";

    //----------TABLE COLUMNS---------------//
    public static final String COL_INT_ID = "Int_Id";
    //    private static final String COL_SORT_ID = "Sort_Id";
    public static final String COL_ID = "Id";
    public static final String COL_LIST = "List";
    public static final String COL_TITLE = "Title";
    public static final String COL_SERVER_UPDATED = "serverUpdated";
    public static final String COL_SELFLINK = "Selflink";
    public static final String COL_PARENT = "Parent";
    public static final String COL_POSITION = "Position";
    public static final String COL_NOTES = "Notes";
    public static final String COL_STATUS = "Status";
    public static final String COL_DUE = "Due";
    public static final String COL_COMPLETED = "Completed";
    public static final String COL_DELETED = "Deleted";
    public static final String COL_HIDDEN = "Hidden";
    public static final String COL_REMINDER = "Reminder";
    public static final String COL_REMINDER_ID = "ReminderID";
    public static final String COL_SYNC_STATUS = "Sync_status";
    private static final String COL_LOCAL_SIBLING = "local_sibling";
    private static final String COL_MOVED = "moved";
    private static final String COL_LOCAL_UPDATED = "local_updated";
    private static final String COL_LOCAL_DELETED = "loca_deleted";
    public static final String ORDER_ASC = " ASC";
    public static final String ORDER_DESC = " DESC";

    //---------ALL COLUMNS ARRAY----------//
    private static final String[] ALL_COLUMNS = {
            COL_INT_ID,
            COL_ID,
            COL_LIST,
            COL_TITLE,
            COL_SERVER_UPDATED,
            COL_SELFLINK,
            COL_PARENT,
            COL_POSITION,
            COL_NOTES,
            COL_STATUS,
            COL_DUE,
            COL_COMPLETED,
            COL_DELETED,
            COL_HIDDEN,
            COL_REMINDER,
            COL_REMINDER_ID,
            COL_SYNC_STATUS,
            COL_LOCAL_SIBLING,
            COL_MOVED,
            COL_LOCAL_UPDATED,
            COL_LOCAL_DELETED};

    //-----------CREATE TABLE STATEMENT--------//
    private static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " +
                    COL_INT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ID + " text," +
                    COL_LIST + " text," +
                    COL_TITLE + " text," +
                    COL_SERVER_UPDATED + " bigint," +
                    COL_SELFLINK + " text," +
                    COL_PARENT + " text," +
                    COL_POSITION + " text," +
                    COL_NOTES + " text," +
                    COL_STATUS + " text," +
                    COL_DUE + " bigint," +
                    COL_COMPLETED + " text," +
                    COL_DELETED + " int," +
                    COL_HIDDEN + " int," +
                    COL_REMINDER + " bigint," +
                    COL_REMINDER_ID + " bigint," +
                    COL_SYNC_STATUS + " int," +
                    COL_LOCAL_SIBLING + " text," +
                    COL_MOVED + " int," +
                    COL_LOCAL_UPDATED + " bigint," +
                    COL_LOCAL_DELETED + " int)";
    private long offSet = TimeZone.getDefault().getRawOffset();


    //----------CONSTRUCTOR--------------//
    public TasksDataBase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    ///********------------------------------OPERATIONS-----------------------------------******//

    public List<LocalTask> getLocalTasks() {
        List<LocalTask> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_POSITION + ORDER_ASC);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            do {
                LocalTask task = new LocalTask();
                task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                task.setSelfLink(cursor.getString(cursor.getColumnIndex(COL_SELFLINK)));
                task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
                task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
                task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
                task.setTaskList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
                task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
                task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
                task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
                task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
                task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
                task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
                task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
                task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
                task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
                task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
                task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
                task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
                task.setLocalSibling(cursor.getString(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    public List<LocalTask> getTasksFromLlist(String listId) {
        List<LocalTask> tasks = new ArrayList<>();
        String selection = COL_LIST + " = ? ";
        String[] selectionArgs = {listId};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            do {
                LocalTask task = new LocalTask();
                task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                task.setSelfLink(cursor.getString(cursor.getColumnIndex(COL_SELFLINK)));
                task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
                task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
                task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
                task.setTaskList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
                task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
                task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
                task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
                task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
                task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
                task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
                task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
                task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
                task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
                task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
                task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
                task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
                task.setLocalSibling(cursor.getString(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }


    public List<LocalTask> getTasksFromListForAdapter(String listId) {
        List<LocalTask> tasks = new ArrayList<>();
        String selection = COL_LIST + " = ? ";
        String[] selectionArgs = {listId};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            do {
                if (cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)) != Co.LOCAL_DELETED) {
                    LocalTask task = new LocalTask();
                    task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
                    task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                    task.setSelfLink(cursor.getString(cursor.getColumnIndex(COL_SELFLINK)));
                    task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
                    task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
                    task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
                    task.setTaskList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
                    task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                    task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
                    task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
                    task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
                    task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
                    task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
                    task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
                    task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
                    task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
                    task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
                    task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
                    task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
                    task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
                    task.setLocalSibling(cursor.getString(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
                    tasks.add(task);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }


    public long addTask(LocalTask localTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, localTask.getId());
        cv.put(COL_LIST, localTask.getTaskList());
        cv.put(COL_TITLE, localTask.getTitle());
        cv.put(COL_SERVER_UPDATED, localTask.getServerModify());
        cv.put(COL_SELFLINK, localTask.getSelfLink());
        cv.put(COL_PARENT, localTask.getParent());
        cv.put(COL_POSITION, localTask.getPosition());
        cv.put(COL_NOTES, localTask.getNotes());
        cv.put(COL_STATUS, localTask.getStatus());
        cv.put(COL_DUE, localTask.getDue());
        cv.put(COL_COMPLETED, localTask.getCompleted());
        cv.put(COL_DELETED, (localTask.isDeleted()) ? 1 : 0);
        cv.put(COL_HIDDEN, (localTask.isHidden()) ? 1 : 0);
        cv.put(COL_REMINDER, localTask.getReminder());
        cv.put(COL_REMINDER_ID, localTask.getReminderId());
        cv.put(COL_SYNC_STATUS, localTask.getSyncStatus());
        cv.put(COL_LOCAL_DELETED, localTask.getLocalDeleted());
        cv.put(COL_LOCAL_UPDATED, localTask.getLocalModify());
        cv.put(COL_LOCAL_SIBLING, localTask.getLocalSibling());
        cv.put(COL_MOVED, localTask.getMoved());
        long insertedRow = db.insert(TABLE_NAME, null, cv);
        db.close();
        return insertedRow;
    }

    public long addTaskFirstTimeFromServer(Task task, String listId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, task.getId());
        cv.put(COL_LIST, listId);
        cv.put(COL_TITLE, task.getTitle());
        cv.put(COL_SERVER_UPDATED, task.getUpdated() == null ? 0 : task.getUpdated().getValue());
        cv.put(COL_SELFLINK, task.getSelfLink());
        cv.put(COL_PARENT, task.getParent());
        cv.put(COL_POSITION, task.getPosition());
        cv.put(COL_NOTES, task.getNotes());
        cv.put(COL_STATUS, task.getStatus());
        cv.put(COL_DUE, task.getDue() == null ? 0 : task.getDue().getValue() - offSet);
        cv.put(COL_COMPLETED, task.getCompleted() == null ? 0 : task.getCompleted().getValue());
        cv.put(COL_DELETED, task.getDeleted() == null ? 0 : task.getDeleted() ? 1 : 0);
        cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : task.getHidden() ? 1 : 0);
        cv.put(COL_REMINDER, 0);
        cv.put(COL_REMINDER_ID, 0);
        cv.put(COL_SYNC_STATUS, Co.SYNCED);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 15000);
        long insertedRow = db.insert(TABLE_NAME, null, cv);
        db.close();
        return insertedRow;
    }

    public void addTasks(List<LocalTask> tasks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (int i = 0; i < tasks.size(); i++) {
            cv.put(COL_ID, tasks.get(i).getId());
            cv.put(COL_LIST, tasks.get(i).getTaskList());
            cv.put(COL_TITLE, tasks.get(i).getTitle());
            cv.put(COL_SERVER_UPDATED, tasks.get(i).getServerModify());
            cv.put(COL_SELFLINK, tasks.get(i).getSelfLink());
            cv.put(COL_PARENT, tasks.get(i).getParent());
            cv.put(COL_POSITION, tasks.get(i).getPosition());
            cv.put(COL_NOTES, tasks.get(i).getNotes());
            cv.put(COL_STATUS, tasks.get(i).getStatus());
            cv.put(COL_DUE, tasks.get(i).getDue());
            cv.put(COL_COMPLETED, tasks.get(i).getCompleted());
            cv.put(COL_DELETED, (tasks.get(i).isDeleted()) ? 1 : 0);
            cv.put(COL_HIDDEN, (tasks.get(i).isHidden()) ? 1 : 0);
            cv.put(COL_REMINDER, tasks.get(i).getReminder());
            cv.put(COL_REMINDER_ID, tasks.get(i).getReminder());
            cv.put(COL_SYNC_STATUS, tasks.get(i).getSyncStatus());
            cv.put(COL_LOCAL_UPDATED, tasks.get(i).getLocalModify());
            cv.put(COL_LOCAL_SIBLING, tasks.get(i).getLocalSibling());
            cv.put(COL_LOCAL_DELETED, tasks.get(i).getLocalDeleted());
            cv.put(COL_MOVED, tasks.get(i).getMoved());
            db.insert(TABLE_NAME, null, cv);
        }
        db.close();
    }

    public void updateTasks(List<LocalTask> tasks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        onUpgrade(db, 1, 1);
        for (int i = 0; i < tasks.size(); i++) {
            cv.put(COL_ID, tasks.get(i).getId());
            cv.put(COL_LIST, tasks.get(i).getTaskList());
            cv.put(COL_TITLE, tasks.get(i).getTitle());
            cv.put(COL_SERVER_UPDATED, tasks.get(i).getServerModify());
            cv.put(COL_SELFLINK, tasks.get(i).getSelfLink());
            cv.put(COL_PARENT, tasks.get(i).getParent());
            cv.put(COL_POSITION, tasks.get(i).getPosition());
            cv.put(COL_NOTES, tasks.get(i).getNotes());
            cv.put(COL_STATUS, tasks.get(i).getStatus());
            cv.put(COL_DUE, tasks.get(i).getDue());
            cv.put(COL_COMPLETED, tasks.get(i).getCompleted());
            cv.put(COL_DELETED, (tasks.get(i).isDeleted()) ? 1 : 0);
            cv.put(COL_HIDDEN, (tasks.get(i).isHidden()) ? 1 : 0);
            cv.put(COL_REMINDER, tasks.get(i).getReminder());
            cv.put(COL_REMINDER_ID, tasks.get(i).getReminder());
            cv.put(COL_SYNC_STATUS, tasks.get(i).getSyncStatus());
            cv.put(COL_LOCAL_UPDATED, tasks.get(i).getLocalModify());
            cv.put(COL_LOCAL_SIBLING, tasks.get(i).getLocalSibling());
            cv.put(COL_LOCAL_DELETED, tasks.get(i).getLocalDeleted());
            cv.put(COL_MOVED, tasks.get(i).getMoved());
            db.insert(TABLE_NAME, null, cv);
        }
        db.close();
    }


    public int deleteTask(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        int deletedRow = db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
        return deletedRow;
    }

    public long getTaskReminder(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long taskReminder = 0;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_REMINDER}, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            taskReminder = cursor.getLong(cursor.getColumnIndex(COL_REMINDER));
            cursor.close();
        }
        db.close();
        return taskReminder;
    }

    public int updateTaskStatus(String taskId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS, newStatus);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public int markDeleted(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_LOCAL_DELETED, Co.LOCAL_DELETED);
        cv.put(COL_SYNC_STATUS, Co.EDITED_NOT_SYNCED);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 10000);
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public int updateSyncStatus(int newStatus, String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_SYNC_STATUS, newStatus);
        if (newStatus == Co.SYNCED) {
            cv.put(COL_MOVED, Co.NOT_MOVED);
            cv.put(COL_DELETED, Co.NOT_DELETED);
            cv.putNull(COL_LOCAL_SIBLING);
        }
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 10000);
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public int updateLocalDeleted(int localDeleted, String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_LOCAL_DELETED, localDeleted);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 10000);
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public int updateMoved(int moved, String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_MOVED, moved);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public int updateLocalSibling(String taskId, String localSibling) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        if (localSibling.equals(Co.TASK_MOVED_TO_FIRST)) {
            cv.putNull(COL_LOCAL_SIBLING);
        } else {
            cv.put(COL_LOCAL_SIBLING, localSibling);
        }
        cv.put(COL_MOVED, Co.MOVED);
        cv.put(COL_SYNC_STATUS, Co.EDITED_NOT_SYNCED);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public int updateLocalModify(long localModify, String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_LOCAL_UPDATED, localModify);
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public boolean taskExistsInDB(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        db.close();
        cursor.close();
        return exists;
    }

    public LocalTask getTask(String taskId) {
        Log.d("Server Task ID", taskId);
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        LocalTask task = new LocalTask();
        Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, null);
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
            task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            task.setSelfLink(cursor.getString(cursor.getColumnIndex(COL_SELFLINK)));
            task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
            task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
            task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
            task.setTaskList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
            task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
            task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
            task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
            task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
            task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
            task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
            task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
            task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
            task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
            task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
            task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
            task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
            task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
            task.setLocalSibling(cursor.getString(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
            db.close();
            cursor.close();
            return task;
        } else {
            cursor.close();
            return null;
        }
    }

    public long updateTaskReminder(String taskId, long reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
        cv.put(COL_REMINDER, reminder);
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }

    public long getTaskReminderId(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long reminderId = 0;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_REMINDER_ID}, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            reminderId = cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID));
            cursor.close();
        }
        db.close();
        return reminderId;
    }

    public long getDeleted(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long deleted = 0;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_DELETED}, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            deleted = cursor.getLong(cursor.getColumnIndex(COL_DELETED));
            cursor.close();
        }
        db.close();
        return deleted;
    }

    public long getLocalModify(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long modify = 0;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_LOCAL_UPDATED}, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            modify = cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED));
            cursor.close();
        }
        db.close();
        return modify;
    }

    public long getLocalSIbling(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long modify = 0;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_LOCAL_SIBLING}, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            modify = cursor.getLong(cursor.getColumnIndex(COL_LOCAL_SIBLING));
            cursor.close();
        }
        db.close();
        return modify;
    }

    public long getMoved(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long modify = 0;
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_MOVED}, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            modify = cursor.getLong(cursor.getColumnIndex(COL_MOVED));
            cursor.close();
        }
        db.close();
        return modify;
    }


    public int updateLocalTask(LocalTask modifiedTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {modifiedTask.getId()};
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, modifiedTask.getId());
        cv.put(COL_LIST, modifiedTask.getTaskList());
        cv.put(COL_TITLE, modifiedTask.getTitle());
        cv.put(COL_SERVER_UPDATED, modifiedTask.getServerModify());
        cv.put(COL_SELFLINK, modifiedTask.getSelfLink());
        cv.put(COL_PARENT, modifiedTask.getParent());
        cv.put(COL_POSITION, modifiedTask.getPosition());
        cv.put(COL_NOTES, modifiedTask.getNotes());
        cv.put(COL_STATUS, modifiedTask.getStatus());
        cv.put(COL_DUE, modifiedTask.getDue());
        cv.put(COL_COMPLETED, modifiedTask.getCompleted());
        cv.put(COL_DELETED, (modifiedTask.isDeleted()) ? 1 : 0);
        cv.put(COL_HIDDEN, (modifiedTask.isHidden()) ? 1 : 0);
        cv.put(COL_REMINDER, modifiedTask.getReminder());
        cv.put(COL_REMINDER_ID, modifiedTask.getReminder());
        cv.put(COL_SYNC_STATUS, modifiedTask.getSyncStatus());
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 10000);
        cv.put(COL_LOCAL_SIBLING, modifiedTask.getLocalSibling());
        cv.put(COL_LOCAL_DELETED, modifiedTask.getLocalDeleted());
        cv.put(COL_MOVED, modifiedTask.getMoved());
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
        return updatedRow;
    }


    public void updateTask(Task task, String listId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {task.getId()};
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, task.getId());
        cv.put(COL_LIST, listId);
        cv.put(COL_TITLE, task.getTitle());
        cv.put(COL_SERVER_UPDATED, task.getUpdated() != null ? task.getUpdated().getValue() : 0);
        cv.put(COL_SELFLINK, task.getSelfLink());
        cv.put(COL_PARENT, task.getParent());
        cv.put(COL_POSITION, task.getPosition());
        cv.put(COL_NOTES, task.getNotes());
        cv.put(COL_STATUS, task.getStatus());
        cv.put(COL_DUE, task.getDue() != null ? task.getDue().getValue() - offSet : 0);
        cv.put(COL_COMPLETED, task.getCompleted() != null ? task.getCompleted().getValue() : 0);
        cv.put(COL_DELETED, task.getDeleted() == null ? 0 : 1);
        cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : 1);
        cv.put(COL_SYNC_STATUS, Co.SYNCED);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 10000);
        db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
    }

    public void updateNewLyCreatedTask(Task task, String listId, String intId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_INT_ID + " = ? ";
        String[] selectionArgs = {intId};
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, task.getId());
        cv.put(COL_LIST, listId);
        cv.put(COL_TITLE, task.getTitle());
        cv.put(COL_SERVER_UPDATED, task.getUpdated() != null ? task.getUpdated().getValue() : 0);
        cv.put(COL_SELFLINK, task.getSelfLink());
        cv.put(COL_PARENT, task.getParent());
        cv.put(COL_POSITION, task.getPosition());
        cv.put(COL_NOTES, task.getNotes());
        cv.put(COL_STATUS, task.getStatus());
        cv.put(COL_DUE, task.getDue() != null ? task.getDue().getValue() - offSet : 0);
        cv.put(COL_COMPLETED, task.getCompleted() != null ? task.getCompleted().getValue() : 0);
        cv.put(COL_DELETED, task.getDeleted() == null ? 0 : 1);
        cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : 1);
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 10000);
        cv.put(COL_SYNC_STATUS, Co.SYNCED);
        int row = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();
    }

    public void setTemporaryPosition(String taskId, String newTaskTempPos) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
        cv.put(COL_POSITION, newTaskTempPos);
        int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
        db.close();

    }
}
