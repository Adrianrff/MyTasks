package com.adrapps.mytasks.helpers;

import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {


    public static long dateTimeToMilliseconds(DateTime date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getValue());
        return calendar.getTimeInMillis();
    }

    public static DateTime millisecondsToDateTime (long timeInMills){
        int offSet = TimeZone.getDefault().getRawOffset();
        return new DateTime(timeInMills);
//        return new DateTime(timeInMills, offSet/60000);
    }

    public static String timeInMillsToString(long timeInMills){
        Calendar calendar = Calendar.getInstance();
        Calendar calToday = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        String format;
        if (calendar.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)){
            format = "EEEE, d MMMM";
        } else {
            format = "EEEE, d MMMM, yyyy";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static String timeInMillsToFullString(long timeInMills){
        Calendar calendar = Calendar.getInstance();
        Calendar calToday = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        String format;
        if (calendar.get(Calendar.YEAR) == calToday.get(Calendar.YEAR)){
            format = "d MMM h:mm a";
        } else {
            format = "d MMM yyyy h:mm a";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static String timeInMillsToSimpleTime(long timeInMills){
        Calendar calendar = Calendar.getInstance();
        Calendar calToday = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        String format = "h:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static String timeInMillsToDay(long timeInMills){
        Calendar calendar = Calendar.getInstance();
        Calendar calToday = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        String format = "EEEE";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static String timeInMillsToDayOfMonth(long timeInMills){
        Calendar calendar = Calendar.getInstance();
        Calendar calToday = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMills);
        String format = "d";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(calendar.getTime());
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
