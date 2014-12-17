package edu.wisc.drivesense.models;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.List;

import edu.wisc.drivesense.model.User;

/**
 * Created by Damouse on 12/16/2014.
 */
public class SugarTest extends TestCase {

    @Test
    public void testCanCreateModels() {
        User user = new User();
        user.save();

        List<User> load = User.listAll(User.class);

        assertEquals(1, load.size());

        user.delete();
    }
}
