package edu.wisc.drivesense.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.orm.SugarRecord;


/**
 * The only model object that gets put on the map either on the app or on the site.
 *
 * Created from GPS coordinates or nothing.
 */
public class MappableEvent extends SugarRecord<MappableEvent> {
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
    @SerializedName("time_stamp_end")
    public long timestampEnd;

    @Expose
    public double latitude;

    @Expose
    public double longitude;

    @Expose
    public double latitude_end;

    @Expose
    public double longitude_end;

    @Expose
    public double speed;

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

    public MappableEvent(Reading gps) {
        this();

        if (gps.type != Reading.Type.GPS)
            throw new NullPointerException();

        timestamp = gps.timestamp;

        speed = gps.values[0];
        latitude = gps.values[1];
        longitude = gps.values[2];

        type = Type.GPS;
    }

    public MappableEvent(Reading gps_start, Reading gps_end, DrivingPattern pattern) {
        this(gps_start);

        latitude_end = gps_end.values[1];
        longitude_end = gps_end.values[1];

        timestamp = pattern.start;
        timestampEnd = pattern.end;
        type = pattern.type;
        score = pattern.score;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(" date: " + timestamp);
        sb.append(" type: " + type);
        sb.append(" lat: " + latitude);
        sb.append(" long: " + longitude);
        sb.append(" score: " + score);

        return sb.toString();
    }
}