package edu.wisc.drivesense.scoring.neural.modelObjects;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Damouse on 12/13/2014.
 */
public class TrainingSet {
    public TimestampQueue acceleration = new TimestampQueue();
    public TimestampQueue gyroscope = new TimestampQueue();
    public TimestampQueue gravity = new TimestampQueue();
    public TimestampQueue magnet = new TimestampQueue();
    public TimestampQueue gps = new TimestampQueue();
    public TimestampQueue labels = new TimestampQueue();


    /* Utility */
    public ArrayList<TimestampQueue> getAllQueues() {
        return new ArrayList<TimestampQueue>(Arrays.asList(acceleration, magnet, gravity, gyroscope, gps));
    }

    public TrainingSet copy() {
        TrainingSet copy = new TrainingSet();

        copy.acceleration = new TimestampQueue(acceleration);
        copy.gyroscope = new TimestampQueue(gyroscope);
        copy.gravity = new TimestampQueue(gravity);
        copy.magnet = new TimestampQueue(magnet);
        copy.gps = new TimestampQueue(gps);
        copy.labels = new TimestampQueue(labels);

        return copy;
    }
}
