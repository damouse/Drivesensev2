package edu.wisc.drivesense.businessLogic;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Damouse on 12/16/2014.
 */
public class BackgroundStateTest extends TestCase {
    private BackgroundState state;
    private ObserverStub stateObserver;

    public void setUp()  {
        state = new BackgroundState();
        stateObserver = new ObserverStub();

        state.addObserver(stateObserver);
    }

    public void tearDown(  ) {
        state.deleteObserver(stateObserver);
        state = null;
        stateObserver = null;
    }


    @Test
    public void testBeginsNone() {
        assertEquals(state.getState(), BackgroundState.State.UNINITIALIZED);
    }


    /**
     * These are the following possible inputs.
     *       power on/off
     *       gps on/off
     *       autoRec on/off
     *       autoUnpo on/off
     *       manualTrigger
     *       adviseStart/Stop
     *
     * There are 11. Each possible input is tested.
     */
    @Test
    public void testManualListen() {
        setManualListen();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);

        //inputs that should not change the state
        state.adviseTripStart();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);

        state.setPowered(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);

        state.setGpsAvailable(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);

        state.setAutomaticUnpoweredRecording(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);


        // inputs that should result in an transition
        state.manualRecordingTrigger();
        assertEquals(BackgroundState.State.MANUAL_RECORDING, stateObserver.lastReportedState);

        setManualListen();
        state.setAutomaticRecording(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        setManualListen();
        state.setAutomaticRecording(true);
        state.setPowered(true);
        state.setGpsAvailable(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_LISTEN);
    }

    public void testManualRecording() {
        setManualRecording();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_RECORDING);

        //inputs that should not change the state
        state.setPowered(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_RECORDING);

        state.setPowered(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_RECORDING);

        state.adviseTripStart();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_RECORDING);


        //inputs that should change the state
        setManualRecording();
        state.adviseTripEnd();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);

        setManualRecording();
        state.setGpsAvailable(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);

        setManualRecording();
        state.manualRecordingTrigger();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);

        setManualRecording();
        state.setAutomaticRecording(true);
        state.setPowered(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        setManualRecording();
        state.setAutomaticRecording(true);
        state.setPowered(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_LISTEN);
    }

    public void testAutomaticStopWait() {
        setAutomaticStopWait();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        //inputs that should not change the state
        state.adviseTripEnd();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        state.adviseTripStart();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        state.manualRecordingTrigger();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        state.setGpsAvailable(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        setAutomaticStopWait();
        state.setPowered(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);


        //inputs that should change the state
        setAutomaticStopWait();
        state.setPowered(true);
        state.setGpsAvailable(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_LISTEN);

        setAutomaticStopWait();
        state.setAutomaticUnpoweredRecording(true);
        state.setGpsAvailable(true);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_LISTEN);

        setAutomaticStopWait();
        state.setAutomaticRecording(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.MANUAL_LISTEN);
    }

    public void testAutomaticListening() {
        setAutomaticListening();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_LISTEN);

        //inputs that should not change that state
        manualTrigger(BackgroundState.State.AUTOMATIC_STOP_LISTEN);
        adviseTripEnd(BackgroundState.State.AUTOMATIC_STOP_LISTEN);
        setAutoUnpowered(true, BackgroundState.State.AUTOMATIC_STOP_LISTEN);
        setAutoUnpowered(false, BackgroundState.State.AUTOMATIC_STOP_LISTEN);

        state.setAutomaticUnpoweredRecording(true);
        state.setPowered(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_LISTEN);


        //inputs that should change the state
        setAutomaticListening();
        state.adviseTripStart();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_RECORDING);

        setAutomaticListening();
        state.setGpsAvailable(false);
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        setAutomaticListening();
        state.setPowered(false);
        assertEquals(BackgroundState.State.AUTOMATIC_STOP_WAIT, stateObserver.lastReportedState);
    }

    public void testAutomaticRecording() {
        setAutomaticRecording();
        assertEquals(stateObserver.lastReportedState, BackgroundState.State.AUTOMATIC_RECORDING);

        //inputs that should not change the state
        adviseTripStart(BackgroundState.State.AUTOMATIC_RECORDING);
        setPower(true, BackgroundState.State.AUTOMATIC_RECORDING);
        setGPS(true, BackgroundState.State.AUTOMATIC_RECORDING);
        setAuto(true, BackgroundState.State.AUTOMATIC_RECORDING);
        setAutoUnpowered(true, BackgroundState.State.AUTOMATIC_RECORDING);
        manualTrigger(BackgroundState.State.AUTOMATIC_RECORDING);

        setAutomaticRecording();
        state.setAutomaticUnpoweredRecording(true);
        setPower(false, BackgroundState.State.AUTOMATIC_RECORDING);


        //inputs that should change the state
        setAutomaticRecording();
        adviseTripEnd(BackgroundState.State.AUTOMATIC_STOP_LISTEN);

        setAutomaticRecording();
        setPower(false, BackgroundState.State.AUTOMATIC_STOP_WAIT);

        setAutomaticRecording();
        setGPS(false, BackgroundState.State.AUTOMATIC_STOP_WAIT);
    }


    /* Setting and testing Utility methods */
    void setPower(boolean on, BackgroundState.State expectedState) {
        state.setPowered(on);
        assertEquals(expectedState, stateObserver.lastReportedState);
    }

    void setGPS(boolean on, BackgroundState.State expectedState) {
        state.setGpsAvailable(on);
        assertEquals(expectedState, stateObserver.lastReportedState);
    }

    void setAuto(boolean on, BackgroundState.State expectedState) {
        state.setAutomaticRecording(on);
        assertEquals(expectedState, stateObserver.lastReportedState);
    }

    void setAutoUnpowered(boolean on, BackgroundState.State expectedState) {
        state.setAutomaticUnpoweredRecording(on);
        assertEquals(expectedState, stateObserver.lastReportedState);
    }

    void manualTrigger(BackgroundState.State expectedState) {
        state.manualRecordingTrigger();
        assertEquals(expectedState, stateObserver.lastReportedState);
    }

    void adviseTripStart(BackgroundState.State expectedState) {
        state.adviseTripStart();
        assertEquals(expectedState, stateObserver.lastReportedState);
    }

    void adviseTripEnd(BackgroundState.State expectedState) {
        state.adviseTripEnd();
        assertEquals(expectedState, stateObserver.lastReportedState);
    }



    /* Utility Methods */
    void setManualListen() {
        state.setAutomaticRecording(false);
        state.setAutomaticUnpoweredRecording(false);
        state.setPowered(false);
        state.setGpsAvailable(true);
    }

    void setManualRecording() {
        state.setAutomaticRecording(false);
        state.setAutomaticUnpoweredRecording(false);
        state.setPowered(false);
        state.setGpsAvailable(true);
        state.manualRecordingTrigger();
    }

    void setAutomaticRecording() {
        state.setAutomaticRecording(true);
        state.setGpsAvailable(true);
        state.setPowered(true);
        state.setAutomaticUnpoweredRecording(false);
        state.adviseTripStart();
    }

    void setAutomaticListening() {
        state.setAutomaticRecording(true);
        state.setAutomaticUnpoweredRecording(false);
        state.setGpsAvailable(true);
        state.setPowered(true);
    }

    void setAutomaticStopWait() {
        state.setAutomaticRecording(true);
        state.setAutomaticUnpoweredRecording(false);
        state.setGpsAvailable(false);
        state.setPowered(false);
    }
}

/* Helper Objects */
class ObserverStub implements Observer {
    BackgroundState.State lastReportedState;

    @Override
    public void update(Observable observable, Object o) {
        lastReportedState = (BackgroundState.State) o;
    }
}