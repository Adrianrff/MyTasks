package com.adrapps.mytasks.Helpers;

import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {

    public static long DateTimeToMilliseconds(DateTime date){
        Calendar calendar = Calendar.getInstance();
        int offset = TimeZone.getDefault().getRawOffset();
        calendar.setTimeInMillis(date.getValue() + offset);
        return calendar.getTimeInMillis();
    }

    public static String timeInMillsToStringSimpleFormat(long timeInMills){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) +
                "/" +
                String.valueOf(calendar.get(Calendar.MONTH) + 1) +
                "/" +
                String.valueOf(calendar.get(Calendar.YEAR));
    }

    public static DateTime millisecondsToDateTime (long timeInMills){
        return new DateTime(timeInMills - TimeZone.getDefault().getRawOffset());

    }

    public static String timeInMillsToString(long timeInMills){
        SimpleDateFormat format = new SimpleDateFormat("EEEE, d MMM yyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        return format.format(calendar.getTime());

    }

}
