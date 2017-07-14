package com.adrapps.mytasks.other;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import com.adrapps.mytasks.domain.Co;

import java.io.File;
import java.io.IOException;

public class Backup extends BackupAgentHelper {

    static final String TASKS_DATABASE = "TasksDatabase";
    static final String LISTS_DATABASE = "ListsDataBase";
    static final String DB_BACKUP_KEY = "databases";
    private static final String PREFS_BACKUP_KEY = "prefsKey";

    public Backup() {
    }

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper prefHelper = new SharedPreferencesBackupHelper(this,
                Co.DEFAULT_PREFERENCES_NAME_FOR_BACKUP);
        addHelper(PREFS_BACKUP_KEY, prefHelper);
        FileBackupHelper fileHelper = new FileBackupHelper(this,
                TASKS_DATABASE, LISTS_DATABASE);
        addHelper(DB_BACKUP_KEY, fileHelper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper performs backup
//        synchronized (MainActivity.sDataLock) {
            super.onBackup(oldState, data, newState);
//        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        // Hold the lock while the FileBackupHelper restores the file
//        synchronized (MainActivity.sDataLock) {
            super.onRestore(data, appVersionCode, newState);
//        }

    }

    @Override
    public File getFilesDir(){
        File path = getDatabasePath(TASKS_DATABASE);
        return path.getParentFile();
    }
}



