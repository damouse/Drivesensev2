package edu.wisc.drivesense.model;

import android.location.Location;

import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;

/**
 * Object that represents one sensor "value" at a given point in time.
 *
 * Value is in quotes because Readings can also be truth labels or processed
 * sensor values.
 *
 * Expect values[] to contain [x, y, z] for a sensor, with the only major exception
 * being GPS, in which case they're [speed, lat, long]
 *
 * Oh, man. Huuuuge issue with timestamps not being accurate...
 * See here: https://code.google.com/p/android/issues/detail?id=78858
 *
 * @author Damouse
 */
public class Reading extends TimestampSortable {
    public enum Type {
        ACCELERATION,
        LINEAR_ACCELERATION,
        GYROSCOPE,
        GRAVITY,
        MAGNETIC,
        LABEL,
        GPS
    }


    public long timestamp;
    public double[] values;
    public int dimension;

    public double degrees;

    public Type type;


    /* Consctructors */
    public Reading(double[] values, long timestamp, Type type) {
        setValues(values);
        this.timestamp = timestamp;
        this.type = type;
    }

    public Reading(Location location) {
        this.type = Type.GPS;

        values = new double[3];

        values[0] = location.getSpeed();
        values[1] = location.getLatitude();
        values[2] = location.getLongitude();

        timestamp = location.getTime();
        dimension = 3;
    }

	public Reading(Reading other) {
		type = other.type;
        timestamp = other.timestamp;
        setValues(other.values);
    }

    /**
     * Create from a file input line.
     */
    public Reading(String line, Type type) {
        String[] data = line.split(",");
        values = new double[data.length - 1];

        timestamp = Long.parseLong(data[0]);
        values[0] = Double.parseDouble(data[1]);
        values[1] = Double.parseDouble(data[2]);
        values[2] = Double.parseDouble(data[3]);

        dimension = 3;

        this.type = type;
    }


    /* Accessors */
    public void setValues(double[] newValues) {
        dimension = newValues.length;
        values = new double[dimension];

        for (int i = 0; i < values.length; i++)
            values[i] = newValues[i];
    }
	
	//Converts to floats for processing by getRotationMatrix
	public float[] getFloatValues() {
        float[] ret = new float[dimension];

        for (int i = 0; i < dimension; i++) {
            ret[i] = (float) values[i];
		}

		return ret;
	}
	
	//processes incoming float arrays as doubles for storage
	public void setFloatValues(float[] newValues, int newDim) {
		double[] ret = new double[newDim];

		for (int i = 0; i < newDim; i ++) {
			ret[i] = (double) newValues[i];
		}

		values = ret;
        dimension = newDim;
    }

    public void calculateDegrees() {
        degrees = Math.asin(values[0] / Math.sqrt(Math.pow(values[0], 2) + Math.pow(values[1], 2)));
    }

    /* Inherited from TimestampSortable */
    public long getTime() {
        return timestamp;
    }
}
