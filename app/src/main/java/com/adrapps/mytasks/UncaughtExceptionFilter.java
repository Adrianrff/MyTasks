package com.adrapps.mytasks;

/**
 * Created by Adrian Flores on 14/4/2017.
 */

public class UncaughtExceptionFilter  {

    private static Thread.UncaughtExceptionHandler defaultUEH;

    public static void setUncaughtExceptionHandler(final Action<Throwable> notifyException) {
        if (defaultUEH == null) {
            defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    notifyException.run(ex);
                    defaultUEH.uncaughtException(thread, ex);
                }
            });
        }
    }

    public interface Action<T> {
        void   run(T arg0);
    }
}
