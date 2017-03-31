package com.adrapps.mytasks.Helpers;

import com.google.api.client.util.DateTime;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Adrian Flores on 28/3/2017.
 */

public class DateHelper {

    public static long DateTimeToMilliseconds(DateTime date){
        Calendar calendar = Calendar.getInstance();
        int offSet = TimeZone.getDefault().getRawOffset();

        return 0;
    }
}
