package edu.wisc.drivesense.utilities;

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
        return String.format("%.2f miles", distance);
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

    public static int convertToSeconds(long miliseconds) {
        return (int) miliseconds / 1000;
    }

}
