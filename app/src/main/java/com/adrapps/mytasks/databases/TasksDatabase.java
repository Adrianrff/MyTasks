package com.adrapps.mytasks.databases;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalTask;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class TasksDatabase extends SQLiteOpenHelper {

   private final String TAG = "TaskDatabase";

   //---------DATABASE AND TABLE NAMES----------//
   private static final String DATABASE_NAME = "TasksDatabase";
   private static final String TABLE_NAME = "TasksTable";
   private static final int DATABASE_VERSION = 2;
   private static TasksDatabase tasksDB;

   //----------TABLE COLUMNS---------------//
   private static final String COL_INT_ID = "Int_Id";
   private static final String COL_ID = "Id";
   private static final String COL_LIST_ID = "List_id";
   private static final String COL_LIST_INT_ID = "List_int_id";
   private static final String COL_TITLE = "Title";
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
   private static final String COL_LOCAL_MODIFY = "local_updated";
   private static final String COL_LOCAL_DELETED = "local_deleted";
   private static final String ORDER_ASC = " ASC";
   public static final String ORDER_DESC = " DESC";
   private static final String TABLE_V1_COPY = " TABLE_V1_COPY";
   private SQLiteDatabase db;

   //---------ALL COLUMNS ARRAY----------//
   private static final String[] ALL_COLUMNS = {
         COL_INT_ID,
         COL_ID,
         COL_LIST_ID,
         COL_LIST_INT_ID,
         COL_TITLE,
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
//         COL_SYNC_STATUS,
         COL_LOCAL_SIBLING,
         COL_MOVED,
         COL_LOCAL_MODIFY,
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
               COL_LOCAL_MODIFY + " bigint default 0," +
               COL_PARENT + " text," +
               COL_STATUS + " text," +
               COL_COMPLETED + " text," +
               COL_REMINDER_REPEAT_MODE + " int default 0," +
               COL_REMINDER_REPEAT_DAY + " int default -1," +
               COL_REMINDER_ID + " bigint," +
//               COL_SYNC_STATUS + " int," +
               COL_LOCAL_SIBLING + " int default 0," +
               COL_MOVED + " int default 0," +
               COL_DELETED + " int," +
               COL_HIDDEN + " int," +
               COL_LIST_ID + " text," +
               COL_LIST_INT_ID + " int," +
               COL_ID + " text," +
               COL_LOCAL_DELETED + " int default 0)";
   private long offSet = TimeZone.getDefault().getRawOffset();
   private final BackupManager bm;


   //Version 2
   private final String CREATE_TABLE_VERSION_2 = "create table " + TABLE_NAME + " ( " +
         COL_INT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
         COL_TITLE + " text," +
         COL_NOTES + " text," +
         COL_DUE + " bigint," +
         COL_REMINDER + " bigint default 0," +
         COL_POSITION + " text default 000," +
         COL_LOCAL_MODIFY + " bigint default 0," +
         COL_PARENT + " text," +
         COL_STATUS + " text," +
         COL_COMPLETED + " text," +
         COL_REMINDER_REPEAT_MODE + " int default 0," +
         COL_REMINDER_REPEAT_DAY + " int default -1," +
         COL_REMINDER_ID + " bigint," +
         COL_LOCAL_SIBLING + " int default 0," +
         COL_MOVED + " int default 0," +
         COL_DELETED + " int," +
         COL_HIDDEN + " int," +
         COL_LIST_ID + " text," +
         COL_LIST_INT_ID + " int," +
         COL_ID + " text," +
         COL_LOCAL_DELETED + " int default 0)";



   public static synchronized TasksDatabase getInstance(Context context) {
      // Use the application context, which will ensure that you don't accidentally leak an Activity's context.
      if (tasksDB == null) {
         tasksDB = new TasksDatabase(context.getApplicationContext());
      }
      return tasksDB;
   }

   //----------CONSTRUCTOR--------------//
   private TasksDatabase(Context context) {
      super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
      bm = new BackupManager(context);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      db.execSQL(CREATE_TABLE);

   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion <= 1) {
         if (newVersion == 2) {
            List<String> newComulmns = new ArrayList<>(Arrays.asList(ALL_COLUMNS));
            String sepColumns = TextUtils.join(",",newComulmns);
            db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + TABLE_NAME + "_old;");
            db.execSQL(CREATE_TABLE_VERSION_2);
            db.execSQL("INSERT INTO " + TABLE_NAME + "(" + sepColumns + ") SELECT "
                  + sepColumns + " FROM " + TABLE_NAME + "_old;");
            db.execSQL("DROP TABLE " + TABLE_NAME + "_old;");
         }
      } else {
         onCreate(db);
      }
//      onCreate(db);
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

   //-----------------------------HELPER METHODS------------------------------------//
   private LocalTask getLocalTaskFromCursor(Cursor cursor){
      LocalTask task = new LocalTask();
      task.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
      task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
      task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
      task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
      task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
      task.setListId(cursor.getString(cursor.getColumnIndex(COL_LIST_ID)));
      task.setListIntId(cursor.getInt(cursor.getColumnIndex(COL_LIST_INT_ID)));
      task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)) == null ?
            Co.TASK_NEEDS_ACTION : cursor.getString(cursor.getColumnIndex(COL_STATUS)));
      task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
      task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
      task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
      task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
      task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
      task.setReminderNoID(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
      task.setReminderId(cursor.getLong(cursor.getColumnIndex(COL_REMINDER_ID)));
      task.setRepeatMode(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_MODE)));
      task.setRepeatDay(cursor.getInt(cursor.getColumnIndex(COL_REMINDER_REPEAT_DAY)));
