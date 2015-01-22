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
	public Trip trip;

    public PolylineOptions line;

    public MarkerOptions marker1 = null;
    public MarkerOptions marker2 = null;

    public List<LatLng> coordinates;
    public List<GroundOverlayOptions> patterns;

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

        sb.append("Trip name: " + trip.getId());
        sb.append(" start: " + marker1.toString());
        sb.append(" end: " + marker2.toString());
        sb.append(" coordinates: " + coordinates.size());
        sb.append(" patterns: " + patterns.size());

        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TripMapInformation))
            return false;

        TripMapInformation other = (TripMapInformation) obj;
        return other.trip.getId() == this.trip.getId();
    }
}