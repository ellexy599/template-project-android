package com.template.project.core.utils.date;

import android.text.format.DateFormat;

import com.template.project.core.utils.FieldValidator;
import com.template.project.core.utils.LogMe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimestampCalendarUtil {

    private static final String TAG = TimestampCalendarUtil.class.getSimpleName();

    /**
     * Convert millisecond to Calendar object.
     * @param timestamp The String value of timestamp
     * @return The Calendar value of timestamp
     */
    public static Calendar getCalendarFromTimestamp(String timestamp) {
        Calendar calTimestamp = Calendar.getInstance();
        if( FieldValidator.isStringValid(timestamp) ) {
            calTimestamp.setTimeZone(TimeZone.getDefault());
            calTimestamp.setTimeInMillis(Long.parseLong(timestamp));
        }
        return calTimestamp;
    }

    /**
     * Convert Calendar object to timestamp.
     * @param cal The Calendar value to convert to timestamp
     */
    public static long getTimestampFromCalendar(Calendar cal) {
        cal.setTimeZone(TimeZone.getDefault());
        long timestamp = cal.getTimeInMillis();
        return timestamp;
    }

    /**
     * Convert the formattedDate String value to Calendar.
     * @param formattedDate The String to convert Ex. December 8, 2012
     * @return Calendar object of formattedDate
     */
    public static Calendar getCalendarFromFormattedString(String formattedDate) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(new SimpleDateFormat("MMMM d, yyyy", Locale.US).parse(formattedDate));
        } catch (ParseException e) {
            LogMe.e(TAG, "ERROR parsing formattedDate to Calendar " + e.toString());
        }
        return cal;
    }

    /**
     * Convert the formattedDate String value (03/13/1992) to Calendar
     * @param formattedDate The String to convert Ex. 03/13/1992
     * @return Calendar object of formattedDate
     */
    public static Calendar getCalendarFromFormattedFacebookBirthday(String formattedDate) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(formattedDate));
        } catch (ParseException e) {
            LogMe.e(TAG, "ERROR parsing formattedDate to Calendar " + e.toString());
        }
        return cal;
    }

    /**
     * Convert Calendar to String pattern "MMMM d, y"
     * @param cal The Calendar to format
     * @return String value in patttern "MMMM d, y" of Calendar
     */
    public static String getFormattedDateFromCalendar(Calendar cal) {
        String calStrDate = new SimpleDateFormat("MMMM", Locale.US).format(cal.getTime()) + " " +
                cal.get(Calendar.DAY_OF_MONTH) + ", " +
                cal.get(Calendar.YEAR);

        return calStrDate;
    }

    /**
     * Convert Calendar to String pattern passed.
     * @param cal The Calendar to format
     * @param datePattern The String date pattern, i.e. "MMMM d, y", "yyyy-MM-dd"
     * @return String value in of formatted Calendar date, i.e. 2014-12-25
     */
    public static String getFormattedDateFromCalendar(Calendar cal, String datePattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(datePattern, Locale.US);
        String calStrDate = sdf.format(cal.getTime());
        return calStrDate;
    }

    /**
     * Convert Calendar to String pattern "hh:mm a"
     * @param cal The Calendar to format
     * @return String value in patttern "hh:mm a" of Calendar. Ex. 08:00PM
     */
    public static String getFormattedTimeFromCalendar(Calendar cal) {
        String calTime = (String) DateFormat.format("hh:mm a", cal);
        calTime = calTime.replace("pm", "PM");
        calTime = calTime.replace("am", "AM");
        return calTime;
    }

    /**
     * Convert Calendar to String pattern for game schedule ex. 15-Sep-2013 7:00PM
     * @param cal The Calendar to format
     * @return The String value of formatted Calendar value to pattern "15-Sep-2013 7:00PM"
     */
    public static String getGameScheduleDateFormat(Calendar cal) {
        String calTime = (String) DateFormat.format("hh:mm a", cal);
        calTime = calTime.replace("pm", "PM");
        calTime = calTime.replace("am", "AM");

        String strGameSched = cal.get(Calendar.DAY_OF_MONTH) + "-" +
                //String.valueOf(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)) + "-" +
                getMonthShortName(cal) + "-" +
                String.valueOf(cal.get(Calendar.YEAR)) + " " +
                calTime;

        return strGameSched;
    }

    /**
     * Convert Calendar to String pattern. Ex. 15-Sep-2013 08:18:08.248 GMT+08:00
     * @return The String value of formatted Calendar value to pattern "15-Sep-2013 08:18:08.248 GMT+08:00"
     */
    public static String getExactDateUpToMillis(long ts) {
        // String calTime = (String) DateFormat.format("kk:mm:ss", ts);

        Calendar cal = TimestampCalendarUtil.getCalendarFromTimestamp(ts+"");
        String tempCal = cal.get(Calendar.DAY_OF_MONTH) + "-" +
                //String.valueOf(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)) + "-" +
                getMonthShortName(cal) + "-" +
                String.valueOf(cal.get(Calendar.YEAR));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        String tempTime      = sdf.format(new Date(ts)) +  " "
                + sdf.getTimeZone().getDisplayName(false, TimeZone.SHORT);


        String timestampReadable = tempCal + " " + tempTime;
        LogMe.d( TAG, "getExactDateUpToMillis: " + timestampReadable) ;
        return timestampReadable;
    }

    // use this method because Calendar getDisplayName method only supported for Android API level 9 and up
    // get the Calendar display name in short abbreviation e.g. "Jan"
    private static String getMonthShortName(Calendar cal) {
        String monthName = "January";
        int mon = cal.get(Calendar.MONTH);
        switch (mon) {
            case Calendar.JANUARY:
                monthName = "January";
                break;
            case Calendar.FEBRUARY:
                monthName = "February";
                break;
            case Calendar.MARCH:
                monthName = "March";
                break;
            case Calendar.APRIL:
                monthName = "April";
                break;
            case Calendar.MAY:
                monthName = "May";
                break;
            case Calendar.JUNE:
                monthName = "June";
                break;
            case Calendar.JULY:
                monthName = "July";
                break;
            case Calendar.AUGUST:
                monthName = "August";
                break;
            case Calendar.SEPTEMBER:
                monthName = "September";
                break;
            case Calendar.OCTOBER:
                monthName = "October";
                break;
            case Calendar.NOVEMBER:
                monthName = "November";
                break;
            case Calendar.DECEMBER:
                monthName = "December";
                break;
        }
        monthName = monthName.substring(0, 3);
        return monthName;
    }

    /**
     * Format the Calendar to pattern "MMMM d, yyyy"
     */
    public static String formatCalendarDisplay(Calendar cal) {
        return (String) DateFormat.format("MMMMM d, yyyy", cal);
    }

    /**
     * Convert the millisecond to String value format of "mm:ss"
     */
    public static String formatMsToMinsSecs(long timeToDisplay) {
        String strMins      = formatMsToMins(timeToDisplay);
        String strSeconds   = formatMsToSeconds(timeToDisplay);
        if(strMins.length() < 2) {
            strMins = "0" + strMins;
        }
        if(strSeconds.length() < 2) {
            strSeconds = "0" + strSeconds;
        }
        return strMins + ":" + strSeconds;
    }

    /**
     * Convert the millisecond to minutes
     */
    public static String formatMsToMins(long timeToDisplay) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeToDisplay);
        return String.valueOf(minutes);
    }

    /**
     * Convert the millisecond to seconds corresponding with minutes
     */
    public static String formatMsToSeconds(long timeToDisplay) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeToDisplay);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeToDisplay) - (minutes * 60);
        return String.valueOf(seconds);
    }

}
