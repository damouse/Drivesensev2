package edu.wisc.drivesense.views;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.businessLogic.Bengal;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.utilities.SensorSimulator;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A wrapper class for the map that takes up most of the main screen of the application. 
 * 
 * Relies on SensorMonitor for GPS updates to consolidate the objects that have to 
 * listen for GPS updates.
 * 
 * Display's the users location, traces along the map when recording, displays the saved
 * trips when in display mode.
 * 
 * Public Methods:
 * - startRecording: follows the user, draws a path, locks map to user
 * - stopRecording: stops above, unlocks map
 * - show trips: draws trips on map, pins and colors
 * - hideTrips: removes trips on map
 * - selectTrip: must be showing trips. Hides all other trips except the selected one
 * - deselectTrips: shows all trips again
 * 
 * WARN: fragment does not check if a trip has been updated. Must manually inform fragment of update and 
 * pass new array of trips. Potentially expensive.
 * 
 * @author Damouse
 */
public class PinMapFragment extends Fragment implements LocationListener, ConnectionCallbacks {
	private static final String TAG = "PinMapFragment";

    private Bengal delegate;

	private GoogleMap map;

    private BitmapLoader bitmapLoader;

    private List<TripMapInformation> tripsCache;

    private boolean showRequested = false;

	private TripMapInformation recordingTrip;
	
	private boolean displaying;

	
	private LocationClient client;

	
	
    /* Boilerplate */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View myFragmentView = inflater.inflate(R.layout.map_fragment, container, false);
	 
		initMap();

		return myFragmentView;
	}
	
	/**
	 * Init the map and location client
	 */
	private void initMap() {
		//grab a ref to the map for later manipulation
		MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        map = fm.getMap();

        bitmapLoader = new BitmapLoader(getActivity().getApplicationContext());

        //default to showing the user's location
        map.setMyLocationEnabled(true);
        
        Context context = getActivity().getApplicationContext();
        
		// check Google Play service APK is available and up to date.
		// see http://developer.android.com/google/play-services/setup.html
		final int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (result != ConnectionResult.SUCCESS) {
			Toast.makeText(context, "Google Play service is not available (status=" + result + ")", Toast.LENGTH_LONG).show();
			return;
		}

		//listeners. Unimportant, so class does not implement them
		OnConnectionFailedListener failed =  new OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult arg0) { }
		};
		
		client = new LocationClient(context, this, failed);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		client.connect();
	}

	@Override
	public void onPause() {
		super.onPause();
		client.disconnect();
	}

    public void setDelegate(Bengal bengal) {
        delegate = bengal;
    }

	  
    /* Public Interface */
	/**
	 * Clears cache and reloads.
	 */
	public void setTrips(List<Trip> newTrips) {
        map.clear();
        tripsCache = new ArrayList<TripMapInformation>();

        if (newTrips == null)
            return;

		for(Trip trip : newTrips) {
            new CalculateMapInfo(bitmapLoader) {
                protected void onPostExecute(TripMapInformation info) {
                    if (info != null) {
                        tripsCache.add(info);
                        Log.d(TAG, "Finished parsing trips");
                    }
                }
            }.execute(trip);
        }
	}

    /**
     * Does not check if the trips exist in the cache-- must call setTrips first.
     *
     * TODO: what if the asynctask hasn't finished
     */
    public void showTrips(List<Trip> showTrips) {
        displaying = true;
        map.clear();

        if (showTrips == null)
            return;

        Log.d(TAG, "Showing " + showTrips.size() + " trips");
        map.setMyLocationEnabled(false);

        if (showTrips.size() == 1)
            addTripToMap(findTripInCache(showTrips.get(0)), true, true);
        else {
            for (Trip trip : showTrips)
                addTripToMap(findTripInCache(showTrips.get(0)), false, false);
        }

        //TODO: zoom to the added trips
    }

    public void showRecordingTrip(Trip trip) {
        map.setMyLocationEnabled(true);

        new CalculateMapInfo(bitmapLoader) {
            protected void onPostExecute(TripMapInformation info) {
                if (info != null) {
                    recordingTrip = info;

                    //reload map
                }
            }
        }.execute(trip);
    }

    public void updateRecordingTrip(List<MappableEvent> events) {
        //add patterns to recording trip
        if (recordingTrip == null) {
            Log.e(TAG, "WARN: Update recording trip called while there is no recordingTrip!");
            return;
        }

//        recordingTrip.addCoordinate(coordinate);
//
//        //do not update the map with the recording trip if the map is busy displaying trips
//        if (displaying) return;
//        map.addPolyline(recordingTrip.line);
//
//        if (recordingTrip.marker1 == null) {
//            recordingTrip.setMarker1(coordinate);
//            map.addMarker(recordingTrip.marker1);
//        }
    }


    /* Private Helpers */
    /**
     * Add the passed info to the map, drawing it on the screen.
     * @param trip trip to be added to the map
     */
    private void addTripToMap(TripMapInformation trip, boolean showPatterns, boolean zoom) {
        if (trip.line == null || trip.marker1 == null)
            return;

        Log.d(TAG, "Adding trip: " + trip.toString());

        map.addPolyline(trip.line);
        map.addMarker(trip.marker1);

        if (trip.marker2 != null)
            map.addMarker(trip.marker2);

//        if (showPatterns) {
//            for (MarkerOptions marker: trip.patterns)
//                map.addMarker(marker);
//        }

        //alternative
        if (showPatterns) {
            for (GroundOverlayOptions marker: trip.patterns)
                map.addGroundOverlay(marker);
        }


        if (zoom) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(trip.marker1.getPosition());
            builder.include(trip.marker2.getPosition());
            LatLngBounds bounds = builder.build();

            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
            }
            catch (IllegalStateException ex) {
                Log.e(TAG, "Map view error!");
                ex.printStackTrace();
            }
        }
    }


    /* Private Implementation */
    private TripMapInformation findTripInCache(Trip trip) {
        for (TripMapInformation info: tripsCache) {
            if(info.trip.getId() == trip.getId())
                return info;
        }

        return null;
    }

	/* Location callback */
	 /**
	  * If currently recording, add a new point to the polyline. 
	  * 
	  * Snaps to the user on the first update. Moves the camera to user if recording,
	  * and records points if recording and displaying.
	  * @param location
	  */
	public void onLocationChanged(Location location) {
		//make a new latlng, assign it to recording trip
		double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);

        //if we're not displaying all of the trips, move the camera to the user's position
        //note: only happens when currently recording
        if (!displaying) {
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
	}


/* Misc Android Callbacks */
	@Override
	public void onConnected(Bundle arg0) { 
		LocationRequest locationrequest = new LocationRequest();
		locationrequest.setInterval(3);

        client.requestLocationUpdates(locationrequest, this);
	}

	@Override
	public void onDisconnected() { }
}
