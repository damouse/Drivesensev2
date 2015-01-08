package edu.wisc.drivesense.scoring.neural.utils;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;


/**
 * Created by Damouse on 12/13/2014.
 * <p/>
 * Utils having to deal with timestamps
 */
public class Timestamp {
    /**
     * return the earliest and latest timestamps that occur for any queue in the set
     */
    public static long[] timestampRangeForSet(ArrayList<TimestampQueue> set) {
        long times[] = {Long.MAX_VALUE, 0};

        for (TimestampQueue queue : set) {
            if (queue.size() == 0)
                continue;

            if (queue.startTime() < times[0])
                times[0] = queue.startTime();

            if (queue.endTime() > times[1])
                times[1] = queue.endTime();
        }

        return times;
    }

    /**
     * Get the earliest and latest timestamp for which there is data in every list
     *
     * @param data
     * @return
     */
    public static long[] completeTimestampRangeInDataSet(ArrayList<TimestampQueue> set) {
        long times[] = {0, Long.MAX_VALUE};

        for (TimestampQueue queue : set) {
            if (queue.startTime() > times[0])
                times[0] = queue.startTime();

            if (queue.endTime() < times[1])
                times[1] = queue.endTime();
        }

        return times;
    }

    /**
     * Dequeue all of the elements in each of the queues in the target that occur before
     * the given timestamp.
     *
     * @param target
     * @return
     */
    public static TimestampQueue dequeueBeforeTimestamp(List<TimestampQueue> target, long time) {
        TimestampQueue result = new TimestampQueue();

        for (TimestampQueue queue : target)
            result.addQueue(queue.dequeueBeforeTimestamp(time));

        result.sort();
        return result;
    }
}
