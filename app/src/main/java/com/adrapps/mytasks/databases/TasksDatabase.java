package com.adrapps.mytasks.databases;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.views.MainActivity;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class TasksDatabase extends SQLiteOpenHelper {

   //---------DATABASE AND TABLE NAMES----------//
   private static final String DATABASE_NAME = "TasksDatabase";
   private static final String TABLE_NAME = "TasksTable";
   private static final int DATABASE_VERSION = 1;
   private static TasksDatabase tasksDB;

   //----------TABLE COLUMNS---------------//
   private static final String COL_INT_ID = "Int_Id";
   //    private static final String COL_SORT_ID = "Sort_Id";
   private static final String COL_ID = "Id";
   private static final String COL_LIST = "List";
   private static final String COL_TITLE = "Title";
   private static final String COL_SERVER_UPDATED = "serverUpdated";
   private static final String COL_PARENT = "Parent";
   private static final String COL_POSITION = "Position";
   private static final String COL_NOTES = "Notes";
   private static final String COL_STATUS = "Status";
   private static final String COL_DUE = "Due";
   private static final String COL_COMPLETED = "Completed";
   private static final String COL_DELETED = "Deleted";
   private static final String COL_HIDDEN = "Hidden";
   private static final String COL_REMINDER = "Reminder";
   private static final String COL_REMINDER_REPEAT_MODE = "Reminder_repeat_mode";
   private static final String COL_REMINDER_REPEAT_DAY = "Reminder_repeat_day";
   private static final String COL_REMINDER_ID = "ReminderID";
   private static final String COL_SYNC_STATUS = "Sync_status";
   private static final String COL_LOCAL_SIBLING = "local_sibling";
   private static final String COL_MOVED = "moved";
   private static final String COL_LOCAL_UPDATED = "local_updated";
   private static final String COL_LOCAL_DELETED = "loca_deleted";
   private static final String ORDER_ASC = " ASC";
   public static final String ORDER_DESC = " DESC";
   private SQLiteDatabase db;

   //---------ALL COLUMNS ARRAY----------//
   private static final String[] ALL_COLUMNS = {
         COL_INT_ID,
         COL_ID,
         COL_LIST,
         COL_TITLE,
         COL_SERVER_UPDATED,
         COL_PARENT,
         COL_POSITION,
         COL_NOTES,
         COL_STATUS,
         COL_DUE,
         COL_COMPLETED,
         COL_DELETED,
         COL_HIDDEN,
         COL_REMINDER,
         COL_REMINDER_REPEAT_MODE,
         COL_REMINDER_REPEAT_DAY,
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
               COL_TITLE + " text," +
               COL_NOTES + " text," +
               COL_DUE + " bigint," +
               COL_REMINDER + " bigint default 0," +
               COL_POSITION + " text default 000," +
               COL_SERVER_UPDATED + " bigint," +
               COL_LOCAL_UPDATED + " bigint," +
               COL_PARENT + " text," +
               COL_STATUS + " text," +
               COL_COMPLETED + " text," +
               COL_REMINDER_REPEAT_MODE + " int default 0," +
               COL_REMINDER_REPEAT_DAY + " int default -1," +
               COL_REMINDER_ID + " bigint," +
               COL_SYNC_STATUS + " int," +
               COL_LOCAL_SIBLING + " int default 0," +
               COL_MOVED + " int default 0," +
               COL_DELETED + " int," +
               COL_HIDDEN + " int," +
               COL_LIST + " text," +
               COL_ID + " text," +
               COL_LOCAL_DELETED + " int default 0)";
   private long offSet = TimeZone.getDefault().getRawOffset();
   private final BackupManager bm;

   public static synchronized TasksDatabase getInstance(Context context) {
      // Use the application context, which will ensure that you don't accidentally leak an Activity's context.
      if (tasksDB == null) {
         tasksDB = new TasksDatabase(context.getApplicationContext());
      }
      return tasksDB;
   }

   //----------CONSTRUCTOR--------------//
   private TasksDatabase(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
      bm = new BackupManager(context);
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

   private SQLiteDatabase getWritableDB() {
      if (db == null || !db.isOpen()) {
         db = getWritableDatabase();
      }
      return db;

   }

   private SQLiteDatabase getReadableDB() {
      if (db == null || !db.isOpen()) {
         db = getReadableDatabase();
      }
      return db;

   }


   ///********------------------------------OPERATIONS-----------------------------------******//

   public List<LocalTask> getLocalTasks() {
      List<LocalTask> tasks = new ArrayList<>();
      db = getWritableDB();
      Cursor cursor;
      try {
         synchronized (MainActivity.sDataLock) {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_POSITION + ORDER_ASC);
            if (cursor.getCount() != 0 && cursor.moveToFirst()) {
               do {
                  LocalTask task = new LocalTask();
                  task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
                  task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                  task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
                  task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
                  task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
                  task.setList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
                  task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                  task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
                  task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
                  task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
                  task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
                  task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
                  task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
                  task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
                  task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
                  task.setRepeatMode(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE)));
                  task.setRepeatDay(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_DAY)));
                  task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
                  task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
                  task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
                  task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
                  task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
                  tasks.add(task);
               } while (cursor.moveToNext());
            }
            cursor.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
         db.close();
         Log.d(TAG, "getLocalTasks: error");
      }
      db.close();
      return tasks;
   }

   public List<LocalTask> getTasksFromList(String listId) {
      List<LocalTask> tasks = new ArrayList<>();
      String selection = COL_LIST + " = ? ";
      String[] selectionArgs = {listId};
      db = getReadableDB();

      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         do {
            LocalTask task = new LocalTask();
            task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
            task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
            task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
            task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
            task.setList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
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
            task.setRepeatMode(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE)));
            task.setRepeatDay(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_DAY)));
            task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
            task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
            task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
            task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
            tasks.add(task);
         } while (cursor.moveToNext());
      }
      cursor.close();
      db.close();
      Log.d(TAG, "getTasksFromList: run");
      return tasks;
   }

   public List<LocalTask> getTasksFromListForAdapter(String listId) {
      List<LocalTask> tasks = new ArrayList<>();
      String selection = COL_LIST + " = ? ";
      String[] selectionArgs = {listId};
      db = getReadableDB();
      db.beginTransaction();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
      try {
         if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            do {
               if (cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)) != Co.LOCAL_DELETED) {
                  LocalTask task = new LocalTask();
                  task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
                  task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                  task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
                  task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
                  task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
                  task.setList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
                  task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                  task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
                  task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
                  task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
                  task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
                  task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
                  task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
                  task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
                  task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
                  task.setRepeatMode(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE)));
                  task.setRepeatDay(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_DAY)));
                  task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
                  task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
                  task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
                  task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
                  task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
                  tasks.add(task);
               }
            } while (cursor.moveToNext());
            db.setTransactionSuccessful();
         }
      } catch (Exception e) {
         e.printStackTrace();
         db.endTransaction();
         Log.d(TAG, "getTasksFromListForAdapter: error");
      } finally {
         db.endTransaction();
         cursor.close();
         db.close();
         Log.d(TAG, "getTasksFromListForAdapter: run");
      }
      db.close();
      return tasks;
   }

   public long addTaskToDataBase(LocalTask localTask) {
      db = getWritableDB();
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, localTask.getId());
      cv.put(COL_LIST, localTask.getList());
      cv.put(COL_TITLE, localTask.getTitle());
      cv.put(COL_SERVER_UPDATED, localTask.getServerModify());
      cv.put(COL_PARENT, localTask.getParent());
      cv.put(COL_POSITION, localTask.getPosition());
      cv.put(COL_NOTES, localTask.getNotes());
      cv.put(COL_STATUS, localTask.getStatus());
      cv.put(COL_DUE, localTask.getDue());
      cv.put(COL_COMPLETED, localTask.getCompleted());
      cv.put(COL_DELETED, (localTask.isDeleted()) ? 1 : 0);
      cv.put(COL_HIDDEN, (localTask.isHidden()) ? 1 : 0);
      cv.put(COL_REMINDER, localTask.getReminder());
      cv.put(COL_REMINDER_REPEAT_MODE, localTask.getRepeatMode());
      cv.put(COL_REMINDER_REPEAT_DAY, localTask.getRepeatDay());
      cv.put(COL_REMINDER_ID, localTask.getReminderId());
      cv.put(COL_SYNC_STATUS, Co.NOT_SYNCED);
      cv.put(COL_LOCAL_DELETED, localTask.getLocalDeleted());
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_LOCAL_SIBLING, localTask.getPreviousTask());
      cv.put(COL_MOVED, localTask.getMoved());
      long insertedRow = db.insert(TABLE_NAME, null, cv);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "addTaskToDataBase: run");
      return insertedRow;
   }

   public long addTaskFirstTimeFromServer(Task task, String listId) {
      db = getWritableDB();
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST, listId);
      cv.put(COL_TITLE, task.getTitle());
      cv.put(COL_SERVER_UPDATED, task.getUpdated() == null ? 0 : task.getUpdated().getValue());
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
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      long insertedRow = db.insert(TABLE_NAME, null, cv);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "addTaskFirstTimeFromServer: run");
      return insertedRow;
   }

   public void updateTasksFirstTime(List<LocalTask> tasks) {
      List<String> taskIds = new ArrayList<>();
      db = getWritableDB();
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, null, null, null, null, null);
      if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
         do {
            taskIds.add(cursor.getString(cursor.getColumnIndex(COL_ID)));
         } while (cursor.moveToNext());
         cursor.close();
      }
      ContentValues cv = new ContentValues();
      LocalTask currentTask;
      for (int i = 0; i < tasks.size(); i++) {
         currentTask = tasks.get(i);
         if (taskIds.contains(currentTask.getId())) {
            updateLocalTask(currentTask, false);
            continue;
         }
         cv.put(COL_ID, currentTask.getId());
         cv.put(COL_LIST, currentTask.getList());
         cv.put(COL_TITLE, currentTask.getTitle());
         cv.put(COL_SERVER_UPDATED, currentTask.getServerModify());
         cv.put(COL_PARENT, currentTask.getParent());
         cv.put(COL_POSITION, currentTask.getPosition());
         cv.put(COL_NOTES, currentTask.getNotes());
         cv.put(COL_STATUS, currentTask.getStatus());
         cv.put(COL_DUE, currentTask.getDue());
         cv.put(COL_COMPLETED, currentTask.getCompleted());
         cv.put(COL_DELETED, (currentTask.isDeleted()) ? 1 : 0);
         cv.put(COL_HIDDEN, (currentTask.isHidden()) ? 1 : 0);
//            cv.put(COL_REMINDER, currentTask.getReminder());
//            cv.put(COL_REMINDER_REPEAT_MODE, currentTask.getRepeatMode());
//            cv.put(COL_REMINDER_ID, currentTask.getReminderId());
         cv.put(COL_SYNC_STATUS, currentTask.getSyncStatus());
         cv.put(COL_LOCAL_UPDATED, currentTask.getLocalModify());
         cv.put(COL_LOCAL_SIBLING, currentTask.getPreviousTask());
         cv.put(COL_LOCAL_DELETED, currentTask.getLocalDeleted());
         cv.put(COL_MOVED, currentTask.getMoved());
         db.insert(TABLE_NAME, null, cv);
      }
      Log.d(TAG, "updateTasksFirstTime: run");
      db.close();
   }

   public void deleteTask(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      int deletedRow = db.delete(TABLE_NAME, selection, selectionArgs);
      bm.dataChanged();
      db.close();
   }

   public int updateTaskStatus(int intId, String newStatus) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_STATUS, newStatus);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "updateTaskStatus: run");
      return updatedRow;
   }

   public int markDeleted(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_DELETED, Co.LOCAL_DELETED);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "markDeleted: run");
      return updatedRow;
   }

   public int updateSyncStatus(int newStatus, int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_SYNC_STATUS, newStatus);
      if (newStatus == Co.SYNCED) {
         cv.put(COL_MOVED, Co.NOT_MOVED);
         cv.put(COL_DELETED, Co.NOT_DELETED);
      }
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "updateSyncStatus: run");
      return updatedRow;
   }

   private LocalTask getTaskByTaskId(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      LocalTask task = new LocalTask();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, null);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
         task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
         task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
         task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
         task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
         task.setList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
         task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
         task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
         task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
         task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
         task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
         task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
         task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
         task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
         task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
         task.setRepeatDay(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_DAY)));
         task.setRepeatMode(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE)));
         task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
         task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
         task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
         task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
         task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
         db.close();
         cursor.close();
         return task;
      } else {
         cursor.close();
         db.close();
         Log.d(TAG, "getTaskByTaskId: run");
         return null;
      }
   }

   public long updateTaskReminder(String taskId, long reminder) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_REMINDER, reminder);
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      return updatedRow;
   }

   public long updateTaskReminder(int intId, long reminder, int repeatMode) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_REMINDER, reminder);
      cv.put(COL_REMINDER_REPEAT_MODE, repeatMode);

