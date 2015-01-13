package edu.wisc.drivesense.model;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

/**
 * Class represents one "trip," or one session or recorded driving data.
 * Sensor information is recorded for each trip and saved along with it.
 *
 * @author Damouse
 */
public class Trip extends SugarRecord<Trip> {
    private static final String TAG = "Trip";

    @Expose
    public int trip_id;

    @Expose
    public String name;

    @Expose
    @SerializedName("time_stamp")
    public long timestamp;

    @Expose
    public int duration; //seconds

    @Expose
    public double distance; //miles

    @Expose
    public float scoreTurns;

    @Expose
    public float scoreBrakes;

    @Expose
    public float scoreAccels;

    @Expose
    public float scoreLaneChanges;

    @Expose
    @SerializedName("scoreAverage")
    public int score;

    public User user;

    @Ignore
    public boolean scored;
    public int numAccels;
    public int numBrakes;
    public int numTurns;
    public int numLaneChanges;
    @Ignore
    private boolean uploaded;

    /* Constructors */
    public Trip() {
        trip_id = -1;
        scored = false;
        uploaded = false;
        timestamp = new Date().getTime();

        numAccels = 0;
        numBrakes = 0;
        numTurns = 0;
        numLaneChanges = 0;
    }


    /* Boilerplate Java Overrides */
    public boolean equals(Object obj) {
        if (!(obj instanceof Trip))
            return false;
        if (obj == this)
            return true;

        Trip other = (Trip) obj;
        return other.id == this.id;
    }

    public String toString() {
        //count patterns- WARNING- this WILL be expensive!
        int patternCount = 0;
        int gpsCount = 0;

        StringBuilder sb = new StringBuilder();

        sb.append("id: " + id);
        sb.append(" name: " + name);
        sb.append(" date: " + timestamp);
        sb.append(" distance: " + distance);
        sb.append(" duration: " + duration);

        sb.append(" GPS: " + gpsCount);

        sb.append("\nnumber of total patterns: " + patternCount);

        //sb.append("\nnumber of brakes:" + brakes.size());
        sb.append("\naverage score of brakes: " + scoreBrakes);

        //sb.append("\nnumber of accelerations:" + accelerations.size());
        sb.append("\naverage score of accelerations: " + scoreAccels);

        //sb.append("\nnumber of turns:" + turns.size());
        sb.append("\naverage score of turns: " + scoreTurns);

        //sb.append("\nnumber of lanechanges:" + laneChanges.size());
        sb.append("\naverage score of lanechanges: " + scoreLaneChanges);

        sb.append("\n\nTotal Score: " + score);

        return sb.toString();
    }

    /** NOT async! */
    public List<MappableEvent> getEvents() {
        return MappableEvent.find(MappableEvent.class, "trip = ? order by timestamp asc", "" + getId());
    }


    /* Accessors */
    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }
}
