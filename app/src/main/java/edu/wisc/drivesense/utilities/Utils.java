package edu.wisc.drivesense.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Basic Utils. Starting out as a migration of methods from existing code,
 * organization to follow.
 */
public class Utils {
    public static final double FEET_T0_MILES = 0.000189394;
    public static final double MILES_TO_FEET = 5280;
    public static final double METERS_TO_FEET = 3.28084;
    public static final double DECIMAL_DEGREE_TO_FEET = 364829.396;

    /* Formatting */
    public static String formatDistance(double distance) {
        if(distance < 5)
            return String.format("%.2f miles", distance);
        else
            return String.format("%.0f miles", distance);
    }

    public static String formatDuration(int duration) {
        int seconds = duration;
        int minutes = 0;
        int hours = 0;

        minutes = seconds / 60;

        if (minutes == 0)
            return "" + seconds + " seconds";

        seconds = seconds % 60;
        hours = minutes / 60;

        if (hours == 0)
            return "" + minutes + " minutes, " + seconds + " seconds";

        minutes = minutes % 60;
        return "" + hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
    }

    /**
     * Same as the method above, but omits the smaller labels.
     *
     * Ex: 'Y minutes' instead of 'Y minutes, Z seconds'
     * @param duration
     * @return
     */
    public static String formatSignificantDuration(int duration) {
        int seconds = duration;
        int minutes = 0;
        int hours = 0;

        minutes = seconds / 60;

        if (minutes == 0)
            return "" + seconds + " seconds";

        hours = minutes / 60;

        if (hours == 0)
            return "" + minutes + " minutes";

        return "" + hours + " hours";
    }

    public static int convertToSeconds(long miliseconds) {
        return (int) miliseconds / 1000;
    }


    /* Date */
    public static String dayOfWeek(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat("EEEE").format(date);
    }

    /**
     * Given a start timestamp and a duration in seconds, return a string that represents the time
     * as a range
     */
    public static String startEndTime(long timestamp, int seconds) {
        String dateFormat = "hh:mma";
        Date date = new Date(timestamp);
        String start = new SimpleDateFormat(dateFormat).format(date);

        date = new Date(timestamp + seconds);
        String end = new SimpleDateFormat(dateFormat).format(date);

        return start + "-" + end;
    }

    public static String startTime(long timestamp) {
        String dateFormat = "hh:mma";
        Date date = new Date(timestamp);
        String start = new SimpleDateFormat(dateFormat).format(date);
        return start;
    }
}
