package edu.wisc.drivesense.scoring.neural.modelObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import static edu.wisc.drivesense.scoring.DrivingAnalyst.log;

/**
 * Created by Damouse on 12/14/2014.
 *
 * Objects are pushed to the end of the array and popped off the front. This means that
 * iterators over the queue will move forward in time with respect to the elements' timestamps
 */
public class TimestampQueue implements Iterable<TimestampSortable> {
    public ArrayList<TimestampSortable> contents;


    /* Boilerplate Constructors */
    public TimestampQueue() {
        contents = new ArrayList<TimestampSortable>();
    }

    public TimestampQueue(TimestampQueue other) {
        contents = new ArrayList<TimestampSortable>(other.contents);
    }

    /** List copy constructor */
    public static ArrayList<TimestampQueue> queueListCopy(ArrayList<TimestampQueue> target) {
        ArrayList<TimestampQueue> result = new ArrayList<TimestampQueue>();

        for (TimestampQueue queue: target)
            result.add(new TimestampQueue(queue));

        return result;
    }


    /* Queue Timestamp Operations */
    /**
     * Return all the elements in this queue that fall before a given timestamp
     *
     * @param timestamp The timestamp to check against
     * @return Queue witht the elements that have timestamps before the passed timestamp
     */
    public TimestampQueue getBeforeTimestamp(long timestamp) {
        return processBeforeTimestamp(timestamp, false);
    }

    /**
     * Returns the same queue that the above method returns, but removes the elements from this
     * queue.
     *
     * @param timestamp the timestamp to filter against
     * @return Queue with elements that have timestamps before the passed timestmap
     */
    public TimestampQueue dequeueBeforeTimestamp(long timestamp) {
        return processBeforeTimestamp(timestamp, true);
    }

    /**
     * Return the element that has a timestamp that matches the current timestamp,
     * or null if one does not exist.
     *
     * @param timestamp time to check against
     * @return Element or null
     */
    public TimestampSortable getAtTimestamp(long timestamp) {
        for (TimestampSortable element: this) {
            if (element.getTime() == timestamp)
                return element;

            if (element.getTime() > timestamp)
                break;
        }

        return null;
    }

    /**
     * Return the element that lies closest to the passed timestamp, null if the
     * queue is empty.
     * @param timestamp
     * @return
     */
    public TimestampSortable getClosestTimestamp(long timestamp) {
        long bestDifference = Long.MAX_VALUE;
        long currentDifference;
        long lastDifference = Long.MAX_VALUE;
        TimestampSortable closestElement = null;

        for (TimestampSortable element: this) {
            currentDifference = Math.abs(element.getTime() - timestamp);

            if (currentDifference < bestDifference) {
                bestDifference = currentDifference;
                closestElement = element;
            }

            //break early to save performance
            if (currentDifference > lastDifference)
                break;

            lastDifference = currentDifference;
        }

        return closestElement;
    }

    /**
     * Retuen a new queue without affecting this one with the elements in this queue that
     * have timestamp between the two passed parameters.
     *
     * @param start start time
     * @param end end time
     * @return new Queue with elements between the timestamps
     */
    public TimestampQueue getQueueInRange(long start, long end) {
        TimestampQueue result = new TimestampQueue();

        if (end < startTime() || start > endTime()) {
            log("Attempted to range a queue with invalid range.");
            return result;
        }

        for (TimestampSortable element: this) {
            if(element.getTime() < start)
                continue;

            if (element.getTime() > end)
                break;

            result.push(element);
        }

        return result;
    }

    /**
     * Destructively remove elements that have timestamps outside of the passed range.
     * @param start start time
     * @param end end time
     */
    public void trimInPlace(long start, long end) {
        ArrayList<TimestampSortable> remove = new ArrayList<TimestampSortable>();

        if (end < startTime() || start > endTime()) {
            log("Trimmed a queue to nothing.");
            contents = new ArrayList<TimestampSortable>();
            return;
        }

        for (TimestampSortable element: this) {
            if(element.getTime() < start)
                remove.add(element);

            if (element.getTime() > end)
                remove.add(element);
        }

        contents.removeAll(remove);
    }

    /**
     * Add the target queue to this queue. Does not sort, not a safe operation!
     * @param target
     */
    public void addQueue(TimestampQueue target) {
        contents.addAll(target.contents);
    }

    public void sort() {
        TimestampComparator compare = new TimestampComparator();
        Collections.sort(contents, compare);
    }


    /* Boilerplate Queue Operations */
    public void push(TimestampSortable object) {
        contents.add(object);
    }

    public TimestampSortable pop() {
        return contents.remove(headIndex());
    }

    public TimestampSortable peek() {
        return contents.get(headIndex());
    }

    public TimestampSortable peekLast() {
        return contents.get(tailIndex());
    }

    public long startTime() {
        return peek().getTime();
    }

    public long endTime() {
        return peekLast().getTime();
    }

    /**
     * Using "get" will access elements from most recent to latest timestamp. This method access
     * from latest to most recent timestamp. Considered another way, it gets based on the order
     * in which elements are added to the queue;
     */
    public TimestampSortable peekFromTail(int index) {
        return contents.get(tailIndex() - index);
    }

    /* Accessors */
    public int size() {
        return contents.size();
    }

    public Iterator<TimestampSortable> iterator() {
        return contents.iterator();
    }

    /* Internal */
    private int headIndex() { return 0; }
    private int tailIndex() { return contents.size() - 1; }

    private TimestampQueue processBeforeTimestamp(long timestamp, boolean shouldRemove) {
        TimestampQueue result = new TimestampQueue();
        ArrayList<TimestampSortable> remove = new ArrayList<TimestampSortable>();

        for (TimestampSortable element: this) {
            if (element.getTime() >= timestamp)
                break;

            result.push(element);
            remove.add(element);
        }

        if (shouldRemove)
            contents.removeAll(remove);

        return  result;
    }
}


/* Sorting */
class TimestampComparator implements Comparator {
    public int compare(Object arg0, Object arg1) {
        TimestampSortable one = (TimestampSortable) arg0;
        TimestampSortable two = (TimestampSortable) arg1;

        if (one.getTime() > two.getTime())
            return 1;

        if (one.getTime() > two.getTime())
            return -1;

        return 0;
    }
}