package edu.wisc.drivesense.scoring.neural.modelObjects;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.wisc.drivesense.model.Reading;

import static edu.wisc.drivesense.scoring.DrivingAnalyst.log;

/**
 * Created by Damouse on 12/14/2014.
 *
 * Objects are pushed to the end of the array and popped off the front. This means that
 * iterators over the queue will move forward in time with respect to the elements' timestamps
 */
public class TimestampQueue<T extends TimestampSortable> implements Iterable<T> {
    private ArrayList<T> contents;


    /* Boilerplate Constructors */
    public TimestampQueue() {
        contents = new ArrayList<T>();
    }

    public TimestampQueue(TimestampQueue<T> other) {
        contents = new ArrayList<T>(other.contents);
    }

    public TimestampQueue(ArrayList<T> contents) {
        this.contents = contents;
    }


    /* Queue Timestamp Operations */
    /**
     * Return all the elements in this queue that fall before a given timestamp
     *
     * @param timestamp The timestamp to check against
     * @return Queue witht the elements that have timestamps before the passed timestamp
     */
    public TimestampQueue<T> getBeforeTimestamp(long timestamp) {
        return processBeforeTimestamp(timestamp, false);
    }

    /**
     * Returns the same queue that the above method returns, but removes the elements from this
     * queue.
     *
     * @param timestamp the timestamp to filter against
     * @return Queue with elements that have timestamps before the passed timestmap
     */
    public TimestampQueue<T> dequeueBeforeTimestamp(long timestamp) {
        return processBeforeTimestamp(timestamp, true);
    }

    /**
     * Return the element that has a timestamp that matches the current timestamp,
     * or null if one does not exist.
     *
     * @param timestamp time to check against
     * @return Element or null
     */
    public T getAtTimestamp(long timestamp) {
        for (T element: this) {
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
    public T getClosestTimestamp(long timestamp) {
        long bestDifference = Long.MAX_VALUE;
        long currentDifference;
        long lastDifference = Long.MAX_VALUE;
        T closestElement = null;

        for (T element: this) {
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
    public TimestampQueue<T> getQueueInRange(long start, long end) {
        TimestampQueue<T> result = new TimestampQueue<T>();

        if (end < startTime() || start > endTime()) {
            log("Attempted to range a queue with invalid range.");
            return result;
        }

        for (T element: this) {
            if(element.getTime() < start)
                continue;

            if (element.getTime() > end)
                break;

            result.push(element);
        }

        return result;
    }


    /* Non-Standard Mutators */
    /**
     * Add the target queue to this queue. Does not sort, not a safe operation!
     * @param target
     */
    public void addQueue(TimestampQueue<T> target) {
        contents.addAll(target.contents);
    }

    public void addList(List<T> target) { contents.addAll(target);}

    public void sort() {
        TimestampComparator compare = new TimestampComparator();
        Collections.sort(contents, compare);
    }


    /* Boilerplate Queue Operations */
    public void push(T object) {
        contents.add(object);
    }

    public T pop() {
        return contents.remove(headIndex());
    }

    /**
     * Destructively remove elements that have timestamps outside of the passed range.
     * @param start start time
     * @param end end time
     */
    public void trimInPlace(long start, long end) {
        ArrayList<T> remove = new ArrayList<T>();

        if (end < startTime() || start > endTime()) {
            log("Trimmed a queue to nothing.");
            contents = new ArrayList<T>();
            return;
        }

        for (T element: this) {
            if(element.getTime() < start)
                remove.add(element);

            if (element.getTime() > end)
                remove.add(element);
        }

        contents.removeAll(remove);
    }


    /* Information */
    public T peek() {
        return contents.get(headIndex());
    }

    public T peekLast() {
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
    public T peekFromTail(int index) {
        return contents.get(tailIndex() - index);
    }


    /* Accessors */
    public int size() {
        return contents.size();
    }

    public Iterator<T> iterator() {
        return contents.iterator();
    }

    public ArrayList<T> getContents() {
        return contents;
    }

    public boolean remove(T element) {
        return contents.remove(element);
    }

    public T get(int index) {
        return contents.get(index);
    }


    /* Internal */
    private int headIndex() { return 0; }
    private int tailIndex() { return contents.size() - 1; }

    /**
     * A more efficient implementation of searching for elements in this queue.
     *
     * Returns the index of an element that matches the given timestamp, or -1
     * if the index does not exist
     *
     * Based on two assumptions: the contents of the queue are sorted, and the
     * relative time between two elements of the queue are similar.
     *
     * If these two assumptions hold, lookup operations are either in constant time
     * or very, very close to constant time.
     *
     * Worst case scenario, lookups are N complex.
     */
    private int efficientTimeSearch(long time) {
        //time is outside of bounds
        if (time < peek().getTime() || time > peekLast().getTime())
            return -1;

        long totalTime = 0;
        int elementsToQuery = 5;

        //case: less elements than the desired query number
        if (contents.size() < elementsToQuery)
            elementsToQuery = contents.size();

        for (int i = 0; i < elementsToQuery; i++)
            totalTime += contents.get(i).getTime();

        //time per index is the relative difference in time between elements
        long timePerIndex = totalTime / elementsToQuery;

        //time between target time and first element / time per index
        int currentIndex = (int) ((time - peek().getTime()) / timePerIndex);
        T probableElement = contents.get(currentIndex);

        //if current element time is later than target time, must move down the array, else up
        int direction = probableElement.getTime() > time ? -1 : 1;

        while (probableElement.getTime() != time) {
            //occurs if moved past the target time: element with time does not exist
            if (direction == -1 && probableElement.getTime() < time)
                return -1;

            if (direction == 1 && probableElement.getTime() > time)
                return -1;

            currentIndex += direction;
            probableElement = contents.get(currentIndex);
        }

        return currentIndex;
    }

    private TimestampQueue<T> processBeforeTimestamp(long timestamp, boolean shouldRemove) {
        TimestampQueue<T> result = new TimestampQueue<T>();
        ArrayList<T> remove = new ArrayList<T>();

        for (T element: this) {
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
/* Saved code from previous implementation of efficientSearch
        //move down the array if element time is greater than target time
        if (probableElement.getTime() >= time) {
            if (probableElement.getTime() == time)
                return currentIndex;

            //passed the target time, target element does not exist
            if (probableElement.getTime() < time)
                return -1;

            currentIndex--;
            probableElement = contents.get(currentIndex);
        }

        //move up the array if time is less than target time
        if (probableElement.getTime() <= time) {
            if (probableElement.getTime() == time)
                return currentIndex;

            //passed the target time, target element does not exist
            if (probableElement.getTime() < time)
                return -1;

            currentIndex++;
            probableElement = contents.get(currentIndex);
        }
 */