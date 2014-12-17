package edu.wisc.drivesense.utilities;

/**
 * Basic Utils. Starting out as a migration of methods from existing code,
 * organization to follow.
 */
public class Utils {
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
}
