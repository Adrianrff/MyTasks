package com.adrapps.mytasks.databases;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.adrapps.mytasks.views.MainActivity;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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
      }
      ////db.close();
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
            task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
            task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
            task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
            task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
            tasks.add(task);
         } while (cursor.moveToNext());
      }
      cursor.close();
      ////db.close();
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
      } finally {
         db.endTransaction();
         cursor.close();
      }
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
      cv.put(COL_REMINDER_ID, localTask.getReminderId());
      cv.put(COL_SYNC_STATUS, Co.NOT_SYNCED);
      cv.put(COL_LOCAL_DELETED, localTask.getLocalDeleted());
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_LOCAL_SIBLING, localTask.getPreviousTask());
      cv.put(COL_MOVED, localTask.getMoved());
      long insertedRow = db.insert(TABLE_NAME, null, cv);
      //db.close();
      bm.dataChanged();
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
      //db.close();
      bm.dataChanged();
      return insertedRow;
   }

   public void addTasks(List<LocalTask> tasks) {
      db = getWritableDB();
      ContentValues cv = new ContentValues();
      for (int i = 0; i < tasks.size(); i++) {
         cv.put(COL_ID, tasks.get(i).getId());
         cv.put(COL_LIST, tasks.get(i).getList());
         cv.put(COL_TITLE, tasks.get(i).getTitle());
         cv.put(COL_SERVER_UPDATED, tasks.get(i).getServerModify());
         cv.put(COL_PARENT, tasks.get(i).getParent());
         cv.put(COL_POSITION, tasks.get(i).getPosition());
         cv.put(COL_NOTES, tasks.get(i).getNotes());
         cv.put(COL_STATUS, tasks.get(i).getStatus());
         cv.put(COL_DUE, tasks.get(i).getDue());
         cv.put(COL_COMPLETED, tasks.get(i).getCompleted());
         cv.put(COL_DELETED, (tasks.get(i).isDeleted()) ? 1 : 0);
         cv.put(COL_HIDDEN, (tasks.get(i).isHidden()) ? 1 : 0);
         cv.put(COL_REMINDER, tasks.get(i).getReminder());
         cv.put(COL_REMINDER_ID, tasks.get(i).getReminderId());
         cv.put(COL_REMINDER_REPEAT_MODE, tasks.get(i).getRepeatMode());
         cv.put(COL_SYNC_STATUS, tasks.get(i).getSyncStatus());
         cv.put(COL_LOCAL_UPDATED, tasks.get(i).getLocalModify());
         cv.put(COL_LOCAL_SIBLING, tasks.get(i).getPreviousTask());
         cv.put(COL_LOCAL_DELETED, tasks.get(i).getLocalDeleted());
         cv.put(COL_MOVED, tasks.get(i).getMoved());
         db.insert(TABLE_NAME, null, cv);
      }
      bm.dataChanged();
      //db.close();
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
      //db.close();
   }


   public int deleteTask(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      int deletedRow = db.delete(TABLE_NAME, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
      return deletedRow;
   }


   public void deleteTask(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      int deletedRow = db.delete(TABLE_NAME, selection, selectionArgs);
      bm.dataChanged();
      //db.close();
   }

   public long getTaskReminder(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      long taskReminder = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_REMINDER}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         taskReminder = cursor.getLong(cursor.getColumnIndex(COL_REMINDER));
         cursor.close();
      }
      //db.close();
      return taskReminder;
   }

   public long getTaskReminderByIntId(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      long taskReminder = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_REMINDER}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         taskReminder = cursor.getLong(cursor.getColumnIndex(COL_REMINDER));
         cursor.close();
      }
      //db.close();
      return taskReminder;
   }

   public int getTaskReminderRepeatModeByIntId(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      int taskReminderMode = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_REMINDER_REPEAT_MODE}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         taskReminderMode = cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE));
         cursor.close();
      }
      //db.close();
      return taskReminderMode;
   }

   public int getTaskReminderRepeatMode(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      int taskReminderMode = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_REMINDER_REPEAT_MODE}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         taskReminderMode = cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE));
         cursor.close();
      }
      //db.close();
      return taskReminderMode;
   }

   public int updateTaskStatus(int intId, String newStatus) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_STATUS, newStatus);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
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
      //db.close();
      bm.dataChanged();
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
      //db.close();
      bm.dataChanged();
      return updatedRow;
   }

   public int updateSibling(int id, int sibling) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(id)};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_LOCAL_SIBLING, sibling);
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
      return updatedRow;
   }

   public int updateLocalDeleted(int localDeleted, String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_DELETED, localDeleted);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
      return updatedRow;
   }

   public int updateMoved(int moved, String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      cv.put(COL_MOVED, moved);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      //db.close();
      return db.update(TABLE_NAME, cv, selection, selectionArgs);
   }

   public int updateLocalSibling(String taskId, String localSibling) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      if (localSibling != null && localSibling.equals(Co.TASK_MOVED_TO_FIRST)) {
         cv.putNull(COL_LOCAL_SIBLING);
      } else {
         cv.put(COL_LOCAL_SIBLING, localSibling);
      }
      cv.put(COL_MOVED, Co.MOVED);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      //db.close();
      return db.update(TABLE_NAME, cv, selection, selectionArgs);
   }

   public int updateLocalModify(long localModify, String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, localModify);
      //db.close();
      return db.update(TABLE_NAME, cv, selection, selectionArgs);
   }

   public boolean taskExistsInDB(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, selection, selectionArgs, null, null, null);
      boolean exists = cursor.getCount() > 0;
      //db.close();
      cursor.close();
      return exists;
   }

   public LocalTask getTaskByTaskId(String taskId) {
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
         task.setRepeatMode(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE)));
         task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
         task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
         task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
         task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
         task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
         //db.close();
         cursor.close();
         return task;
      } else {
         cursor.close();
         return null;
      }
   }

   public LocalTask getTaskByIntId(int Id) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(Id)};
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
         task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
         task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
         task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
         task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
         task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
         //db.close();
         cursor.close();
         return task;
      } else {
         cursor.close();
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
      //db.close();
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
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
      return updatedRow;
   }

   public long getTaskReminderId(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      long reminderId = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_REMINDER_ID}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         reminderId = cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID));
         cursor.close();
      }
      //db.close();
      return reminderId;
   }

   public long getDeleted(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      long deleted = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_DELETED}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         deleted = cursor.getLong(cursor.getColumnIndex(COL_DELETED));
         cursor.close();
      }
      //db.close();
      return deleted;
   }

   public long getLocalModify(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      long modify = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_LOCAL_UPDATED}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         modify = cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED));
         cursor.close();
      }
      //db.close();
      return modify;
   }

   public long getLocalSIbling(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      long modify = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_LOCAL_SIBLING}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         modify = cursor.getLong(cursor.getColumnIndex(COL_LOCAL_SIBLING));
         cursor.close();
      }
      //db.close();
      return modify;
   }

   public long getMoved(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      long modify = 0;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_MOVED}, selection, selectionArgs, null, null, null);
      if (cursor != null) {
         cursor.moveToFirst();
         modify = cursor.getLong(cursor.getColumnIndex(COL_MOVED));
         cursor.close();
      }
      //db.close();
      return modify;
   }


   public int updateLocalTask(LocalTask modifiedTask, boolean updateReminders) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(modifiedTask.getIntId())};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, modifiedTask.getId());
      cv.put(COL_LIST, modifiedTask.getList());
      cv.put(COL_TITLE, modifiedTask.getTitle());
      cv.put(COL_SERVER_UPDATED, modifiedTask.getServerModify());
      cv.put(COL_PARENT, modifiedTask.getParent());
      cv.put(COL_POSITION, modifiedTask.getPosition());
      cv.put(COL_NOTES, modifiedTask.getNotes());
      cv.put(COL_STATUS, modifiedTask.getStatus());
      cv.put(COL_DUE, modifiedTask.getDue());
      cv.put(COL_COMPLETED, modifiedTask.getCompleted());
      cv.put(COL_DELETED, (modifiedTask.isDeleted()) ? 1 : 0);
      cv.put(COL_HIDDEN, (modifiedTask.isHidden()) ? 1 : 0);
      if (updateReminders) {
         cv.put(COL_REMINDER, modifiedTask.getReminder());
         cv.put(COL_REMINDER_ID, modifiedTask.getReminderId());
         cv.put(COL_REMINDER_REPEAT_MODE, modifiedTask.getRepeatMode());
      }
      cv.put(COL_SYNC_STATUS, modifiedTask.getSyncStatus());
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_LOCAL_SIBLING, modifiedTask.getPreviousTask());
      cv.put(COL_LOCAL_DELETED, modifiedTask.getLocalDeleted());
      cv.put(COL_MOVED, modifiedTask.getMoved());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
      return updatedRow;
   }


   public void updateTask(Task task, String listId) {
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
      //db.close();
      bm.dataChanged();
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
      //db.close();
      bm.dataChanged();
      return getTaskByTaskId(task.getId());
   }

   public void setTemporaryPosition(String taskId, String newTaskTempPos) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_POSITION, newTaskTempPos);
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();

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
      //db.close();
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
      //db.close();
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
      //db.close();
      return intId;
   }

   public void updateMovedByIntId(int intId, int moved) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_MOVED, moved);
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
   }

   public void setTemporaryPositionByIntId(int intId, String newTaskTempPos) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis() + 10000);
      cv.put(COL_POSITION, newTaskTempPos);
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
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
         task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
         task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
         task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
         task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
         task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
         //db.close();
         cursor.close();
         return task;
      } else {
         cursor.close();
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
      } finally {
         db.endTransaction();
         //db.close();
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
      } finally {
         db.endTransaction();
         bm.dataChanged();
      }
   }
}


