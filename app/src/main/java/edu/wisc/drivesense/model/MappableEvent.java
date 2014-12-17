package edu.wisc.drivesense.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.orm.SugarRecord;


/**
 * The only model object that gets put on the map either on the app or on the site.
 *
 * Created from GPS coordinates or nothing.
 */
public class MappableEvent extends SugarRecord<User> {
    public static enum Type {
        ACCELERATION,
        BRAKE,
        TURN,
        LANE_CHANGE,
        STOP,
        GPS,
        UNSET
    }

    @Expose
    @SerializedName("time_stamp")
    public long timestamp;

    @Expose
    public double latitude;

    @Expose
    public double longitude;

    @Expose
    public float speed;

    @Expose
    public Type type;

    @Expose
    public double score;

    public Trip trip;


    public MappableEvent() {
        timestamp = 0;
        latitude = 0;
        longitude = 0;
        speed = 0;
        type = Type.UNSET;
        score = 0;
    }

//    public MappableEvent(Location loc) {
//        this();
//        latitude = loc.getLatitude();
//        longitude = loc.getLongitude();
//        timestamp = new Date();
//
//        //2.2369 mph for 1 m/s
//        speed = loc.getSpeed() * 2.23694f;
//    }

//    public MappableEvent(Reading reading) {
//        this();
//
//        latitude = reading.values[1];
//        longitude = reading.values[2];
//        speed = (float) reading.values[0] * 2.23694f;
//
//        timestamp = reading.timestamp;
//    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(" date: " + timestamp);
        sb.append(" lat: " + latitude);
        sb.append(" long: " + longitude);

        return sb.toString();
    }
}