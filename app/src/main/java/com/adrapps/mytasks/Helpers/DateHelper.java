package com.adrapps.mytasks.Helpers;

import com.google.api.client.util.DateTime;
import java.util.Calendar;
import java.util.TimeZone;

public class DateHelper {

    public static long DateTimeToMilliseconds(DateTime date){
        Calendar calendar = Calendar.getInstance();
        int offset = TimeZone.getDefault().getRawOffset();
        calendar.setTimeInMillis(date.getValue() + offset);
        return calendar.getTimeInMillis();
    }
}
