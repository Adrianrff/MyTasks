package com.adrapps.mytasks.Databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.adrapps.mytasks.Domain.LocalTask;
import com.google.api.services.tasks.model.Task;
import java.util.ArrayList;
import java.util.List;

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
    public static final String COL_UPDATED = "Updated";
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
    public static final String ORDER_ASC = " ASC";
    public static final String ORDER_DESC = " DESC";

    //---------ALL COLUMNS ARRAY----------//
    private static final String[] ALL_COLUMNS = {
            COL_INT_ID,
//            COL_SORT_ID,
            COL_ID,
            COL_LIST,
            COL_TITLE,
            COL_UPDATED,
            COL_SELFLINK,
            COL_PARENT,
            COL_POSITION,
            COL_NOTES,
            COL_STATUS,
            COL_DUE,
            COL_COMPLETED,
            COL_DELETED,
            COL_HIDDEN,
            COL_REMINDER};

    //-----------CREATE TABLE STATEMENT--------//
    private static final String CREATE_TABLE =
            "create table " + TABLE_NAME + " ( " +
                    COL_INT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    COL_SORT_ID + "integer, " +
                    COL_ID + " text," +
                    COL_LIST + " text," +
                    COL_TITLE + " text," +
                    COL_UPDATED + " bigint," +
                    COL_SELFLINK + " text," +
                    COL_PARENT + " text," +
                    COL_POSITION + " text," +
                    COL_NOTES + " text," +
                    COL_STATUS + " text," +
                    COL_DUE + " bigint," +
                    COL_COMPLETED + " text," +
                    COL_DELETED + " int," +
                    COL_HIDDEN + " int," +
                    COL_REMINDER + " bigint)";




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

    public List<LocalTask> getTasksFromLlist(String listId){
        List<LocalTask> tasks = new ArrayList<>();
        String selection = COL_LIST + " = ? ";
        String[] selectionArgs = {listId};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,ALL_COLUMNS,selection,selectionArgs,null,null,COL_POSITION + ORDER_ASC);
        if (cursor.getCount() != 0 && cursor.moveToFirst()){
            do{
                LocalTask task = new LocalTask();
                task.setTaskId(cursor.getString(cursor.getColumnIndex(COL_ID)));
//                task.setSortId(cursor.getInt(cursor.getColumnIndex(COL_SORT_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
                task.setSelfLink(cursor.getString(cursor.getColumnIndex(COL_SELFLINK)));
                task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
                task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
                task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
                task.setTaskList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
                task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
                task.setUpdated(cursor.getLong(cursor.getColumnIndex(COL_UPDATED)));
                task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
                task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
                task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
                task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
                task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
                task.setReminder(cursor.getLong(cursor.getColumnIndex(COL_REMINDER)));
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

//    public List<LocalTask> getTasksFromLlist(String listId, String sort){
//        List<LocalTask> tasks = new ArrayList<>();
//        String selection = COL_LIST + " = ? ";
//        String[] selectionArgs = {listId};
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor;
//        if (sort.equals(Co.ORDER_POSITION_DESC))
//            cursor = db.query(TABLE_NAME,ALL_COLUMNS,selection,selectionArgs,
//                    null,null,ORDER_BY_STATEMENT + COL_SORT_ID + ORDER_DESC);
//        else if (sort.equals(Co.ORDER_POSITION_ASC))
//            cursor = db.query(TABLE_NAME,ALL_COLUMNS,selection,selectionArgs,
//                    null,null,ORDER_BY_STATEMENT + COL_SORT_ID + ORDER_ASC);
//        else if (sort.equals(Co.ORDER_DUE_DATE_ASC))
//            cursor = db.query(TABLE_NAME,ALL_COLUMNS,selection,selectionArgs,
//                    null,null,ORDER_BY_STATEMENT + COL_DUE + ORDER_ASC);
//        else
//            cursor = db.query(TABLE_NAME,ALL_COLUMNS,selection,selectionArgs,null,null,null);
//        if (cursor.getCount() != 0 && cursor.moveToFirst()){
//            do{
//                LocalTask task = new LocalTask();
//                task.setTaskId(cursor.getString(cursor.getColumnIndex(COL_ID)));
//                task.setSortId(cursor.getInt(cursor.getColumnIndex(COL_SORT_ID)));
//                task.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
//                task.setSelfLink(cursor.getString(cursor.getColumnIndex(COL_SELFLINK)));
//                task.setParent(cursor.getString(cursor.getColumnIndex(COL_PARENT)));
//                task.setPosition(cursor.getString(cursor.getColumnIndex(COL_POSITION)));
//                task.setNotes(cursor.getString(cursor.getColumnIndex(COL_NOTES)));
//                task.setTaskList(cursor.getString(cursor.getColumnIndex(COL_LIST)));
//                task.setStatus(cursor.getString(cursor.getColumnIndex(COL_STATUS)));
//                task.setUpdated(cursor.getLong(cursor.getColumnIndex(COL_UPDATED)));
//                task.setCompleted(cursor.getLong(cursor.getColumnIndex(COL_COMPLETED)));
//                task.setDue(cursor.getLong(cursor.getColumnIndex(COL_DUE)));
//                task.setDeleted((cursor.getInt(cursor.getColumnIndex(COL_DELETED)) == 1));
//                task.setHidden((cursor.getInt(cursor.getColumnIndex(COL_HIDDEN)) == 1));
//                task.setIntId(cursor.getInt(cursor.getColumnIndex(COL_INT_ID)));
//                tasks.add(task);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        db.close();
//        return tasks;
//    }

    public long addTask(LocalTask localTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID,localTask.getTaskId());
        cv.put(COL_LIST,localTask.getTaskList());
        cv.put(COL_TITLE,localTask.getTitle());
        cv.put(COL_UPDATED,localTask.getUpdated());
        cv.put(COL_SELFLINK,localTask.getSelfLink());
        cv.put(COL_PARENT,localTask.getParent());
        cv.put(COL_POSITION,localTask.getPosition());
        cv.put(COL_NOTES,localTask.getNotes());
        cv.put(COL_STATUS,localTask.getStatus());
        cv.put(COL_DUE,localTask.getDue());
        cv.put(COL_COMPLETED,localTask.getCompleted());
        cv.put(COL_DELETED,(localTask.isDeleted()) ? 1:0);
        cv.put(COL_HIDDEN,(localTask.isHidden()) ? 1:0);
        cv.put(COL_REMINDER,localTask.getReminder());
        long insertedRow = db.insert(TABLE_NAME,null,cv);
        db.close();
        return insertedRow;
    }
    public long addTask(Task task, String listId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ID,task.getId());
        cv.put(COL_LIST,listId);
        cv.put(COL_TITLE,task.getTitle());
        cv.put(COL_UPDATED,task.getUpdated() == null ? 0 : task.getUpdated().getValue());
        cv.put(COL_SELFLINK,task.getSelfLink());
        cv.put(COL_PARENT,task.getParent());
        cv.put(COL_POSITION,task.getPosition());
        cv.put(COL_NOTES,task.getNotes());
        cv.put(COL_STATUS,task.getStatus());
        cv.put(COL_DUE,task.getDue() == null ? 0 : task.getDue().getValue());
        cv.put(COL_COMPLETED,task.getCompleted() == null ? 0 : task.getCompleted().getValue());
        cv.put(COL_DELETED,task.getDeleted() == null ? 0 : task.getDeleted() ? 1 : 0);
        cv.put(COL_HIDDEN,task.getHidden() == null ? 0 : task.getHidden() ? 1 : 0);
        cv.put(COL_REMINDER,0);
        long insertedRow = db.insert(TABLE_NAME,null,cv);
        db.close();
        return insertedRow;
    }

    public void updateTasks(List<LocalTask> tasks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        onUpgrade(db,1,1);
        for (int i = 0; i < tasks.size(); i++ ){
            cv.put(COL_ID,tasks.get(i).getTaskId());
            cv.put(COL_LIST,tasks.get(i).getTaskList());
            cv.put(COL_TITLE,tasks.get(i).getTitle());
            cv.put(COL_UPDATED,tasks.get(i).getUpdated());
            cv.put(COL_SELFLINK,tasks.get(i).getSelfLink());
            cv.put(COL_PARENT,tasks.get(i).getParent());
            cv.put(COL_POSITION,tasks.get(i).getPosition());
            cv.put(COL_NOTES,tasks.get(i).getNotes());
            cv.put(COL_STATUS,tasks.get(i).getStatus());
            cv.put(COL_DUE,tasks.get(i).getDue());
            cv.put(COL_COMPLETED,tasks.get(i).getCompleted());
            cv.put(COL_DELETED,(tasks.get(i).isDeleted()) ? 1:0);
            cv.put(COL_HIDDEN,(tasks.get(i).isHidden()) ? 1:0);
            cv.put(COL_REMINDER,tasks.get(i).getReminder());
            db.insert(TABLE_NAME,null,cv);
        }
        db.close();
    }


    public int deleteTask(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        int deletedRow = db.delete(TABLE_NAME,selection,selectionArgs);
        db.close();
        return deletedRow;
    }

    public long getTaskRemminder(String taskId){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long taskReminder = 0;
        Cursor cursor = db.query(TABLE_NAME,new String[] {COL_REMINDER},selection,selectionArgs,null,null,null);
        if (cursor != null){
            cursor.moveToFirst();
            taskReminder = cursor.getLong(cursor.getColumnIndex(COL_REMINDER));
            cursor.close();
        }
        db.close();
        return taskReminder;
    }

    public int updateTaskStatus(String taskId, String newStatus){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        ContentValues cv = new ContentValues();
        cv.put(COL_STATUS,newStatus);
        int updatedRow = db.update(TABLE_NAME,cv,selection,selectionArgs);
        db.close();
        return updatedRow;
    }

    public boolean taskExistsInDB (String taskId){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COL_ID + " = ? ";
        String[] selectionArgs = {taskId};
        long taskReminder = 0;
        Cursor cursor = db.query(TABLE_NAME,new String[] {COL_ID},selection,selectionArgs,null,null,null);
        boolean exists = cursor.getCount() > 0;
        db.close();
        return exists;
    }
}
