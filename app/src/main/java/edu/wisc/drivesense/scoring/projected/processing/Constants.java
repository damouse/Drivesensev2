package edu.wisc.drivesense.scoring.projected.processing;

public class Constants {

    public static final double kEarthGravity = 9.80665; /*m^2/s*/

    /*for gps*/
    public static final double kSmallEPSILON = 1e-8;
    public static final double kEarthRadius = 6371 * 1000; /*m*/

    public static final double kMeterToMile = 0.000621371;
    public static final double kMeterPSToMilePH = 2.23694;

    public static final double kGPSMinimumDistance = 3; /*m*/

    /**/
    public static final String kInputSeperator = "\t";
    public static final String kOutputSeperator = "\t";

    public static final double PERCENT_ = 0.7;
    
    
    
    
    
    /*====================database related======================================================*/
    

    /*
    public static final String kDatabaseName = "drivesense";
    public static final String kDatabaseURL = "jdbc:mysql://144.92.202.85:3306/"+kDatabaseName+"?useUnicode=true&characterEncoding=GBK";
    public static final String kDatabaseUserName = "root";
    public static final String kDatabasePassword = "soekris;";
    */

    public static final String kDatabaseName = "drivesense";
    public static final String kServerIP = "71.87.61.229";
    public static final String kDatabaseURL = "jdbc:mysql://" + kServerIP + ":3306/" + kDatabaseName + "?useUnicode=true&characterEncoding=GBK";
    public static final String kDatabaseUserName = "root";
    public static final String kDatabasePassword = "kanglei";


    /**/
    public static final String kMagnetic = "MAGNETIC_FIELD";
    public static final String kGyroscope = "GYROSCOPE";
    public static final String kOrientation = "ORIENTATION";
    public static final String kRotation = "ROTATION_VECTOR";
    public static final String kLinear = "LINEAR_ACCELERATION";
    public static final String kAccelerometer = "ACCELEROMETER";
    public static final String kGravity = "GRAVITY";
    public static final String kLight = "LIGHT";
    public static final String kGPS = "gps";
    

    /*====================database related======================================================*/

}
