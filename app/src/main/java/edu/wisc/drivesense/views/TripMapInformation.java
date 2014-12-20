package edu.wisc.drivesense.views;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Trip;

/**
 * Quick wrapper class that keeps track of trips and their stored polylines.
 * 
 * Improves performance: keeps us from having to redraw trips every time the display is triggered.
 * @author Damouse
 */
public class TripMapInformation {
    private static final String TAG = "TripMapInformation";
	Trip trip;
	
	PolylineOptions line;
	
	MarkerOptions marker1 = null;
	MarkerOptions marker2 = null;
	
	List<LatLng> coordinates;
    List<GroundOverlayOptions> patterns;

//    List<MarkerOptions> accelerations;
//    List<MarkerOptions> brakes;
//    List<MarkerOptions> turns;
//    List<MarkerOptions> laneChanges;

//        accelerations = new ArrayList<MarkerOptions>();
//        brakes = new ArrayList<MarkerOptions>();
//        turns = new ArrayList<MarkerOptions>();
//        laneChanges = new ArrayList<MarkerOptions>();


	TripMapInformation() {
		coordinates = new ArrayList<LatLng>();
        patterns = new ArrayList<GroundOverlayOptions>();

        line = new PolylineOptions().geodesic(true);
	}
	
	void addCoordinate(LatLng coord) {
		coordinates.add(coord);
		line.add(coord);
	}

    void setMarker1(LatLng coordinate) {
        marker1 = new MarkerOptions()
                        .title("Start")
                        .position(coordinate)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_map));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Trip name: " + trip.name);
        sb.append(" start: " + marker1.toString());
        sb.append(" end: " + marker2.toString());
        sb.append(" coordinates: " + coordinates.size());
        sb.append(" patterns: " + patterns.size());

        return sb.toString();
    }
}

/* Background task for creating polylines-- consider caching for performance*/
class CalculateMapInfo extends AsyncTask<Trip, Integer, TripMapInformation> {
    private final static String TAG = "TripMapInfo";

    private BitmapLoader loader;

    public CalculateMapInfo(BitmapLoader bitmap) {
        loader = bitmap;
    }

    @Override
    protected TripMapInformation doInBackground(Trip... params) {
        Trip trip = params[0];

    	List<MappableEvent> readings = trip.getEvents();
        TripMapInformation info = new TripMapInformation();

        info.trip = trip;

    	if(readings.size() < 1)
    		return null;
    	
    	for(MappableEvent reading : readings) {
            LatLng coord = new LatLng(reading.latitude, reading.longitude);
            info.addCoordinate(coord);

            if (reading.type != MappableEvent.Type.GPS) {
                info.patterns.add(createOverlay(reading, coord));
            }
    	}
    	
    	//add pins
    	MappableEvent startReading = readings.get(0);
    	MappableEvent endReading = readings.get(readings.size() - 1);
    	
    	LatLng start = new LatLng(startReading.latitude, startReading.longitude);
    	LatLng end = new LatLng(endReading.latitude, endReading.longitude);
    	
    	MarkerOptions marker1 = new MarkerOptions() .title("Start") .snippet(trip.name) .position(start)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_map));
    	MarkerOptions marker2 = new MarkerOptions() .title("End") .snippet(trip.name) .position(end)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.stop));

        info.marker1 = marker1;
        info.marker2 = marker2;
    	
    	return info;
    }


    private MarkerOptions createMarker(MappableEvent event, LatLng coordinate) {
        MarkerOptions marker = new MarkerOptions();

        marker.position(coordinate);
        marker.snippet("Score: " + event.score);
        marker.icon(BitmapDescriptorFactory.fromBitmap(loader.getBitmap(event)));

        if (event.type == MappableEvent.Type.ACCELERATION)
            marker.title("Acceleration");

        else if (event.type == MappableEvent.Type.BRAKE)
            marker.title("Brake");

        else if (event.type == MappableEvent.Type.TURN)
            marker.title("Turn");


        else if (event.type == MappableEvent.Type.LANE_CHANGE)
            marker.title("Lane Change");

        else
            return null;

        return marker;
    }

    private GroundOverlayOptions createOverlay(MappableEvent event, LatLng coordinate) {
        GroundOverlayOptions marker = new GroundOverlayOptions().zIndex(1)
                .position(coordinate, 50, 50)
                .image(BitmapDescriptorFactory.fromBitmap(loader.getBitmap(event)));


        return marker;
    }
}