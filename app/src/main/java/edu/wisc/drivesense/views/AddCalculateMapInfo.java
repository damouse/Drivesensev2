package edu.wisc.drivesense.views;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import edu.wisc.drivesense.R;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Trip;

import java.util.List;

import static edu.wisc.drivesense.views.CalculateMapInfo.createOverlay;

/**
 * Takes an existing TripMapInfo and adds new patterns to it. These patterns MUST BE SET on the
 * trip.mappable_events before being passed in!
 */
public class AddCalculateMapInfo extends AsyncTask<TripMapInformation, Integer, TripMapInformation> {
    private final static String TAG = "AddCalculateMapInfo";
    private BitmapLoader loader;

    public AddCalculateMapInfo(BitmapLoader bitmap) {
        loader = bitmap;
    }

    @Override
    protected TripMapInformation doInBackground(TripMapInformation... params) {
        TripMapInformation tripInfo = params[0];
        Trip trip = tripInfo.trip;

        //mappableEvents contains only the new events. Old events have been encoded already
        List<MappableEvent> readings = trip.mappable_events;
        trip.mappable_events = null;

        if (readings == null || readings.size() == 0) {
            Log.w(TAG, "Could not find new patterns to add!");
            return tripInfo;
        }

        for(MappableEvent reading : readings) {
            LatLng coord = new LatLng(reading.latitude, reading.longitude);
            tripInfo.addCoordinate(coord);

            if (reading.type != MappableEvent.Type.gps) {
                tripInfo.patterns.add(createOverlay(reading, coord, loader));
            }
        }

        if (tripInfo.marker1 == null) {
            MappableEvent startReading = readings.get(0);
            LatLng start = new LatLng(startReading.latitude, startReading.longitude);

            tripInfo.marker1 = new MarkerOptions().title("Start")
                    .snippet(trip.name())
                    .position(start)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_map));
        }

        return tripInfo;
    }
}