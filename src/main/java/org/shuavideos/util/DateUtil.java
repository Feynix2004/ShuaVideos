package org.shuavideos.util;

import org.joda.time.DateTime;

import java.util.Date;

public class DateUtil {

    public static final String dateFormat = "yyyy-MM-dd";

    public static Date addDateMinutes(Date date, int minutes){
        DateTime dateTime = new DateTime(date);
        return dateTime.plusMinutes(minutes).toDate();

    }

    /**
     * 对日期的【天】进行加/减
     *
     * @param date 日期
     * @param days 天数，负数为减
     * @return 加/减几天后的日期
     */
    public static Date addDateDays(Date date, int days) {
        DateTime dateTime = new DateTime(date);
        return dateTime.plusDays(days).toDate();
    }
}
