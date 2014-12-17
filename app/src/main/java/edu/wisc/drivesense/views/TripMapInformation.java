package edu.wisc.drivesense.views;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
	
	TripMapInformation() {
		coordinates = new ArrayList<LatLng>();
        line = new PolylineOptions();
	}
	
	void addCoordinate(LatLng coord) {
		coordinates.add(coord);
		line.add(coord);
	}

    void setMarker1(LatLng coordinate) {
        marker1 = new MarkerOptions() .title("Start") .position(coordinate)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_map));
    }
}

/* Background task for creating polylines-- consider caching for performance*/
class CalculateMapInfo extends AsyncTask<Void, Integer, TripMapInformation> {
    private final static String TAG = "TripMapInfo";
	private Trip trip;
	
	public CalculateMapInfo(Trip t) {
		trip = t;
	}
	
    @Override
    protected TripMapInformation doInBackground(Void... arg0) {
    	PolylineOptions line = new PolylineOptions().geodesic(true);
    	ArrayList<MappableEvent> readings = null; //trip.getGPSReadings();
    	
    	if(readings.size() < 1)
    		return null;
    	
    	for(MappableEvent reading : readings) {
    		LatLng coord = new LatLng(reading.latitude, reading.longitude);
    		line.add(coord);
    		//Log.d(TAG, "adding coord");

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

    	
    	//wrap the return data in a wrapper class, return to UI thread
    	TripMapInformation info = new TripMapInformation();

    	info.trip = trip;
    	info.line = line;
    	info.marker1 = marker1;
    	info.marker2 = marker2;
    	
    	return info;
    }	
}