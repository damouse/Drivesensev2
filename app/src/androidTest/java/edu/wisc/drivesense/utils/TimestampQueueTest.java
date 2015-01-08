package edu.wisc.drivesense.utils;

import android.util.Log;
import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class TimestampQueueTest extends TestCase {
    private TimestampQueue<Reading> queue;
    private ArrayList<Reading> readings;

    int n = 100000;

    public void setUp()  {
        queue = new TimestampQueue<Reading>();
        readings = new ArrayList<Reading>();

        double values[] = {0, 0, 0};
        long startTime = 1234567;
        long distance = 1000;

        for (int i = 0; i < n; i++) {
            Reading reading = new Reading(values, startTime + distance * i, Reading.Type.ACCELERATION);
            reading.degrees = i;
            queue.push(reading);
            readings.add(reading);
        }
    }

    public void tearDown(  ) {
        queue = null;
        readings = null;
    }


    public void testUniformSearchWorks() {
        Reading target = readings.get(20000);

        long startTime = System.currentTimeMillis();
        Reading query = queue.getAtTimestamp(target.timestamp);
        long endTime = System.currentTimeMillis();

        assertEquals(target.timestamp, query.timestamp);
    }

    public void testSearchDoesntWork() {
        //Queue should return -1 for queries that don't exist
        Reading target = readings.get(20000);
        Reading query = queue.getAtTimestamp(target.timestamp + 1);
        assertEquals(null, query);
    }

    /**
     * timeBound is the method that returns a bounded version of the target queue instead of querying for a specific element
     */
    public void testTimeBoundWorks() {
        TimestampQueue<Reading> shortQueue = new TimestampQueue<Reading>();

        double values[] = {0, 0, 0};
        long startTime = 1000;
        long distance = 1000;

        for (int i = 0; i < 10; i++) {
            Reading reading = new Reading(values, startTime + distance * i, Reading.Type.ACCELERATION);
            reading.degrees = i;
            shortQueue.push(reading);
        }

        Reading reading = shortQueue.getContents().get(7);
        int index = shortQueue.efficientTimeBound(reading.timestamp);
        assertEquals(7, index);

        reading = shortQueue.getContents().get(7);
        index = shortQueue.efficientTimeBound(reading.timestamp + 100);
        assertEquals(7, index);

        index = shortQueue.efficientTimeBound(1000000);
        assertEquals(9, index);
    }
}
