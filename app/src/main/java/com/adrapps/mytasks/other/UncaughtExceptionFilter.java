package com.adrapps.mytasks.other;

class UncaughtExceptionFilter  {

    private static Thread.UncaughtExceptionHandler defaultUEH;

    static void setUncaughtExceptionHandler(final Action<Throwable> notifyException) {
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

    interface Action<T> {
        void   run(T arg0);
    }
}
