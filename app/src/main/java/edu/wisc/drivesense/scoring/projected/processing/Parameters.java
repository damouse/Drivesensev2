package edu.wisc.drivesense.scoring.projected.processing;

public class Parameters {

	
	/*=============StopIntervalExtraction related=============================*/
    /*comes from the stationary sensor value
	 * even when the phone is statistic, the sensor value changes around >0.12 in a scale of 0.04*3
	 * sensor value is discrete 0.04 
	 * */
	
	/*fix parameter first*/

    public static final int kAccelerometerSlidingWindowSize = 50; /*for all accelerometer data processing old: 15*/
    public static final double kMovementDetectionThreshold = 0.01/*0.15, 0.05*/;
    public static final double kStraightDetectionThreshold = 0.03;

    /*
     * usually from .1 to .7
     * */
    public static final double kExponentialMovingAverageAlpha = 0.15;
    //public static final double kExponentialMovingAverageAlpha = 0.1;
	
	/*=============Acceleration Constant Deceleration====================*/

    public static final double kAccelerationThreshold = 0.2;
    public static final double kDecelerationThreshold = -0.2;
    public static final int kMovementDetectionWindowSize = 10;

    public static final int kDimensionXY = 1;
    public static final int kDimensionYZ = 2;
    public static final int kDimensionXZ = 3;

}