//      task.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
      task.setLocalDeleted(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)));
      task.setLocalModify(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_MODIFY)));
      task.setMoved(cursor.getInt(cursor.getColumnIndex(COL_MOVED)));
      task.setPreviousTask(cursor.getInt(cursor.getColumnIndex(COL_LOCAL_SIBLING)));
      return task;
   }


   ///********------------------------------OPERATIONS-----------------------------------******//

   public List<LocalTask> getLocalTasks() {
      List<LocalTask> tasks = new ArrayList<>();
      db = getWritableDB();
      Cursor cursor;
      try {
         cursor = db.query(TABLE_NAME, null, null, null, null, null, COL_POSITION + ORDER_ASC);
         if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            do {
               tasks.add(getLocalTaskFromCursor(cursor));
            } while (cursor.moveToNext());
         }
         cursor.close();
      } catch (Exception e) {
         e.printStackTrace();
         //db.close();
      }
//      //db.close();
      return tasks;
   }

   public List<LocalTask> getTasksFromList(String listId) {
      List<LocalTask> tasks = new ArrayList<>();
      String selection = COL_LIST_ID + " = ? ";
      String[] selectionArgs = {listId};
      db = getReadableDB();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         do {
            tasks.add(getLocalTaskFromCursor(cursor));
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return tasks;
   }

   public int getTasksNotCompletedFromList(int intId) {
      int tasksNotCompletedCount = 0;
      String selection = COL_LIST_INT_ID + " = ?";
      String[] selectionArgs = {String.valueOf(intId)};
      db = getReadableDB();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         do {
            String taskStatus = cursor.getString(cursor.getColumnIndex(COL_STATUS));
            if (taskStatus == null || !taskStatus.equals(Co.TASK_COMPLETED)) {
               tasksNotCompletedCount += 1;
            }
         } while (cursor.moveToNext());
      }
      cursor.close();
      return tasksNotCompletedCount;
   }

   public List<LocalTask> getTasksFromList(int listIntId) {
      List<LocalTask> tasks = new ArrayList<>();
      String selection = COL_LIST_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(listIntId)};
      db = getReadableDB();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         do {
            tasks.add(getLocalTaskFromCursor(cursor));
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return tasks;
   }

   public List<LocalTask> getTasksFromListForAdapter(int listIntId) {
      List<LocalTask> tasks = new ArrayList<>();
      String selection = COL_LIST_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(listIntId)};
      db = getReadableDB();
      db.beginTransaction();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, COL_POSITION + ORDER_ASC);
      try {
         if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            do {
               if (cursor.getInt(cursor.getColumnIndex(COL_LOCAL_DELETED)) != Co.LOCAL_DELETED) {
                  tasks.add(getLocalTaskFromCursor(cursor));
               }
            } while (cursor.moveToNext());
            db.setTransactionSuccessful();
         }
      } catch (Exception e) {
         e.printStackTrace();
         db.endTransaction();
      } finally {
         db.endTransaction();
         cursor.close();
      }
      return tasks;
   }

   public int addTaskToDataBase(LocalTask localTask) {
      db = getWritableDB();
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, localTask.getId());
      cv.put(COL_LIST_ID, localTask.getListId());
      cv.put(COL_LIST_INT_ID, localTask.getListIntId());
      cv.put(COL_TITLE, localTask.getTitle());
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
//      cv.put(COL_SYNC_STATUS, Co.NOT_SYNCED);
      cv.put(COL_LOCAL_DELETED, localTask.getLocalDeleted());
      cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
      cv.put(COL_LOCAL_SIBLING, localTask.getPreviousTask());
      cv.put(COL_MOVED, localTask.getMoved());
      int insertedRow = (int) db.insert(TABLE_NAME, null, cv);
      bm.dataChanged();
      return insertedRow;
   }

   public long addTaskFirstTimeFromServer(Task task, String listId, int listIntId) {
      db = getWritableDB();
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST_ID, listId);
      cv.put(COL_LIST_INT_ID, listIntId);
      cv.put(COL_TITLE, task.getTitle());
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
//      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      cv.put(COL_LOCAL_MODIFY, task.getUpdated().getValue());
      long insertedRow = db.insert(TABLE_NAME, null, cv);
      //db.close();
      bm.dataChanged();
      return insertedRow;
   }

   public void updateTasksFirstTime(List<LocalTask> tasks) {
      List<String> taskIds = new ArrayList<>();
      ArrayList<LocalTask> updatedTasks = new ArrayList<>();
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
         } else {
            cv.put(COL_ID, currentTask.getId());
            cv.put(COL_LIST_ID, currentTask.getListId());
            cv.put(COL_LIST_INT_ID, currentTask.getListIntId());
            cv.put(COL_TITLE, currentTask.getTitle());
            cv.put(COL_PARENT, currentTask.getParent());
            cv.put(COL_POSITION, currentTask.getPosition());
            cv.put(COL_NOTES, currentTask.getNotes());
            cv.put(COL_STATUS, currentTask.getStatus());
            cv.put(COL_DUE, currentTask.getDue());
            cv.put(COL_COMPLETED, currentTask.getCompleted());
            cv.put(COL_DELETED, (currentTask.isDeleted()) ? 1 : 0);
            cv.put(COL_HIDDEN, (currentTask.isHidden()) ? 1 : 0);
//            cv.put(COL_SYNC_STATUS, currentTask.getSyncStatus());
            cv.put(COL_LOCAL_MODIFY, currentTask.getLocalModify());
            cv.put(COL_LOCAL_SIBLING, currentTask.getPreviousTask());
            cv.put(COL_LOCAL_DELETED, currentTask.getLocalDeleted());
            cv.put(COL_MOVED, currentTask.getMoved());
            db.insert(TABLE_NAME, null, cv);
         }
      }
   }

   public void deleteTask(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      db.delete(TABLE_NAME, selection, selectionArgs);
      bm.dataChanged();
      //db.close();
   }

   public int updateTaskStatus(int intId, String newStatus) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_STATUS, newStatus);
      cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
