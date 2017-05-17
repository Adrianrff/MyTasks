package com.adrapps.mytasks;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.os.ParcelFileDescriptor;

import com.adrapps.mytasks.views.MainActivity;

import java.io.File;
import java.io.IOException;

public class Backup extends BackupAgentHelper {

    static final String TASKS_DATABASE = "TasksDatabase";
    static final String LISTS_DATABASE = "ListsDataBase";
    static final String DB_BACKUP_KEY = "databases";


    @Override
    public void onCreate() {
//        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this,
//                File_Name_Of_Preferences);
//        addHelper(PREFS_BACKUP_KEY, helper);
        FileBackupHelper helper = new FileBackupHelper(this,
                TASKS_DATABASE, LISTS_DATABASE);
        addHelper(DB_BACKUP_KEY, helper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper performs backup
        synchronized (MainActivity.sDataLock) {
            super.onBackup(oldState, data, newState);
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper restores the file
        synchronized (MainActivity.sDataLock) {
            super.onRestore(data, appVersionCode, newState);
        }

    }

    @Override
    public File getFilesDir(){
        File path = getDatabasePath(TASKS_DATABASE);
        return path.getParentFile();
    }
}



