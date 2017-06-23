package com.adrapps.mytasks.databases;

import android.app.backup.BackupManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.adrapps.mytasks.domain.Co;
import com.adrapps.mytasks.domain.LocalList;
import com.google.api.services.tasks.model.TaskList;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ListsDatabase extends SQLiteOpenHelper {

   private static ListsDatabase listsDb;
   private final BackupManager bm;
   private SQLiteDatabase db;

   //---------DATABASE AND TABLE NAMES----------//
   private static final String DATABASE_NAME = "ListsDataBase";
   private static final String TABLE_NAME = "ListsTable";
   private static final int DATABASE_VERSION = 1;

   //----------TABLE COLUMNS--------------//
   private static final String COL_INT_ID = "Int_Id";
   private static final String COL_ID = "Id";
   private static final String COL_TITLE = "Title";
   private static final String COL_SERVER_UPDATED = "Server_updated";
   private static final String COL_LOCAL_UPDATED = "Local_updated";
   private static final String COL_SYNC_STATUS = "Sync_status";

   //---------ALL COLUMNS ARRAY----------//
   private static final String[] ALL_COLUMNS = {
         COL_INT_ID,
         COL_ID,
         COL_TITLE,
         COL_SERVER_UPDATED,
         COL_LOCAL_UPDATED,
         COL_SYNC_STATUS};

   //-----------CREATE TABLE STATEMENT--------//
   private static final String CREATE_TABLE =
         "create table " + TABLE_NAME + " ( " +
               COL_INT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
               COL_ID + " text," +
               COL_TITLE + " text," +
               COL_SERVER_UPDATED + " bigint," +
               COL_LOCAL_UPDATED + " bigint," +
               COL_SYNC_STATUS + " int)";

   public static synchronized ListsDatabase getInstance(Context context) {
      // Use the application context, which will ensure that you don't accidentally leak an Activity's context.
      if (listsDb == null) {
         listsDb = new ListsDatabase(context.getApplicationContext());
      }
      return listsDb;
   }

   //----------CONSTRUCTOR--------------//
   private ListsDatabase(Context context) {
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


   public List<String> getListsTitles() {
      List<String> listsTitles = new ArrayList<>();
      db = getReadableDB();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
      if (cursor.moveToFirst()) {
         do {
            listsTitles.add(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return listsTitles;
   }

   public List<String> getListsIds() {
      List<String> listsTitles = new ArrayList<>();
      db = getReadableDB();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
      if (cursor.moveToFirst()) {
         do {
            listsTitles.add(cursor.getString(cursor.getColumnIndex(COL_ID)));
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return listsTitles;
   }

   public long addList(TaskList list) {
      db = getWritableDB();
      int offset = TimeZone.getDefault().getRawOffset();
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, list.getId());
      cv.put(COL_TITLE, list.getTitle());
      cv.put(COL_SERVER_UPDATED, list.getUpdated().getValue() + offset);
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      long insertedRow = db.insert(TABLE_NAME, null, cv);
      //db.close();
      bm.dataChanged();
      return insertedRow;
   }

   public int addList(String listTitle) {
      db = getWritableDB();
      ContentValues cv = new ContentValues();
      cv.put(COL_TITLE, listTitle);
      cv.put(COL_SYNC_STATUS, Co.NOT_SYNCED);
      int insertedRow = (int) db.insert(TABLE_NAME, null, cv);
      //db.close();
      bm.dataChanged();
      return insertedRow;
   }

   public void updateLists(List<TaskList> lists) {
      db = getWritableDB();
      db.beginTransaction();
      ContentValues cv = new ContentValues();
      onUpgrade(db, 1, 1);
      try {
         for (int i = 0; i < lists.size(); i++) {
            cv.put(COL_ID, lists.get(i).getId());
            cv.put(COL_TITLE, lists.get(i).getTitle());
            cv.put(COL_SERVER_UPDATED, lists.get(i).getUpdated().getValue());
            cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
            cv.put(COL_SYNC_STATUS, Co.SYNCED);
            db.insert(TABLE_NAME, null, cv);
         }
         db.setTransactionSuccessful();
      } catch (Exception e) {
         e.printStackTrace();
      }
      finally {
         db.endTransaction();
         bm.dataChanged();
      }
   }

   public String getListTitleFromId(String listId) {
      String listTitle = null;
      db = getReadableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {listId};
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, selection, selectionArgs, null, null, null);
      if (cursor.moveToFirst()) {
         do {
            listTitle = cursor.getString(cursor.getColumnIndex(COL_TITLE));
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return listTitle;
   }

   public void updateList(LocalList localList) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(localList.getIntId())};
      ContentValues cv = new ContentValues();
      cv.put(COL_ID, localList.getId());
      cv.put(COL_TITLE, localList.getTitle());
      cv.put(COL_SERVER_UPDATED, localList.getServerUpdated());
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
   }

   public void updateList(TaskList list) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {list.getId()};
      ContentValues cv = new ContentValues();
      cv.put(COL_TITLE, list.getTitle());
      cv.put(COL_SERVER_UPDATED, list.getUpdated().getValue());
      cv.put(COL_LOCAL_UPDATED, System.currentTimeMillis());
      cv.put(COL_SYNC_STATUS, Co.SYNCED);
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
   }

   public void editListTitle(String listId, String title) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {listId};
      ContentValues cv = new ContentValues();
      cv.put(COL_TITLE, title);
      db.update(TABLE_NAME, cv, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
   }

   public int getIntItByListId(String listId) {
      int intId = -1;
      db = getReadableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {listId};
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_INT_ID}, selection, selectionArgs, null, null, null);
      if (cursor.moveToFirst()) {
         do {
            intId = cursor.getInt(cursor.getColumnIndex(COL_INT_ID));
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return intId;
   }

   public String getListIdByIntId(int intId) {
      String listId = null;
      db = getReadableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, selection, selectionArgs, null, null, null);
      if (cursor.moveToFirst()) {
         do {
            listId = cursor.getString(cursor.getColumnIndex(COL_ID));
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return listId;
   }

   public void deleteList(String listId) {
      db = getWritableDB();
      String selection = COL_ID + " = ? ";
      String[] selectionArgs = {listId};
      db.delete(TABLE_NAME, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
   }

   public void deleteList(int intId) {
      db = getWritableDB();
      String selection = COL_INT_ID + " = ? ";
      String[] selectionArgs = {String.valueOf(intId)};
      db.delete(TABLE_NAME, selection, selectionArgs);
      //db.close();
      bm.dataChanged();
   }

   public List<LocalList> getLocalLists() {
      List<LocalList> localLists = new ArrayList<>();
      db = getReadableDB();
      Cursor cursor = db.query(TABLE_NAME, ALL_COLUMNS, null, null, null, null, null);
      if (cursor.moveToFirst()) {
         do {
            LocalList list = new LocalList();
            list.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
            list.setId(cursor.getString(cursor.getColumnIndex(COL_ID)));
            list.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            list.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATUS)));
            list.setServerUpdated(cursor.getLong(cursor.getColumnIndex(COL_SERVER_UPDATED)));
            list.setLocalUpdated(cursor.getLong(cursor.getColumnIndex(COL_LOCAL_UPDATED)));
            localLists.add(list);
         } while (cursor.moveToNext());
      }
      cursor.close();
      //db.close();
      return localLists;
   }

   public int getListsCount() {
      db = getReadableDB();
      int listCount;
      Cursor cursor = db.query(TABLE_NAME, new String[]{COL_ID}, null, null, null, null, null);
      listCount = cursor.getCount();
      cursor.close();
      //db.close();
      return listCount;
   }
}
