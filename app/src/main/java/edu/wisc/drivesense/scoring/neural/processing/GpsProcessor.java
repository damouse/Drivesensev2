package edu.wisc.drivesense.scoring.neural.processing;

import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;

import static edu.wisc.drivesense.scoring.neural.processing.GeneralProcessor.normalize;

/**
 * Created by Damouse on 12/12/2014.
 */
public class GpsProcessor {
    //1 m/s = this many miles per hour
    public static final double MPH_PER_MS = 2.23694;
    //how many past GPS readings should we search through to determine speed and bearing?
    public static int memoryWindow = 1;
    //Speed is normalized to the range [-1, 1]. Theses variables determine the range of the
    //incoming speed values in MPH.
    private static double minSpeedNormalization = -10;
    private static double maxSpeedNormalization = 10;
    //Which angle to map to the top of the normalized range
    private static double angleNormalization = 45;

    /**
     * Main entry point for GPS preprocessing. Calcualtes GPS speed and angle.
     * <p/>
     * Requires a window of the last chunk of readings to determine GPS speed difference. This is
     * to account for the fact that GPS updates may occur less frequently than the period sampling size--
     * you may get more
     *
     * @return
     */
    public static void processGps(DataSetInput data, TimestampQueue inputMemory) {
        data.speedDelta = calculateSpeedChange(inputMemory, data.preProcessedGPS[0]);
        data.bearingDelta = calculateBearing(inputMemory, data.preProcessedGPS);
    }

    /**
     * Returns the change in speed over the GPS coordinates as a double.
     *
     * @return
     */
    private static double calculateSpeedChange(TimestampQueue memory, double currentSpeed) {
        //if theres no memory, return current speed
        if (memory.size() < 1)
            return currentSpeed;

        DataSetInput lastCoordinate;

        //try and stretch back memoryWindow inputs to calculate speed change
        if (memory.size() < memoryWindow)
            lastCoordinate = (DataSetInput) memory.peek();
        else
            lastCoordinate = (DataSetInput) memory.peekFromTail(memoryWindow - 1);

        double speedChange = currentSpeed - lastCoordinate.preProcessedGPS[0];
        speedChange = kphToMph(speedChange);
        return normalize(speedChange, -1, 1, minSpeedNormalization, maxSpeedNormalization);
    }

    /**
     * Calculate the bearing for this set of GPS coordinates.
     */
    private static double calculateBearing(TimestampQueue memory, double[] currentGpsValues) {
        if (memory.size() == 0)
            return 0;

        double currentLat = currentGpsValues[1];
        double currentLon = currentGpsValues[2];

        double pastLat = currentLat;
        double pastLon = currentLon;

        DataSetInput rememberedInput;

        if (memory.size() < memoryWindow)
            rememberedInput = (DataSetInput) memory.peek();
        else
            rememberedInput = (DataSetInput) memory.peekFromTail(memoryWindow - 1);

        pastLat = rememberedInput.preProcessedGPS[1];
        pastLon = rememberedInput.preProcessedGPS[2];

        double diffLon = currentLon - pastLon;
        double y = Math.sin(diffLon) * Math.cos(currentLat);
        double x = Math.cos(pastLat) * Math.sin(currentLat) - Math.sin(pastLat) * Math.cos(currentLat) * Math.cos(diffLon);

        double radians = (Math.atan2(y, x));
        double bearing = Math.toDegrees(radians);
        double angle = (360 - ((bearing + 360) % 360));

        return normalize(angle, 0, 1, 0, Math.abs(angleNormalization));
    }


    /* Private Convenience Methods */
    private static double kphToMph(double kilometersPerHour) {
        return kilometersPerHour * MPH_PER_MS;
    }
}
