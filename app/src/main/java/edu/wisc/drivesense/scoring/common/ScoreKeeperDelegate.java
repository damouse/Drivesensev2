package edu.wisc.drivesense.scoring.common;

import java.util.ArrayList;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;

/**
 * Interface that defines interactions between the Scorekeeper and whomever
 * wants to hear things.
 */
public interface ScoreKeeperDelegate {
    /**
     * Called when the scorekeeper declares a new pattern.
     */
    public void newPatterns(ArrayList<MappableEvent> events);
}
