package edu.wisc.drivesense.models;

import junit.framework.TestCase;

import org.junit.Test;

import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;

/**
 * Created by Damouse on 12/16/2014.
 */
public class UserTest extends TestCase {
    private User user;

    public void setUp()  {
        user = new User();
        user.save();
    }

    public void tearDown(  ) {
        user.delete();
    }

    @Test
    void testGetEventsFailsGracefully() {
        User newUser = new User();
        assertEquals(0, newUser.getTrips().size());
    }
}
