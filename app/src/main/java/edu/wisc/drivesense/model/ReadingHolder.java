package edu.wisc.drivesense.model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

/**
 * Created by Damouse on 12/16/2014.
 *
 * TEMPORARY CLASS for loading saved trips to the device! DONT USE THIS!
 */
public class ReadingHolder extends SugarRecord<ReadingHolder> {
    public long timestamp;

    @Ignore
    public double x;
    public double y;
    public double z;

    public int dimension;

    public double degrees;

    public Reading.Type type;

    public ReadingHolder() {

    }

    public ReadingHolder(Reading reading) {
        timestamp = reading.timestamp;
        x = reading.values[0];
        y = reading.values[1];
        z = reading.values[2];
        dimension = reading.dimension;
        type = reading.type;
    }

    public Reading getReading() {
        double values[] = {x, y, z};
        return new Reading(values, timestamp, type);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("time: " + timestamp);
        sb.append(" values: " + x + " " + y + " " + z);
        sb.append(" dimension: " + dimension);
        sb.append(" type: " + type);

        return sb.toString();
    }

}
