package com.adrapps.mytasks.Helpers;

import android.util.Log;

import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {

    public static long dateTimeToMilliseconds(DateTime date){
        Calendar calendar = Calendar.getInstance();
//        int offset = TimeZone.getDefault().getRawOffset();
//        Log.d("offset",String.valueOf(offset));
        calendar.setTimeInMillis(date.getValue());
        return calendar.getTimeInMillis();
    }

    public static DateTime millisecondsToDateTime (long timeInMills){
        return new DateTime(timeInMills,-240);
    }

    public static String timeInMillsToString(long timeInMills){
        SimpleDateFormat format = new SimpleDateFormat("EEEE, d MMM yyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        return format.format(calendar.getTime());
    }

    public static boolean isTomorrow(long timeInMills){
        Calendar ca = Calendar.getInstance();
        Calendar ca1 = Calendar.getInstance();
        ca1.setTimeInMillis(timeInMills);
        int yearToday = ca.get(Calendar.YEAR);
        int monthToday = ca.get(Calendar.MONTH);
        int dayToday = ca.get(Calendar.DAY_OF_MONTH);
        int yearInput = ca1.get(Calendar.YEAR);
        int monthInput = ca1.get(Calendar.MONTH);
        int dayInput = ca1.get(Calendar.DAY_OF_MONTH);
        return yearToday == yearInput && monthToday == monthInput && dayInput == dayToday + 1;
    }

    public static boolean isInInThePast(long timeInMills){
        Calendar ca = Calendar.getInstance();
        Calendar ca1 = Calendar.getInstance();
        ca1.setTimeInMillis(timeInMills);
        int yearToday = ca.get(Calendar.YEAR);
        int monthToday = ca.get(Calendar.MONTH);
        int dayToday = ca.get(Calendar.DAY_OF_MONTH);
        int yearInput = ca1.get(Calendar.YEAR);
        int monthInput = ca1.get(Calendar.MONTH);
        int dayInput = ca1.get(Calendar.DAY_OF_MONTH);
        return yearToday > yearInput ||
                yearToday == yearInput && monthToday > monthInput ||
                yearToday == yearInput && monthToday == monthInput && dayToday > dayInput;
    }



}
