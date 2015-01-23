package edu.wisc.drivesense.scoring.neural.modelObjects;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.wisc.drivesense.model.Reading;

import static edu.wisc.drivesense.scoring.neural.offline.OfflineWrapper.log;


/**
 * Created by Damouse on 12/14/2014.
 *
 * Objects are pushed to the end of the array and popped off the front. This means that
 * iterators over the queue will move forward in time with respect to the elements' timestamps
 */
public class TimestampQueue<T extends TimestampSortable> implements Iterable<T> {
    private ArrayList<T> contents;

    public static void main(String[] args) {
        /* BENCHMARKING
        Note: most of the methods below have big commented out setins. These represent the original
        implementation of the methods, before the efficiency methods were added.
        TODO: write tests for new methods

        Results- 1m elements, old method
        getAtTimestamp: 11
        getBeforeTimestamp: 19
        dequeueBeforeTimestamp: 101048
        getClosestTimestamp: 0
        getQueueInRange: 29
        trimInPlace: 198261

        Results- 1m elements, using efficient time searches
        getAtTimestamp: 12
        getBeforeTimestamp: 19
        dequeueBeforeTimestamp: 14
        getClosestTimestamp: 0
        getQueueInRange: 1
        trimInPlace: 2
         */

        //create queue and items
        TimestampQueue<Reading> queue = new TimestampQueue<Reading>();
        int n = 1000000;
        double values[] = {0, 0, 0};
        long startTime = 1234567;
        long distance = 1000; //between neighboring timestamps

        for (int i = 0; i < n; i++) {
            Reading reading = new Reading(values, startTime + distance * i, Reading.Type.ACCELERATION);
            reading.degrees = i;
            queue.push(reading);
        }

        Reading targetMiddle = queue.getContents().get(500000);
        Reading targetQuarter = queue.getContents().get(250000);
        Reading targetThreeQuarter = queue.getContents().get(750000);


        //BENCH-
        long timeOne = System.currentTimeMillis();
        queue.getAtTimestamp(targetMiddle.timestamp);
        long timeTwo = System.currentTimeMillis();
        System.out.println("getAtTimestamp: " + (timeTwo - timeOne));

        queue.getBeforeTimestamp(targetMiddle.timestamp);
        timeOne = System.currentTimeMillis();
        System.out.println("getBeforeTimestamp: " + (timeOne - timeTwo));

        queue.dequeueBeforeTimestamp(targetMiddle.timestamp);
        timeTwo = System.currentTimeMillis();
        System.out.println("dequeueBeforeTimestamp: " + (timeTwo - timeOne));

        queue.getClosestTimestamp(targetMiddle.timestamp);
        timeOne = System.currentTimeMillis();
        System.out.println("getClosestTimestamp: " + (timeOne - timeTwo));

        queue.getQueueInRange(targetQuarter.timestamp, targetThreeQuarter.timestamp);
        timeTwo = System.currentTimeMillis();
        System.out.println("getQueueInRange: " + (timeTwo - timeOne));

        queue.trimInPlace(targetQuarter.timestamp, targetThreeQuarter.timestamp);
        timeOne = System.currentTimeMillis();
        System.out.println("trimInPlace: " + (timeOne - timeTwo));
    }

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
        int index = efficientTimeSearch(timestamp);
        if (index == -1)
            return null;

        return contents.get(index);

//        for (T element: this) {
//            if (element.getTime() == timestamp)
//                return element;
//
//            if (element.getTime() > timestamp)
//                break;
//        }
//
//        return null;
    }

    /**
     * Return the element that lies closest to the passed timestamp, null if the
     * queue is empty.
     * @param timestamp
     * @return
     */
    public T getClosestTimestamp(long timestamp) {
        int index = efficientTimeBound(timestamp);
        return contents.get(index);
//        long bestDifference = Long.MAX_VALUE;
//        long currentDifference;
//        long lastDifference = Long.MAX_VALUE;
//        T closestElement = null;
//
//        for (T element: this) {
//            currentDifference = Math.abs(element.getTime() - timestamp);
//
//            if (currentDifference < bestDifference) {
//                bestDifference = currentDifference;
//                closestElement = element;
//            }
//
//            //break early to save performance
//            if (currentDifference > lastDifference)
//                break;
//
//            lastDifference = currentDifference;
//        }
//
//        return closestElement;
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

        int startIndex = efficientTimeBound(start);
        int endIndex = efficientTimeBound(end);

        if (startIndex == endIndex)
            return new TimestampQueue<T>();

        return new TimestampQueue<T>(new ArrayList<T>(contents.subList(startIndex, endIndex)));
//        for (T element: this) {
//            if(element.getTime() < start)
//                continue;
//
//            if (element.getTime() > end)
//                break;
//
//            result.push(element);
//        }
//
//        return result;
    }


    /* Non-Standard Mutators */
    /**
     * Add the target queue to this queue. Does not sort, not a safe operation!
     * @param target
     */
    public void addQueue(TimestampQueue<T> target) {
        contents.addAll(target.contents);
    }

    public void addList(List<T> target) { contents.addAll(target); }

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

        int startIndex = efficientTimeBound(start);
        int endIndex = efficientTimeBound(end);

        if (startIndex == endIndex){
            log("Trimmed a queue to nothing.");
            contents = new ArrayList<T>();
            return;
        }

        contents.subList(0, startIndex).clear();
        contents.subList(endIndex + 1, contents.size() - 1);

