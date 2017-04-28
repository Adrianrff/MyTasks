package com.adrapps.mytasks;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.crash.FirebaseCrash;

import org.acra.*;
import org.acra.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setupDebugNotification();
    }

    private void setupDebugNotification() {
        UncaughtExceptionFilter.setUncaughtExceptionHandler(new UncaughtExceptionFilter.Action<Throwable>() {
            @Override
            public void run(Throwable arg0) {
                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setType("message/rfc822");
                mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"adrianrff@gmail.com"});
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Exception stacktrace");
                mailIntent.putExtra(Intent.EXTRA_TEXT, getFullErrorMessage(arg0));
                PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.this,
                        0, mailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(MyApplication.this)
                        .setSmallIcon(R.drawable.ic_bug_report)
                        .setContentTitle(getString(R.string.bug_report_notification_title))
                        .setContentText(arg0.getMessage())
                        .setContentIntent(pendingIntent);
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
                        (int) System.currentTimeMillis(), notifyBuilder.build());
//                FirebaseCrash.report(arg0);

            }
        });
    }

    private String getFullErrorMessage(Throwable error) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        error.printStackTrace(printWriter);
        printWriter.flush();
        return writer.toString();
    }
}
