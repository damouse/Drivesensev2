package edu.wisc.drivesense.models;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Date;
import java.util.List;

import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;

/**
 * Created by Damouse on 12/16/2014.
 */
public class MappableEventTest extends TestCase {
    private MappableEvent event;
    private List<MappableEvent> events;
    private Trip trip;
    private int numEvents = 10;

    public void setUp()  {
        trip = new Trip();
        trip.save();

        event = new MappableEvent();
        event.trip = trip;
        event.save();

        for (int i = 0; i < numEvents - 1; i++) {
            MappableEvent event = new MappableEvent();
            event.trip = trip;
            event.save();
        }

        events = MappableEvent.listAll(MappableEvent.class);
    }

    public void tearDown(  ) {
        event = null;

        events = MappableEvent.listAll(MappableEvent.class);
        for (MappableEvent event: events)
            event.delete();

        events = null;
        trip.delete();
    }



    @Test
    public void testEventsExist() {
        events = MappableEvent.listAll(MappableEvent.class);
        assertEquals(numEvents, events.size());
    }

    @Test
    public void testTripPersistsData() {
        event.timestamp = new Date().getTime();
        event.type = MappableEvent.Type.ACCELERATION;
        event.score = 1;

        event.save();

        MappableEvent loaded = Trip.findById(MappableEvent.class, event.getId());

        assertEquals(event.timestamp, loaded.timestamp);
        assertEquals(event.type, loaded.type);
        assertEquals(event.score, loaded.score);
    }

    @Test
    public void testEventBelongsToTrip() {
        Trip loadedTrip = Trip.findById(Trip.class, trip.getId());
        assertEquals(events.size(), loadedTrip.getEvents().size());
    }
}