//      cv.put(COL_SYNC_STATUS, Co.EDITED_NOT_SYNCED);
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      bm.dataChanged();
      return updatedRow;
   }

   public int markDeleted(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_DELETED, Co.LOCAL_DELETED);
      cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
      return updatedRow;
   }

//   public int updateSyncStatus(LocalTask localTask, int newStatus) {
//      db = getWritableDB();
//      String selection = COL_INT_ID + " = ? ";
//      String[] selectionArgs = {String.valueOf(localTask.getIntId())};
//      ContentValues cv = new ContentValues();
//      cv.put(COL_SYNC_STATUS, newStatus);
//      if (newStatus == Co.SYNCED) {
//         cv.put(COL_MOVED, Co.NOT_MOVED);
//         cv.put(COL_DELETED, Co.NOT_DELETED);
//         cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
//      }
//      cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
//      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
//      //db.close();
//      bm.dataChanged();
//      return updatedRow;
//   }

   private LocalTask getTaskByTaskId(String taskId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {taskId};
      LocalTask task;
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, null);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         task = getLocalTaskFromCursor(cursor);
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
//      cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
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
      cv.put(COL_REMINDER, reminder);
      cv.put(COL_REMINDER_REPEAT_MODE, repeatMode);
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      bm.dataChanged();
      return updatedRow;
   }

   public int updateLocalTask(LocalTask task, boolean updateReminders) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(task.getIntId())};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST_ID, task.getListId());
      cv.put(COL_LIST_INT_ID, task.getListIntId());
      cv.put(COL_TITLE, task.getTitle());
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
//      cv.put(COL_SYNC_STATUS, task.getSyncStatus());
      cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
      cv.put(COL_LOCAL_SIBLING, task.getPreviousTask());
      cv.put(COL_LOCAL_DELETED, task.getLocalDeleted());
      cv.put(COL_MOVED, task.getMoved());
      int updatedRow = db.update(TABLE_NAME, cv, selection, selectionArgs);
      bm.dataChanged();
      return updatedRow;
   }

   public void updateExistingTaskFromServerTask(Task task, String listId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {task.getId()};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST_ID, listId);
      cv.put(COL_TITLE, task.getTitle());
      cv.put(COL_PARENT, task.getParent());
      cv.put(COL_POSITION, task.getPosition());
      cv.put(COL_NOTES, task.getNotes());
      cv.put(COL_STATUS, task.getStatus());
      cv.put(COL_DUE, task.getDue() != null ? task.getDue().getValue() - offSet : 0);
      cv.put(COL_COMPLETED, task.getCompleted() != null ? task.getCompleted().getValue() : null);
      cv.put(COL_DELETED, task.getDeleted() == null ? 0 : 1);
      cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : 1);
