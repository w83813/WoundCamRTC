package org.itri.woundcamrtc.helper;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class TimeHelper {


    public static final int HOURS_OF_ONE_DAY = 24;
    public static final int MINUTES_OF_ONE_HOUR = 60;
    public static final int MINUTES_OF_ONE_DAY = 60 * 24;
    public static final int SECONDS_OF_ONE_HOUR = 3600;
    public static final int SECONDS_OF_ONE_DAY = 24 * 3600;
    public static final int SECONDS_OF_ONE_WEEK = 7 * 24 * 3600;
    public static final int SECONDS_OF_TWO_WEEKS = 14 * 24 * 3600;
    public static final int SECONDS_OF_FOUR_WEEKS = 28 * 24 * 3600;
    public static final int SECONDS_OF_NINETY_DAYS = 90 * 24 * 3600;

    private static DateFormat sFormatterInSec = createUTCDateFormatter("yyyy-MM-dd HH:mm:ss");
    private static DateFormat sFormatterInMin = createUTCDateFormatter("yyyy-MM-dd HH:mm");
    private static DateFormat sFormatterInDay = createUTCDateFormatter("yyyy-MM-dd");

    /**
     * Creates a DateFormat that assumes the incoming date is in UTC time.
     *
     * @param dateFormat - the format string, e.g. "yyyy-MM-dd".
     */
    public static DateFormat createUTCDateFormatter(String dateFormat) {
        DateFormat formatter = new SimpleDateFormat(dateFormat);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter;
    }

    /**
     * Get the current timestamp in milliseconds.
     */
    public static long getNowTimestampInMilliseconds() {
        Date date = new Date();
        return date.getTime();
    }


    /*
     * Example input: 2013-06-08 23:59:59 and 2013-06-09 00:00:01
     * Example output: 1
     */
    public static long getDiffInDays(Date date1, Date date2) {
        long ts1 = stringInDaysToTimestampInSeconds(dateToStringInDays(date1));
        long ts2 = stringInDaysToTimestampInSeconds(dateToStringInDays(date2));
        return Math.abs(ts1 - ts2) / SECONDS_OF_ONE_DAY;
    }

    /*
     * Example input: timestamp representing 2013-06-08 23:59:59 and 2013-06-09 00:00:01
     * Example output: 1
     */
    public static long getDiffInDays(long ts1, long ts2) {
        return getDiffInDays(timestampInSecondsToDate(ts1), timestampInSecondsToDate(ts2));
    }

    /**
     * Get the current timestamp in seconds.
     */
    public static long getNowTimestampInSeconds() {
        return getNowTimestampInMilliseconds() / 1000;
    }

    /**
     * Convert a date time string (e.g., "2011-06-07 19:12:18") to a Date object.
     */
    public static Date stringToDate(String s, DateFormat formatter) {
        try {
            return formatter.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    public static long getStartTimestampOfADay(long timestamp) {
        return stringInDaysToTimestampInSeconds(
                dateToStringInDays(timestampInSecondsToDate(timestamp)));
    }

    public static Date stringInSecondsToDate(String s) {
        return stringToDate(s, sFormatterInSec);
    }

    public static Date timestampInSecondsToDate(long timestamp) {
        return new Date(timestamp * 1000);
    }

    public static String dateToStringInDays(Date date) {
        return sFormatterInDay.format(date);
    }

    public static String dateToStringInMinutes(Date date) {
        return sFormatterInMin.format(date);
    }

    /**
     * Convert a date time string (e.g., "2011-06-07 19:12:18") to a timestamp
     * in milliseconds (i.e., the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    public static long stringToTimestampInMilliseconds(String s, DateFormat formatter) {
        Date d = stringToDate(s, formatter);
        return d == null ? 0 : d.getTime();
    }

    /**
     * Convert a date time string (e.g., "2011-06-07 19:12:18") to a timestamp
     * in seconds (i.e., the number of seconds since January 1, 1970, 00:00:00 GMT.
     */
    public static long stringToTimestampInSeconds(String s, DateFormat formatter) {
        return stringToTimestampInMilliseconds(s, formatter) / 1000;
    }

    public static long stringInSecondsToTimestampInSeconds(String s) {
        return stringToTimestampInSeconds(s, sFormatterInSec);
    }

    public static long stringInDaysToTimestampInSeconds(String s) {
        return stringToTimestampInSeconds(s, sFormatterInDay);
    }

    public static long stringInMinutesToTimestampInSeconds(String s) {
        return stringToTimestampInSeconds(s, sFormatterInMin);
    }

    /**
     * Calculate a time decay penalty score in (0.3, 1).
     * <p>
     * The demotion function used here is based on the exponential decay and
     * maps the value to interval (0.3, 1).
     * <p>
     * D(x) = 0.3 + 0.7 * (1 - 0.1)^delta(t)
     * <p>
     * where 0.3 the lower bound of the penaly score, 0.7 is the initial value
     * and 0.1 (10%) is the percent decrease over a day, and delta(t) is the
     * days between a pin's creation time and now.
     * <p>
     * Search "y = 0.3 + 0.7 * (1 - 0.1)^x" at google to visualize the function.
     * <p>
     * Examples:
     * D(0.0)   = 1.0
     * D(0.5)   = 0.9640783086353595
     * P(1.0)   = 0.9299999999999999
     * P(2.0)   = 0.8670000000000000
     * P(3.0)   = 0.810300000000000
     * P(4.0)   = 0.7592699999999999
     * P(5.0)   = 0.7133430000000001
     * P(10.0)  = 0.54407490807
     * P(20.0)  = 0.38510365821339854
     * P(30.0)  = 0.32967381079265135
     * P(365.0) = 0.300000000000000
     * <p>
     * Args:
     * eventTimestamp: the timestamp when the event took place.
     * nowTimestamp: the current timestamp.
     * <p>
     * Returns:
     * The time decay penalty of the event.
     */
    public static double calculateTimeDecayPenalty(long eventTimestamp, long nowTimestamp) {
        if (nowTimestamp == 0) {
            return 1.0;
        }

        if (eventTimestamp == 0) {
            return 0.3;
        }

        // Calculate the days between the event and now.
        double days = (nowTimestamp - eventTimestamp) / 86400.0;
        return calculateTimeDecayPenalty(0.3, 0.7, 0.9, days);
    }

    /**
     * Calculate the arbitary time decay: y = base + boost * (param)^days
     */
    public static double calculateTimeDecayPenalty(double base, double boost,
                                                   double param, double days) {
        if (days <= 0.0) {
            return base + boost;
        }
        return base + boost * Math.pow(param, days);
    }

    /**
     * Return the current wall-clock time in nanoseconds.
     */
    public static long getNowTimestampInNanos() {
        return System.currentTimeMillis() * 1000 * 1000;
    }

    public static long getSystemTime() {
        return Calendar.getInstance(Locale.TAIWAN).getTimeInMillis();
    }

    /**
     * 將伺服器所傳過來的時間格式轉為long值
     * example : 2016-05-16 18:56:21
     */

    public static long getTimeFormat(String formatTime) {

        if (TextUtils.isEmpty(formatTime) || !formatTime.contains("-")) {
            return -1;
        }
        String[] eachValue = formatTime.split(" ");

        String[] dateValue = eachValue[0].split("-");
        String[] timeValue = eachValue[1].split(":");

        Calendar mSystemLocalTime = Calendar.getInstance();
        mSystemLocalTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        mSystemLocalTime.set(Calendar.YEAR, Integer.valueOf(dateValue[0]));
        mSystemLocalTime.set(Calendar.MONTH, Integer.valueOf(dateValue[1]) - 1);
        mSystemLocalTime.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateValue[2]));
        mSystemLocalTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeValue[0]));
        mSystemLocalTime.set(Calendar.MINUTE, Integer.valueOf(timeValue[1]));
        mSystemLocalTime.set(Calendar.SECOND, Integer.valueOf(timeValue[2]));
        mSystemLocalTime.set(Calendar.MILLISECOND, 0);
        if (timeValue.length == 4) {
            mSystemLocalTime.set(Calendar.MILLISECOND, Integer.valueOf(timeValue[3]));
        }
        return mSystemLocalTime.getTimeInMillis();
    }

    /**
     * 將時間轉為伺服器能接受的格式
     */
    public String getTimeFormat(long formatTime) {
        SimpleDateFormat sdf_MM_dd = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.TAIWAN);
        return sdf_MM_dd.format(formatTime);
    }

    public static String getDateFormat(long formatTime) {
        SimpleDateFormat sdf_MM_dd = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        return sdf_MM_dd.format(formatTime);
    }

    public static String getDateTimeFormat(long formatTime) {
        SimpleDateFormat sdf_MM_dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
        return sdf_MM_dd.format(formatTime);
    }

    //取得本日凌晨12點00分01秒至晚上11點59分59秒的UTC時間範圍
    public long[] getEventTimeOneDayRange(long eventTime) {

        long[] localTimeRange = new long[2];

        Calendar mSystemLocalTime = Calendar.getInstance(Locale.TAIWAN);
        mSystemLocalTime.setTimeInMillis(eventTime);

        //設定時間為本日凌晨12點00分00秒
        mSystemLocalTime.set(Calendar.HOUR_OF_DAY, 0);
        mSystemLocalTime.set(Calendar.MINUTE, 0);
        mSystemLocalTime.set(Calendar.SECOND, 0);
        mSystemLocalTime.set(Calendar.MILLISECOND, 0);


        localTimeRange[0] = mSystemLocalTime.getTimeInMillis();


        //設定時間為本日晚上11點59分59秒
        mSystemLocalTime.set(Calendar.HOUR_OF_DAY, 23);
        mSystemLocalTime.set(Calendar.MINUTE, 59);
        mSystemLocalTime.set(Calendar.SECOND, 59);
        mSystemLocalTime.set(Calendar.MILLISECOND, 59);


        localTimeRange[1] = mSystemLocalTime.getTimeInMillis();
        return localTimeRange;

    }

}
