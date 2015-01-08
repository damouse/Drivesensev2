package edu.wisc.drivesense.scoring.common;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.utilities.Utils;

/**
 * Created by Damouse on 12/15/2014.
 * <p/>
 * Comes in the night and steals all of your GPS coordinates!
 * <p/>
 * Or at least the ones that arent needed.
 */
public class GpsThief {
    //in feet: how far deviations from a straight line should be considered as another line
    public static final double epsilon = 10 * Utils.FEET_T0_MILES;

    /**
     * Given the current chunk of data and a window X into the past, make the gps coordinates more sparse
     * using a RamerDouglasPeucker function with an epsilon measured in miles.
     *
     * More specifically, removes GPS points that deviate from a straight line at least epsilon.
     *
     * Assumes all relevant arrays are sorted
     */
    public static ArrayList<MappableEvent> getSparseCoordinates(TimestampQueue<Reading> gps, TimestampQueue<DrivingPattern> patterns) {
        ArrayList<MappableEvent> mappedPatterns = attachPatternsToCoordinates(gps, patterns);
        mappedPatterns = mergeGpsPatterns(mappedPatterns, gps);
        return mappedPatterns;
//        return ramerDouglasPeuckerFunction(mappedPatterns, 0, mappedPatterns.size() - 1);
    }

    /**
     * Find closest GPS coordinate to each pattern, create a mappable event, and remove that gps coordinate
     *
     * Assumes there are enough coordinates to actually perform the mapping accurately. May not be true.
     *
     * Also assumes the patterns don't overlap so much that removing coordinates would be a Bad Thing.
     *
     * TODO: check timestamp difference, only accept if its within a threshold
     */
    public static ArrayList<MappableEvent> attachPatternsToCoordinates(TimestampQueue<Reading> gps, TimestampQueue<DrivingPattern> patterns) {
        ArrayList<MappableEvent> result = new ArrayList<MappableEvent>();
        ArrayList<MappableEvent> mappedGps = new ArrayList<MappableEvent>();

        for (DrivingPattern pattern: patterns) {
            Reading start = gps.getClosestTimestamp(pattern.start);
            Reading end = gps.getClosestTimestamp(pattern.end);
            result.add(new MappableEvent(start, end, pattern));
        }

        return result;
    }

    public static ArrayList<MappableEvent> mergeGpsPatterns(ArrayList<MappableEvent> patterns, TimestampQueue<Reading> gps) {
        ArrayList<MappableEvent> result = new ArrayList<MappableEvent>();
        result.addAll(patterns);

        for (Reading reading: gps)
            result.add(new MappableEvent(reading));

        EventComparator compare = new EventComparator();
        Collections.sort(result, compare);

        return result;
    }


    /* Ramer Doublas Peucker Filter */
    private static ArrayList<MappableEvent> ramerDouglasPeuckerFunction(ArrayList<MappableEvent> events, int startIndex, int lastIndex) {
        double dmax = 0f;
        int index = startIndex;

        for (int i = index + 1; i < lastIndex; ++i) {
            MappableEvent event = events.get(i);

            //break early if a pattern is found-- we want to split on it no matter what
            if (event.type != MappableEvent.Type.GPS) {
                dmax = Double.MAX_VALUE;
                index = i;
                break;
            }
            else {
                double d = PointLineDistance(events.get(i), events.get(startIndex), events.get(lastIndex));
                Log.d("GPSThief", "RDP Distance: " + d * Utils.MILES_TO_FEET + " epsilon: " + epsilon * Utils.MILES_TO_FEET);

                if (d > dmax) {
                    index = i;
                    dmax = d;
                }
            }
        }

        if (dmax > epsilon) {
            ArrayList<MappableEvent> res1 = ramerDouglasPeuckerFunction(events, startIndex, index);
            ArrayList<MappableEvent> res2 = ramerDouglasPeuckerFunction(events, index, lastIndex);

            ArrayList<MappableEvent>  finalRes = new ArrayList<MappableEvent>();
            finalRes.addAll(res1);
            finalRes.addAll(res2);

            return finalRes;
        }
        else {
            ArrayList<MappableEvent> result = new ArrayList<MappableEvent>();
            result.add(events.get(0));
            result.add(events.get(events.size() - 1));
            return result;
        }
    }

    public static double PointLineDistance(MappableEvent point, MappableEvent start, MappableEvent end) {
        if (start.latitude == end.latitude && start.longitude == end.longitude) {
            return distance(point, start);
        }

        double n = Math.abs((end.latitude - start.latitude) * (start.longitude - point.longitude) - (start.latitude - point.latitude) * (end.longitude - start.longitude));
        double d = Math.sqrt((end.latitude - start.latitude) * (end.latitude - start.latitude) + (end.longitude - start.longitude) * (end.longitude - start.longitude));

        return n / d;
    }


    //Returns distance in feet
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        return dist * 60 * 1.1515;
    }

    public static double distance(MappableEvent first, MappableEvent second) {
        return distance(first.latitude, first.longitude, second.latitude, second.longitude);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
}

class EventComparator implements Comparator {
    public int compare(Object arg0, Object arg1) {
        MappableEvent one = (MappableEvent) arg0;
        MappableEvent two = (MappableEvent) arg1;

        if (one.timestamp > two.timestamp)
            return 1;

        if (one.timestamp > two.timestamp)
            return -1;

        return 0;
    }
}