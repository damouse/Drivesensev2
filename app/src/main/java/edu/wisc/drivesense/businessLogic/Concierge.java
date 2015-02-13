package edu.wisc.drivesense.businessLogic;

import android.util.Log;

import java.util.List;

import edu.wisc.drivesense.model.User;

/**
 * Created by Damouse on 12/19/2014.
 * <p/>
 * The sole interface for interacting with and changing the state of the
 * currently logged in user.
 * <p/>
 * Loads the logged in user, informs
 */
public class Concierge {
    private static final String TAG = "Concierge";
    private static User currentUser;


    public static void initializeConcierge() {
        currentUser = loadActiveUser();
        Log.d(TAG, "Initializing: loaded user: " + currentUser.email);
    }

    /**
     * Get or create and return the demo user
     *
     * @return
     */
    private static User getDemoUser() {
        List<User> demo = User.find(User.class, "backend_id = ?", "-7");

        if (demo.size() == 1)
            return demo.get(0);

        //TODO: fix this. Merge all of their trips and delete all but one
        if (demo.size() > 1) {
            Log.e(TAG, "HCF-- multiple demo users!");
            throw new NullPointerException();
        }

        User newDemo = new User();
        newDemo.backendId = -7;
        newDemo.email = "Demo User";
        newDemo.loggedIn = true;
        newDemo.save();

        Log.i(TAG, "Created new Demo User");

        return newDemo;
    }

    /* Update Methods */
    public static User getCurrentUser() {
        if (currentUser == null)
            initializeConcierge();

        return currentUser;
    }

    public static User reloadUser() {
        initializeConcierge();
        return currentUser;
    }

    public static void logOut() {
        if (currentUser == null)
            return;

        Log.i(TAG, "Logging " + currentUser.email + " out");
        if (currentUser.demoUser())
            return;

        currentUser.loggedIn = false;
        currentUser.save();

        currentUser = getDemoUser();
        currentUser.loggedIn = true;
        currentUser.save();
    }


    /* User loading and management */

    public static void logIn(User user) {
        if (currentUser.loggedIn) {
            currentUser.loggedIn = false;
            currentUser.save();
        }

        currentUser = user;
        currentUser.loggedIn = true;
        currentUser.save();

        Log.i(TAG, "Logged " + user.email + " in");
    }

    /**
     * If no users exist, create a demo user.
     * Else, check preferences for a saved user and load him
     */
    private static User loadActiveUser() {
        List<User> users = User.find(User.class, "logged_in = ?", "1");

        if (users.size() == 1)
            return users.get(0);

        //no logged in users
        if (users.size() == 0) {
            return getDemoUser();
        } else {
            Log.e(TAG, "WARN-- MULTIPLE LOGGED IN USERS");
            for (User user : users)
                user.logOut();
            return getDemoUser();
        }
    }
}
