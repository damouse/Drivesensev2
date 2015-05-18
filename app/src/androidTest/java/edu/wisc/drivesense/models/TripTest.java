package edu.wisc.drivesense.models;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Date;
import java.util.List;

import edu.wisc.drivesense.businessLogic.BackgroundState;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;

/**
 * Created by Damouse on 12/16/2014.
 */
public class TripTest extends TestCase {
    private List<Trip> trips;
    private Trip oneTrip;
    private User user;
    private final int numTrips = 5;


    public void setUp()  {
        user = new User();
        user.save();

        oneTrip = new Trip();
        oneTrip.user = user;
        oneTrip.save();

        for (int i = 0; i < numTrips - 1; i++)
            new Trip().save();

        trips = Trip.listAll(Trip.class);
    }

    public void tearDown(  ) {
        oneTrip = null;

        trips = Trip.listAll(Trip.class);
        for (Trip trip: trips)
            trip.delete();

        trips = null;
        user.delete();
    }


    @Test
    public void testTripsExist() {
        trips = Trip.listAll(Trip.class);
        assertEquals(numTrips, trips.size());
    }

    @Test
    public void testTripPersistsData() {
//        oneTrip.trip_id = 1;
        oneTrip.timestamp = new Date().getTime();
        oneTrip.duration = 10;
        oneTrip.distance = 10;
        oneTrip.score = 1;
        oneTrip.scoreTurns = 50;

        oneTrip.save();

        Trip loaded = Trip.findById(Trip.class, oneTrip.getId());

//        assertEquals(oneTrip.trip_id, loaded.trip_id);
        assertEquals(oneTrip.timestamp, loaded.timestamp);
        assertEquals(oneTrip.duration, loaded.duration);
        assertEquals(oneTrip.distance, loaded.distance);
        assertEquals(oneTrip.score, loaded.score);
        assertEquals(oneTrip.scoreTurns, loaded.scoreTurns);
    }

    @Test
    public void testTripBelongsToUser() {
        User loaded = User.findById(User.class, user.getId());
        assertEquals(oneTrip.user.getId(), loaded.getId());
    }

    @Test void testGetEventsFailsGracefully() {
        Trip trip = new Trip();
        assertEquals(0, trip.getEvents().size());
    }
}
