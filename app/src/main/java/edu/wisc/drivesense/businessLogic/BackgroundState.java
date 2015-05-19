package edu.wisc.drivesense.businessLogic;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Service messages
 */

/**
 * Created by Damouse on 12/15/2014.
 *
 * Encapsulates all state for the backgroundService as well as the rest of the app.
 * Does not enact any changes in state itself (other than literally changing its variables,)
 *
 * calls back out to BackgroundRecordingService when it determines state changed.
 */
public class BackgroundState extends Observable {
    public enum State {
        AUTOMATIC_RECORDING,
        AUTOMATIC_STOP_WAIT,
        AUTOMATIC_STOP_LISTEN,
        MANUAL_RECORDING,
        MANUAL_LISTEN,
        UNINITIALIZED
    }

    private static final String TAG = "BackgroundState";
    private static State state = State.UNINITIALIZED;
    private String stateString = "";

    //State Inputs (they're variables for the sake of reasonable method names)
    private boolean powered = false;
    private boolean automaticRecording = false;
    private boolean automaticUnpoweredRecording = false;
    private boolean gpsAvailable = false;
    private boolean serviceOnline = false;


    public static State getState() { return state; }
    public String getStateString() { return stateString; }


    /* State Input Setters */
    public void setPowered(boolean state) {
        powered = state;
        establishState(false, false, false);
    }

    public void setGpsAvailable(boolean state) {
        gpsAvailable = state;
        establishState(false, false, false);
        Log.d(TAG, "GPS State changed to : " + state);
    }

    public void setAutomaticRecording(boolean state) {
        automaticRecording = state;
        establishState(false, false, false);
    }

    public void setAutomaticUnpoweredRecording(boolean state) {
        automaticUnpoweredRecording = state;
        establishState(false, false, false);
    }

    /** Called when the user touches a button while not in automatic mode */
    public void manualRecordingTrigger() {
        establishState(true, false, false);
    }

    /** Called when listeners think a trip started or stopped */
    public void adviseTripStart() {
        establishState(false, true, false);
    }

    public void adviseTripEnd() {
        establishState(false, false, true);
    }

    public void setServiceOn(boolean on) {
        serviceOnline = on;
    }


    /* State input setters */
    public boolean getPowered() { return powered; }
    public boolean getGpsAvailable() { return gpsAvailable; }
    public boolean getAutomaticRecording() { return automaticRecording; }
    public boolean getAutomaticUnpoweredRecording() { return automaticUnpoweredRecording; }


    /* State Decisions */
    /**
     * The money method of this class. Every change in state occurs from only this method.
     * No arguments are passed. To change the state, change the flags (acting as variables) declared
     * above and then recall this method. It will settle the state appropriately.
     * <p/>
     * Full documentation, including FSM, is available externally. Please talk to Mickey.
     */
    private void establishState(boolean manualTrigger, boolean adviseTripStart, boolean adviseTripStop) {
        State oldState = state;

        if (!serviceOnline)
            state = State.UNINITIALIZED;
        else if (state == State.AUTOMATIC_RECORDING)
            transitionAutomaticRecording(adviseTripStop);
        else if (state == State.AUTOMATIC_STOP_LISTEN)
            transitionAutomaticStopListen(adviseTripStart);
        else if (state == State.AUTOMATIC_STOP_WAIT)
            transitionAutomaticStopWait();
        else if (state == State.MANUAL_LISTEN)
            transitionManualListen(manualTrigger);
        else if (state == State.MANUAL_RECORDING)
            transitionManualRecording(manualTrigger, adviseTripStop);
        else if (state == State.UNINITIALIZED)
            transitionUninitialized();

        //done establishing state, notify observers only if the state changed
        if (oldState != state) {
            Log.i(TAG, "Changed state from " + oldState + " to " + state);
            setStateMessage();
            setChanged();
            notifyObservers(state);
        }
    }


    /* State Dependant Transitions
    *   Called when input variables change. Each method is a possible state and implenets
    *   transitions out of that state
    */
    private void transitionAutomaticRecording(boolean adviseTripStop) {
        if (!automaticRecording)
            state = State.MANUAL_LISTEN;

        else if (!(powered || automaticUnpoweredRecording) || !gpsAvailable)
            state = State.AUTOMATIC_STOP_WAIT;

        else {
            //actively recording, advised a trip is not running-- stop recording
            if (adviseTripStop)
                state = State.AUTOMATIC_STOP_LISTEN;
        }
    }

    private void transitionAutomaticStopListen(boolean adviseTripStart) {
        if (!automaticRecording)
            state = State.MANUAL_LISTEN;

        else if (!(powered || automaticUnpoweredRecording) || !gpsAvailable)
            state = State.AUTOMATIC_STOP_WAIT;

        else {
            if (adviseTripStart)
                state = State.AUTOMATIC_RECORDING;
        }
    }

    private void transitionAutomaticStopWait() {
        if (!automaticRecording)
            state = State.MANUAL_LISTEN;

        else if ((powered || automaticUnpoweredRecording) && gpsAvailable)
            state = State.AUTOMATIC_STOP_LISTEN;
    }

    private void transitionManualRecording(boolean manualTrigger, boolean adviseTripStop) {
        if (automaticRecording) {
            if ((powered || automaticUnpoweredRecording) && gpsAvailable)
                state = State.AUTOMATIC_STOP_LISTEN;
            else
                state = State.AUTOMATIC_STOP_WAIT;
        }
        else if (!gpsAvailable || manualTrigger || adviseTripStop)
            state = State.MANUAL_LISTEN;
    }

    private void transitionManualListen(boolean manualTrigger) {
        if (automaticRecording) {
            if ((powered || automaticUnpoweredRecording) && gpsAvailable)
                state = State.AUTOMATIC_STOP_LISTEN;
            else
                state = State.AUTOMATIC_STOP_WAIT;
        }
        else if (manualTrigger) {
            if (gpsAvailable)
                state = State.MANUAL_RECORDING;
        }
    }

    private void transitionUninitialized() {
        if (automaticRecording)
            state = State.AUTOMATIC_STOP_WAIT;
        else
            state = State.MANUAL_LISTEN;
    }


    /**
     * Set the state message based on current state
     */
    private void setStateMessage() {
        if (state == State.AUTOMATIC_RECORDING)
            stateString = "Recording Trip";
        else if (state == State.AUTOMATIC_STOP_LISTEN)
            stateString = "Listening for a trip to start";
        else if (state == State.AUTOMATIC_STOP_WAIT) {
            if (!(powered || automaticUnpoweredRecording))
                stateString = "Waiting for device power...";
            else if (!gpsAvailable)
                stateString = "Waiting for gps to be turned on...";
            else
                stateString = "Waiting for sensors...";
        }
        else if (state == State.MANUAL_LISTEN)
            stateString = "Ready to record a trip";
        else if (state == State.MANUAL_RECORDING)
            stateString = "Recording trip";
        else if (state == State.UNINITIALIZED)
            stateString = "Initializing...";
    }
}