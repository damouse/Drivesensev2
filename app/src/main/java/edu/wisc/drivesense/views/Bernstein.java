package edu.wisc.drivesense.views;

/**
 * Created by Damouse on 12/19/2014.
 *
 * A fameous conductor.
 *
 * Responsible for all UI state and animations.
 */
public class Bernstein {

    //These states represent the possible states the MainActivity
    //could be in. Class implements the animations and layout for each state
    public static enum UiState {
        STARTUP, //inital state. Showing just map and buttons
        MENU, //left slidein is onscreen
        SETTINGS, //right slidein is onscreen
        RECORDING, //recording slider is onscreen
        TRIPS_LIST, //the list of user's trips is up
        TRIPS_DETAIL //detail view for one trip is up
    }

    public Bernstein() {

    }
}