//        for (T element: this) {
//            if(element.getTime() < start)
//                remove.add(element);
//
//            if (element.getTime() > end)
//                remove.add(element);
//        }
//
//        contents.removeAll(remove);
    }


    /* Information */
    public T peek() {
        if (headIndex() >= contents.size())
            return null;

        return contents.get(headIndex());
    }

    public T peekLast() {
        if (tailIndex() >= contents.size())
            return null;

        return contents.get(tailIndex());
    }

    public long startTime() {
        if (contents.size() == 0)
            return -1;

        return peek().getTime();
    }

    public long endTime() {
        if (contents.size() == 0)
            return -1;

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
        if (contents.size() == 0)
            return -1;

        //time is outside of bounds
        if (time < peek().getTime() || time > peekLast().getTime())
            return -1;

        //use the other method to get the element with the closest timestamp to the target time--
        //return its index only if the timestamp EXACTLY matches the target
        int index = efficientTimeBound(time);
        if (contents.get(index).getTime() == time)
            return index;

        return -1;
    }

    /**
     * Similar to the above method, but returns the element with the timestamp closest to
     * the passed timestamp.
     *
     * @param time timestamp to use when querying elements
     * @return index of element closest to the passed timestamp, or -1 if it doesn't exist
     */
    public int efficientTimeBound(long time) {
        if (contents.size() == 0)
            return -1;

        if (time < peek().getTime())
            return 0;

        if (time > peekLast().getTime())
            return contents.size() -1;

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
        long bestDifference = Math.abs(probableElement.getTime() - time);
        int bestIndex = currentIndex;

        while (probableElement.getTime() != time) {
            currentIndex += direction;

            //check to make sure you don't run off the end of the queues
            //return the last valid index
            if (currentIndex < 0 || currentIndex >= contents.size())
                return currentIndex - direction;

            probableElement = contents.get(currentIndex);
            long newDifference = Math.abs(probableElement.getTime() - time);

            //if the new difference is greater than the best, we passed the element- return closest
            if (newDifference >= bestDifference) {
                return bestIndex;
            }
            else {
                bestDifference = newDifference;
                bestIndex = currentIndex;
            }
        }

        return bestIndex;
    }

    private TimestampQueue<T> processBeforeTimestamp(long timestamp, boolean shouldRemove) {
        //get the starting point for the cut
        int closestIndex = efficientTimeBound(timestamp);

        if (closestIndex == -1)
            return null;

        T closestElement = contents.get(closestIndex);

        //Three cases for the returned closest index-- could be exactly the timestamp (which is fine),
        //coule be earlier than the target timestamp (also fine), or could be an element that is later than the
        //bounded time (not acceptable). In the last case, move the index forward an element
        //NOTE: this is currently not implemented, as a huge amount of readings means a sortof close return is
        //"good enough"
//        if (closestElement.getTime() > timestamp) {
//            closestElement = contents.get(closestIndex - 1);
//        }

        TimestampQueue<T> result = new TimestampQueue<T>(new ArrayList<T>(contents.subList(0, closestIndex)));

        if (shouldRemove)
            contents.subList(0, closestIndex).clear();

//        for (T element: this) {
//            if (element.getTime() >= timestamp)
//                break;
//
//            result.push(element);
//            remove.add(element);
//        }
//
//        if (shouldRemove)
//            contents.removeAll(remove);

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

        if (one.getTime() < two.getTime())
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