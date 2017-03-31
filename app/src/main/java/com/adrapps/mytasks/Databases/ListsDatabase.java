package com.adrapps.mytasks.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.services.tasks.model.TaskList;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Adrian Flores on 28/3/2017.
 */

public class ListsDatabase extends SQLiteOpenHelper {

    //---------DATABASE AND TABLE NAMES----------//
    public static final String DATABASE_NAME = "ListsDataBase.db";
    public static final String TABLE_NAME = "ListsTable";

    //----------TABLE COLUMNS--------------//
    public static final String COL_INT_ID = "Int_Id";
    public static final String COL_ID = "Id";
    public static final String COL_TITLE = "Title";
    public static final String COL_UPDATED = "Updated";
    public static final String COL_SELFLINK = "Selflink";



    //---------ALL COLUMNS ARRAY----------//
    public static final String[] ALL_COLUMNS = {
            COL_INT_ID,
            COL_ID,
            COL_TITLE,
            COL_UPDATED,
            COL_SELFLINK};

    //-----------CREATE TABLE STATEMENT--------//
    public static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " +
                    COL_INT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ID + " text," +
                    COL_TITLE + " text," +
                    COL_UPDATED + " bigint," +
                    COL_SELFLINK + " text)";


    //----------CONSTRUCTOR--------------//
    public ListsDatabase(Context context) {
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

    public List<String> getListsTitles(){
        List<String> listsTitles = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(query,ALL_COLUMNS,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                listsTitles.add(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listsTitles;
    }

    public List<String> getListsIds(){
        List<String> listsTitles = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(query,ALL_COLUMNS,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                listsTitles.add(cursor.getString(cursor.getColumnIndex(COL_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listsTitles;
    }

    public long addList(TaskList list){
        SQLiteDatabase db = this.getWritableDatabase();
        int offset = TimeZone.getDefault().getRawOffset();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID,list.getId());
        cv.put(COL_TITLE,list.getTitle());
        cv.put(COL_UPDATED,list.getUpdated().getValue() + offset);
        cv.put(COL_SELFLINK,list.getSelfLink());
        long insertedRow = db.insert(TABLE_NAME,null,cv);
        db.close();
        return insertedRow;
    }

    public void updateLists (List<TaskList> lists){
        SQLiteDatabase db = this.getWritableDatabase();
        int offset = TimeZone.getDefault().getRawOffset();
        ContentValues cv = new ContentValues();
        onUpgrade(db,1,1);
        for (int i = 0; i < lists.size(); i++ ){
            cv.put(COL_ID,lists.get(i).getId());
            cv.put(COL_TITLE,lists.get(i).getTitle());
            cv.put(COL_UPDATED,lists.get(i).getUpdated().getValue() + offset);
            cv.put(COL_SELFLINK,lists.get(i).getSelfLink());
            db.insert(TABLE_NAME,null,cv);
        }
        db.close();
    }
}
