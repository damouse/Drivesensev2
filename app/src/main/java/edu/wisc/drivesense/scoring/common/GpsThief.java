package edu.wisc.drivesense.scoring.common;

import java.util.ArrayList;

import edu.wisc.drivesense.model.DrivingPattern;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;

/**
 * Created by Damouse on 12/15/2014.
 * <p/>
 * Comes in the night and steals all of your GPS coordinates!
 * <p/>
 * Or at least the ones that arent needed.
 */
public class GpsThief {

    /**
     * Given the current chunk of data and a window X into the past, make the gps coordinates more sparse
     *
     * @param period
     * @return
     */
    public static ArrayList<MappableEvent> getSparseCoordinates(DataSetInput period, ArrayList<DrivingPattern> patterns) {
        ArrayList<MappableEvent> result = new ArrayList<MappableEvent>();

//        for (TimestampSortable element : period.gps) {
//            Reading reading = (Reading) element;
//            result.add(new MappableEvent(reading));
//        }

        return result;
    }
}
