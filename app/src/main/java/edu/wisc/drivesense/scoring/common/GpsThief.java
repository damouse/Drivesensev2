package edu.wisc.drivesense.scoring.common;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.utilities.Utils;

/**
 * Created by Damouse on 12/15/2014.
 * <p/>
 * Comes in the night and steals all of your gps coordinates!
 * <p/>
 * Or at least the ones that arent needed.
 */
public class GpsThief {
    //in feet: how far deviations from a straight line should be considered as another line
    public static final double epsilon = 10;

    /**
     * Given the current chunk of data and a window X into the past, make the gps coordinates more sparse
     * using a RamerDouglasPeucker function with an epsilon measured in miles.
     *
     * More specifically, removes gps points that deviate from a straight line at least epsilon.
     *
     * Assumes all relevant arrays are sorted
     */
    public static List<MappableEvent> getSparseCoordinates(TimestampQueue<Reading> gps, TimestampQueue<DrivingPattern> patterns) {
        ArrayList<MappableEvent> mappedPatterns = attachPatternsToCoordinates(gps, patterns);
        mappedPatterns = mergeGpsPatterns(mappedPatterns, gps);

        int countBefore = mappedPatterns.size();
        List<MappableEvent> result = ramerDouglasPeuckerFunction(mappedPatterns);
        Log.d("Theif", "Theif started with " + countBefore + " coordinates, ended with " + result.size());

        return result;
    }

    /**
     * Find closest gps coordinate to each pattern, create a mappable event, and remove that gps coordinate
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
    public static List<MappableEvent> ramerDouglasPeuckerFunction(List<MappableEvent> events) {

        //Can't remove points if there are aren't at least three
        if (events.size() < 3)
            return events;

        double dmax = 0f;
        int index = 1;

        for (int i = index; i < events.size() - 1; ++i) {
            MappableEvent event = events.get(i);

            //break early if a pattern is found-- we want to split on it no matter what
            if (event.type != MappableEvent.Type.gps) {
                dmax = Double.MAX_VALUE;
                index = i;
                break;
            }
            else {
                double d = pointLineDistance(event, events.get(0), events.get(events.size() - 1));

                if (d > dmax) {
                    index = i;
                    dmax = d;
                }
            }
        }

        if (dmax > epsilon) {
            List<MappableEvent> res1 = new ArrayList<MappableEvent>(ramerDouglasPeuckerFunction(events.subList(0, index + 1)));
            List<MappableEvent> res2 = ramerDouglasPeuckerFunction(events.subList(index, events.size()));

            //RDP is called recursively with points [A...B] and [B...C]. B is included twice and must be removed.
            res1.addAll(res2.subList(1, res2.size()));

            return res1;
        }
        else {
            ArrayList<MappableEvent> result = new ArrayList<MappableEvent>();
            result.add(events.get(0));
            result.add(events.get(events.size() - 1));
            return result;
        }
    }

    /**
     * WARN- this method calcuates euclidean distance-- will break (badly) near the poles and as distances increase
     * TODO: fix this.
     */
    public static double pointLineDistance(MappableEvent point, MappableEvent start, MappableEvent end) {
        if (start.latitude == end.latitude && start.longitude == end.longitude) {
            return distance(point, start);
        }

        double n = Math.abs((end.latitude - start.latitude) * (start.longitude - point.longitude) - (start.latitude - point.latitude) * (end.longitude - start.longitude));
        double d = Math.sqrt((end.latitude - start.latitude) * (end.latitude - start.latitude) + (end.longitude - start.longitude) * (end.longitude - start.longitude));

        return (n / d) * Utils.DECIMAL_DEGREE_TO_FEET;
    }


    //Returns distance in feet
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        Location locationA = new Location("");
        Location locationB = new Location("");

        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);

        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);

        return locationA.distanceTo(locationB) * Utils.METERS_TO_FEET;
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