//      cv.put(COL_REMINDER_REPEAT_DAY, tasks.get(i).getRepeatDay());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "updateTaskReminder: run");
      return updatedRow;
   }

   public int updateLocalTask(LocalTask task, boolean updateReminders) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(task.getIntId())};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST, task.getList());
      cv.put(COL_TITLE, task.getTitle());
      cv.put(COL_SERVER_UPDATED, task.getServerModify());
      cv.put(COL_PARENT, task.getParent());
      cv.put(COL_POSITION, task.getPosition());
      cv.put(COL_NOTES, task.getNotes());
      cv.put(COL_STATUS, task.getStatus());
      cv.put(COL_DUE, task.getDue());
      cv.put(COL_COMPLETED, task.getCompleted());
      cv.put(COL_DELETED, (task.isDeleted()) ? 1 : 0);
      cv.put(COL_HIDDEN, (task.isHidden()) ? 1 : 0);
      if (updateReminders) {
         cv.put(COL_REMINDER, task.getReminder());
         cv.put(COL_REMINDER_ID, task.getReminderId());
         cv.put(COL_REMINDER_REPEAT_MODE, task.getRepeatMode());
         cv.put(COL_REMINDER_REPEAT_DAY, task.getRepeatDay());
      }
      cv.put(COL_SYNC_STATUS, task.getSyncStatus());
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_LOCAL_SIBLING, task.getPreviousTask());
      cv.put(COL_LOCAL_DELETED, task.getLocalDeleted());
      cv.put(COL_MOVED, task.getMoved());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "updateLocalTask: run");
      return updatedRow;
   }

   public void updateExistingTaskFromServerTask(Task task, String listId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {task.getId()};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST, listId);
      cv.put(COL_TITLE, task.getTitle());
      cv.put(COL_SERVER_UPDATED, task.getUpdated() != null ? task.getUpdated().getValue() : 0);
      cv.put(COL_PARENT, task.getParent());
      cv.put(COL_POSITION, task.getPosition());
      cv.put(COL_NOTES, task.getNotes());
      cv.put(COL_STATUS, task.getStatus());
      cv.put(COL_DUE, task.getDue() != null ? task.getDue().getValue() - offSet : 0);
      cv.put(COL_COMPLETED, task.getCompleted() != null ? task.getCompleted().getValue() : 0);
      cv.put(COL_DELETED, task.getDeleted() == null ? 0 : 1);
      cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : 1);
      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "updateExistingTaskFromServerTask: run");
   }

   public void updateExistingTaskFromLocalTask(LocalTask task, String listId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(task.getIntId())};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST, listId);
      cv.put(COL_TITLE, task.getTitle());
      cv.put(COL_SERVER_UPDATED, task.getServerModify());
      cv.put(COL_PARENT, task.getParent());
      cv.put(COL_POSITION, task.getPosition());
      cv.put(COL_NOTES, task.getNotes());
      cv.put(COL_STATUS, task.getStatus());
      cv.put(COL_DUE, task.getDue());
      cv.put(COL_COMPLETED, task.getCompleted());
      cv.put(COL_REMINDER, task.getReminder());
      cv.put(COL_REMINDER_REPEAT_MODE, task.getRepeatMode());
      cv.put(COL_REMINDER_REPEAT_DAY, task.getRepeatDay());
      cv.put(COL_REMINDER_ID, task.getReminderId());
      cv.put(COL_SYNC_STATUS, task.getSyncStatus());
      cv.put(COL_LOCAL_SIBLING, task.getPreviousTask());
      cv.put(COL_MOVED, task.getMoved());
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_LOCAL_DELETED, task.getLocalDeleted());
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "updateExistingTaskFromLocalTask: run");
   }

   public LocalTask updateNewlyCreatedTask(Task task, String listId, String intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {intId};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST, listId);
      cv.put(COL_TITLE, task.getTitle());
      cv.put(COL_SERVER_UPDATED, task.getUpdated() != null ? task.getUpdated().getValue() : 0);
      cv.put(COL_PARENT, task.getParent());
      cv.put(COL_POSITION, task.getPosition());
      cv.put(COL_NOTES, task.getNotes());
      cv.put(COL_STATUS, task.getStatus());
      cv.put(COL_DUE, task.getDue() != null ? task.getDue().getValue() - offSet : 0);
      cv.put(COL_COMPLETED, task.getCompleted() != null ? task.getCompleted().getValue() : 0);
      cv.put(COL_DELETED, task.getDeleted() == null ? 0 : 1);
      cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : 1);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      int row = db.update(TABLE_NAME, cv, selection, selectionArgs);
      db.close();
      bm.dataChanged();
      Log.d(TAG, "updateNewlyCreatedTask: run");
      return getTaskByTaskId(task.getId());
   }

   public void updatePosition(Task task) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {task.getId()};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_POSITION, task.getPosition());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      bm.dataChanged();
      db.close();
   }

   public String getTaskIdByIntId(int id) {
      if (id == 0) {
         return null;
      }
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(id)};
      String taskId = null;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, selection, selectionArgs, null, null, null);
      if (cursor != null && cursor.getCount() > 0) {
         cursor.moveToFirst();
         taskId = cursor.getString(cursor.getColumnIndex(COL_ID));
         cursor.close();
      }
      db.close();
      Log.d(TAG, "getTaskIdByIntId: run");
      return taskId;
   }

   public int getIntIdByTaskId(String id) {
      if (id == null) {
         return -1;
      }
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {id};
      int intId = -1;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_INT_ID}, selection, selectionArgs, null, null, null);
      if (cursor != null && cursor.getCount() > 0) {
         cursor.moveToFirst();
         intId = cursor.getInt(cursor.getColumnIndex(COL_INT_ID));
         cursor.close();
      }
      db.close();
      Log.d(TAG, "getIntIdByTaskId: run");
      return intId;
   }

   public LocalTask getTask(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      LocalTask task = new LocalTask();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, null);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
         task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
         task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
         task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
         task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
         task.setList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
         task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
         task.setServerModify(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
         task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
         task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
         task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
         task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
         task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
         task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
         task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
         task.setRepeatMode(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE)));
         task.setRepeatDay(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_DAY)));
         task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
         task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
         task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
         task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
         task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
         db.close();
         cursor.close();
         Log.d(TAG, "getTask: run");
         return task;
      } else {
         cursor.close();
         db.close();
         Log.d(TAG, "getTask: run1");
         return null;
      }
   }

   public void updateNewTasksInBulk(HashMap<Task, LocalTask> map) {
      db = getWritableDB();
      db.beginTransaction();
      LocalTask localTask;
      List<Task> serverTasks = new ArrayList<>(map.keySet());
      String selection = COL_INT_ID + " = ? ";
      try {
         for (Task task : serverTasks) {
            localTask = map.get(task);
            String[] selectionArgs = {String.valueOf(localTask.getIntId())};
            ContentValues cv = new ContentValues();
            cv.put(COL_ID, task.getId());
            cv.put(COL_LIST, localTask.getList());
            cv.put(COL_TITLE, task.getTitle());
            cv.put(COL_SERVER_UPDATED, task.getUpdated() != null ? task.getUpdated().getValue() : 0);
            cv.put(COL_PARENT, task.getParent());
            cv.put(COL_POSITION, task.getPosition());
            cv.put(COL_NOTES, task.getNotes());
            cv.put(COL_STATUS, task.getStatus());
            cv.put(COL_DUE, task.getDue() != null ? task.getDue().getValue() - offSet : 0);
            cv.put(COL_COMPLETED, task.getCompleted() != null ? task.getCompleted().getValue() : 0);
            cv.put(COL_DELETED, task.getDeleted() == null ? 0 : 1);
            cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : 1);
            cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
            cv.put(COL_SYNC_STATUS, Co.SYNCED);
            int row = db.update(TABLE_NAME, cv, selection, selectionArgs);
         }
         db.setTransactionSuccessful();
      } catch (Exception e) {
         e.printStackTrace();
         Log.d(TAG, "updateNewTasksInBulk: error");
      } finally {
         db.endTransaction();
         db.close();
         Log.d(TAG, "updateNewTasksInBulk: run");
         bm.dataChanged();
      }
   }

   public void updatePositions(List<Task> tasks) {
      db = getWritableDB();
      db.beginTransaction();
      String selection = COL_ID + " = ? ";
      try {
         for (Task task : tasks) {
            String[] selectionArgs = {task.getId()};
            ContentValues cv = new ContentValues();
            cv.put(COL_SERVER_UPDATED, task.getUpdated() != null ? task.getUpdated().getValue() : 0);
            cv.put(COL_POSITION, task.getPosition());
            cv.put(COL_MOVED, Co.NOT_MOVED);
            cv.put(COL_STATUS, Co.SYNCED);
            int row = db.update(TABLE_NAME, cv, selection, selectionArgs);
         }
         db.setTransactionSuccessful();
      } catch (Exception e) {
         e.printStackTrace();
         Log.d(TAG, "updatePositions: error");
      } finally {
         db.endTransaction();
         bm.dataChanged();
         db.close();
         Log.d(TAG, "updatePositions: run");
      }
   }
}


