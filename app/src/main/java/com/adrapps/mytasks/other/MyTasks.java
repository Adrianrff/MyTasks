package com.adrapps.mytasks.other;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.adrapps.mytasks.R;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.PrintWriter;
import java.io.StringWriter;


public class MyTasks extends Application {



   public static RefWatcher getRefWatcher(Context context) {
      MyTasks application = (MyTasks) context.getApplicationContext();
      return application.refWatcher;
   }

   private RefWatcher refWatcher;


    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
       refWatcher = LeakCanary.install(this);
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
                PendingIntent pendingIntent = PendingIntent.getActivity(MyTasks.this,
                        0, mailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(MyTasks.this)
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
