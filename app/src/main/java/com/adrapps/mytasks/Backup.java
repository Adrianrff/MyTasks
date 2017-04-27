package com.adrapps.mytasks;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;


/**
 * Created by Adrian Flores on 26/4/2017.
 */

public class Backup extends BackupAgentHelper {

    static final String PREFS_BACKUP_KEY = "backup";
    final String File_Name_Of_Prefrences = "com.adrapps.mytasks" + "_preferences";

    SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(
            this,File_Name_Of_Prefrences);

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this,
                File_Name_Of_Prefrences);
        addHelper(PREFS_BACKUP_KEY, helper);

    }



}