//      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      cv.put(COL_LOCAL_MODIFY, task.getUpdated().getValue());
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
   }

   public void updateExistingTaskFromLocalTask(LocalTask task) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(task.getIntId())};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST_ID, task.getListId());
      cv.put(COL_LIST_INT_ID, task.getListIntId());
      cv.put(COL_TITLE, task.getTitle());
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
//      cv.put(COL_SYNC_STATUS, task.getSyncStatus());
      cv.put(COL_LOCAL_SIBLING, task.getPreviousTask());
      cv.put(COL_MOVED, task.getMoved());
      cv.put(COL_LOCAL_MODIFY, task.getLocalModify());
      cv.put(COL_LOCAL_DELETED, task.getLocalDeleted());
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
   }

   public LocalTask updateNewlyCreatedTask(Task task, String listId, int taskIntId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(taskIntId)};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, task.getId());
      cv.put(COL_LIST_ID, listId);
      cv.put(COL_INT_ID, taskIntId);
      cv.put(COL_TITLE, task.getTitle());
      cv.put(COL_PARENT, task.getParent());
      cv.put(COL_POSITION, task.getPosition());
      cv.put(COL_NOTES, task.getNotes());
      cv.put(COL_STATUS, task.getStatus());
      cv.put(COL_DUE, task.getDue() != null ? task.getDue().getValue() - offSet : 0);
      cv.put(COL_COMPLETED, task.getCompleted() != null ? task.getCompleted().getValue() : 0);
      cv.put(COL_DELETED, task.getDeleted() == null ? 0 : 1);
      cv.put(COL_HIDDEN, task.getHidden() == null ? 0 : 1);
      cv.put(COL_LOCAL_MODIFY, task.getUpdated().getValue());
//      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      bm.dataChanged();
      return getTaskByTaskId(task.getId());
   }

   public void updatePosition(Task task) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {task.getId()};
      ContentValues cv = new ContentValues();
      cv.put(COL_LOCAL_MODIFY, System.currentTimeMillis());
      cv.put(COL_POSITION, task.getPosition());
      db.update(TABLE_NAME, cv, selection, selectionArgs);
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
      return intId;
   }

   public LocalTask getTask(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      LocalTask task;
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, null);
      if (cursor.getCount() != 0 && cursor.moveToFirst()) {
         task = getLocalTaskFromCursor(cursor);
         cursor.close();
         return task;
      } else {
         cursor.close();
         return null;
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
            cv.put(COL_POSITION, task.getPosition());
            cv.put(COL_MOVED, Co.NOT_MOVED);
//            cv.put(COL_SYNC_STATUS, Co.SYNCED);
            db.update(TABLE_NAME, cv, selection, selectionArgs);
         }
         db.setTransactionSuccessful();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         db.endTransaction();
         bm.dataChanged();
         //db.close();
      }
   }

   public void deleteTaskFromList(int listIntId) {
      List<LocalTask> tasks = getTasksFromList(listIntId);
      db = getWritableDB();
      db.beginTransaction();
      try {
         for (LocalTask task : tasks) {
            String selection = COL_INT_ID + " = ? ";
            String[] selectionArgs = {String.valueOf(task.getIntId())};
            db.delete(TABLE_NAME, selection, selectionArgs);
         }
         db.setTransactionSuccessful();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         db.endTransaction();
         bm.dataChanged();
      }
   }

   public void markTasksDeleted(List<LocalTask> tasksFromList) {
      db = getWritableDB();
      db.beginTransaction();
      try {
         for (LocalTask task : tasksFromList) {
            String selection = COL_INT_ID + " = ? ";
            String[] selectionArgs = {String.valueOf(task.getIntId())};
            ContentValues cv = new ContentValues();
            cv.put(COL_DELETED, Co.LOCAL_DELETED);
            db.update(TABLE_NAME, cv, selection, selectionArgs);
         }
         db.setTransactionSuccessful();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         db.endTransaction();
         bm.dataChanged();
      }
   }

   public void updateTaskParent(LocalTask task, String parent) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(task.getIntId())};
      ContentValues cv = new ContentValues();
      cv.put(COL_PARENT, parent);
      cv.put(COL_LOCAL_MODIFY, task.getLocalModify());
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      bm.dataChanged();
   }
}


