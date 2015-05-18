package edu.wisc.drivesense.controllers.fragments;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.views.AddCalculateMapInfo;
import edu.wisc.drivesense.views.BitmapLoader;
import edu.wisc.drivesense.views.CalculateMapInfo;
import edu.wisc.drivesense.views.TripMapInformation;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A wrapper class for the map that takes up most of the main screen of the application. 
 * 
 * Relies on SensorMonitor for gps updates to consolidate the objects that have to
 * listen for gps updates.
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
public class PinMapFragment extends Fragment implements LocationListener {
	private static final String TAG = "PinMapFragment";

	private GoogleMap map;
    private BitmapLoader bitmapLoader;
    private TripMapInformation recordingTrip;
    private GoogleApiClient client;

    private List<TripMapInformation> tripsCache;
	private List<AsyncTask> processing;

	private boolean displaying = false;
    private boolean displayingAllTrips = false;

	
    /* Boilerplate */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View myFragmentView = inflater.inflate(R.layout.map_fragment, container, false);

        tripsCache = new ArrayList<TripMapInformation>();
        processing = new ArrayList<AsyncTask>();

        //grab a ref to the map for later manipulation
        MapFragment fm = (MapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        Log.v(TAG, "FM IS NULL: " + fm.toString());
        map = fm.getMap();

		initMap();

		return myFragmentView;
	}
	
	/**
	 * Init the map and location client
	 */
	private void initMap() {
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
		
		client = new GoogleApiClient.Builder(context).addApi(LocationServices.API).build();
	}
	
	@Override
	public void onResume() {
		super.onResume();

        if (BackgroundRecordingService.DEBUG && BackgroundRecordingService.getInstance() != null)
            BackgroundRecordingService.getInstance().monitor.simulator.startSendingLocations(this);
        else
		    client.connect();
	}

	@Override
	public void onPause() {
		super.onPause();

        if (BackgroundRecordingService.DEBUG && BackgroundRecordingService.getInstance() != null)
            BackgroundRecordingService.getInstance().monitor.simulator.stopSendingLocations(this);
        else
            client.disconnect();
	}

	  
    /* Public Interface */
    /**
     * Searches the cache for the passed trips, displaying them immediately if found.
     *
     * If the trip is not found in the cache, a new async processing task is spun off
     * and added to a local queue. When finished, trip is added to the map unless the
     * user has navigated off show all.
     *
     * Zooms to the range of trips.
     */
    public void showTrips(List<Trip> trips) {
        Log.d(TAG, "Showing " + trips.size() + " trips");
        map.setMyLocationEnabled(false);
        displayingAllTrips = true;
        map.clear();

        for (Trip trip: trips) {
            queueParseTrip(trip, false);
        }
    }

    /**
     * Show just one trip on the map, with patterns. All other trips are removed from the
     * map, but not discarded.
     */
    public void showTrip(Trip trip) {
        map.setMyLocationEnabled(false);
        displayingAllTrips = true;
        map.clear();

        queueParseTrip(trip, true);
    }

    public void showNothing() {
        map.setMyLocationEnabled(true);
        displayingAllTrips = false;
        map.clear();
    }

    public void showRecordingTrip(Trip trip) {
        map.clear();
        map.setMyLocationEnabled(true);
        displayingAllTrips = false;
        displaying = true;

        new CalculateMapInfo(bitmapLoader) {
            protected void onPostExecute(TripMapInformation info) {
                if (info != null) {
                    recordingTrip = info;
                    recordingTrip.marker2 = null;
                    addTripToMap(recordingTrip, true);
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

            recordingTrip.trip.mappable_events = events;

            new AddCalculateMapInfo(bitmapLoader) {
                protected void onPostExecute(TripMapInformation info) {
                    Log.d(TAG, "Finished trip process, adding to map.");
                    if (info == null) {
                        Log.e(TAG, "Received a null tripInfo from the processor!");
                    }

                    map.clear();
                    addTripToMap(recordingTrip, true);
            }
        }.execute(recordingTrip);
    }


    /* Private Helpers */
    /**
     * Create a parse task for the trip, enqueue it in the task queue, and add the trip
     * once the parse finishes.
     * @param trip
     */
    private void queueParseTrip(Trip trip, final boolean showPatterns) {
        TripMapInformation info = findTripInCache(trip);

        if (info != null)
            addTripToMap(info, showPatterns);
        else {

            //TODO: the start of a long list of "carefuls." Trip added to task before adding to queue...
            //what if it finishes before the add?
            AsyncTask processor = new CalculateMapInfo(bitmapLoader) {
                protected void onPostExecute(TripMapInformation info) {
                    Log.d(TAG, "Finished trip process, adding to map.");
                    if (info != null) {
                        tripsCache.add(info);
                    }
                    else {
                        Log.e(TAG, "Trip info is null! Don't know how to proceed!");
                    }

                    //TODO: careful- what does "this" refer to?
                    processing.remove(this);

                    //TODO: careful again- what happens if the map goes away before the task completes?
                    addTripToMap(info, showPatterns);

                }
            }.execute(trip);

            processing.add(processor);
            //processor.execute(trip);
        }

        //TODO: zoom to the added trips
    }

    /**
     * Add the passed info to the map, drawing it on the screen.
    * @param trip trip to be added to the map
    */
    private void addTripToMap(TripMapInformation trip, boolean showPatterns) {
        if (trip == null) {
            Log.e(TAG, "Trip is null when returning from the calculate async call!");
            return;
        }
        if (trip.line == null || trip.marker1 == null)
            return;

        Log.d(TAG, "Adding trip: " + trip.toString());

        map.addPolyline(trip.line);
        map.addMarker(trip.marker1);

        if (trip.marker2 != null)
            map.addMarker(trip.marker2);

        if (showPatterns) {
            for (GroundOverlayOptions marker: trip.patterns)
                map.addGroundOverlay(marker);
        }

        //zoom to the map
        zoomToBounds(trip);
    }

    private void zoomToBounds(TripMapInformation trip) {

        try {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(trip.bounds, 200));
        }
        catch (IllegalStateException ex) {
            Log.e(TAG, "Map view error!");
            ex.printStackTrace();
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
        if (!displayingAllTrips) {
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            map.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
	}
}
