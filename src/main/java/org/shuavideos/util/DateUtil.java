package org.shuavideos.util;

import org.joda.time.DateTime;

import java.util.Date;

public class DateUtil {

    public static final String dateFormat = "yyyy-MM-dd";

    public static Date addDateMinutes(Date date, int minutes){
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMinutes(minutes).toDate();

    }

}
