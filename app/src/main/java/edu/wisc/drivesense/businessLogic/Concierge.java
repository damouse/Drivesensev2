package edu.wisc.drivesense.businessLogic;

import android.util.Log;

import java.util.List;
import java.util.Observable;

import edu.wisc.drivesense.model.User;

/**
 * Created by Damouse on 12/19/2014.
 * <p/>
 * The sole interface for interacting with and changing the state of the
 * currently logged in user.
 * <p/>
 * Loads the logged in user, informs
 */
public class Concierge extends Observable {
    private static final String TAG = "Concierge";
    private User currentUser;


    public Concierge() {
        currentUser = loadActiveUser();
        Log.d(TAG, "Loaded user: " + currentUser.email);
    }

    /**
     * Get or create and return the demo user
     *
     * @return
     */
    private static User getDemoUser() {
        List<User> demo = User.find(User.class, "user_id = ?", "-7");

        if (demo.size() == 1)
            return demo.get(0);

        //TODO: fix this. Merge all of their trips and delete all but one
        if (demo.size() > 1) {
            Log.e(TAG, "HCF-- multiple demo users!");
            throw new NullPointerException();
        }

        User newDemo = new User();
        newDemo.userId = -7;
        newDemo.email = "Demo User";
        newDemo.save();
        return newDemo;
    }

    /* Update Methods */
    public User getCurrentUser() {
        return currentUser;
    }

    public void logOut() {
        if (currentUser.demoUser())
            return;

        currentUser.loggedIn = false;
        currentUser.save();

        currentUser = getDemoUser();
        setChanged();
        notifyObservers(currentUser);
    }


    /* User loading and management */

    public void logIn(User user) {
        if (currentUser.loggedIn) {
            currentUser.loggedIn = false;
            currentUser.save();
        }

        currentUser = user;
        currentUser.loggedIn = true;
        currentUser.save();

        setChanged();
        notifyObservers(currentUser);
    }

    /**
     * If no users exist, create a demo user.
     * Else, check preferences for a saved user and load him
     */
    public User loadActiveUser() {
        List<User> users = User.find(User.class, "logged_in = ?", "true");

        if (users.size() == 1)
            return users.get(0);

        //no logged in users
        if (users.size() == 0) {
            return getDemoUser();
        } else {
            Log.e(TAG, "HCF-- MULTIPLE LOGGED IN USERS");
            for (User user : users)
                user.logOut();
            return getDemoUser();
        }
    }
}
