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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
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
	
	private GoogleMap map;
	
	private List<TripMapInformation> displayTrips;
	private TripMapInformation recordingTrip;
	
	private boolean displaying;
    private boolean dropOddCoordinates;
	
	private LocationClient client;

    private SensorSimulator simulator;
	
	
/* Boilerplate */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View myFragmentView = inflater.inflate(R.layout.map_fragment, container, false);
	 
		initMap();
        displayTrips = new ArrayList<TripMapInformation>();

		return myFragmentView;
	}
	
	/**
	 * Init the map and location client
	 */
	private void initMap() {
		//grab a ref to the map for later manipulation
		MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        map = fm.getMap();
        
        //default to showing the user's location
        map.setMyLocationEnabled(true);

        dropOddCoordinates = false;
        
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

	  
/* Public Interface */
	/**
	 * Load a given array of trips into the map cache. 
	 * @param trips
	 */
	public void loadTrips(List<Trip> trips) {
        Log.d(TAG, "Starting trip parse");
		 //draw trips, cache
		 for(Trip trip : trips) {   			
 			new CalculateMapInfo(trip) { 
 		        protected void onPostExecute(TripMapInformation info) {
 		        	if (info != null) {
    		        	displayTrips.add(info);
 		        	}
 		        }
 		    }.execute();
 		}
	}

    public void addExisitingRecordingTrip(Trip trip) {
        new CalculateMapInfo(trip) {
            protected void onPostExecute(TripMapInformation info) {
                if (info != null) {
                    startRecording();
                    recordingTrip = info;
                    addTripToMap(info);
                }
            }
        }.execute();
    }

	 /**
	  * Follow the user on the map. Trace the user's route with a polyline. 
	  * Ask locationManager to update more frequently.
      * Note that recording begins regardless of the displaty status of the rest of the
      * fragment. The displayed trip is hidden or presented, while maintaining its state,
      * when trips are shown or hidden.
	  */
	 public void startRecording() {
		 map.setMyLocationEnabled(true);
		 recordingTrip = new TripMapInformation();

         //a faking object that fakes location updates
         if (simulator != null && BackgroundRecordingService.DEBUG) simulator.startSendingLocations(this, 120);
	 }
	 
	 /**
	  * Reverse of the above method. Stop following the user, remove the previous polyline.
	  */
	 public void stopRecording() {
		 recordingTrip = null;
		 if (!displaying) map.clear();

         //a faking module used to debug and test

         if (simulator != null && BackgroundRecordingService.DEBUG) simulator.stopSendingLocations();
	 }
	 
	 /**
	  * Show the given trips by drawing them on the map. Each trip gets a pair of points and a polyline between the two.
	  */
	 public void showTrips() {
		 map.setMyLocationEnabled(false);
         map.clear();
		 addAllTripsToMap();
		 displaying = true;
	 }
	 
	 /**
	  * Reverse of the above method. Remove all polylines drawn on the map.
	  * 
	  *  If a trip is currently selected, deselect it. 
	  */
	 public void hideTrips() {
		 map.clear();
		 map.setMyLocationEnabled(true);
		 displaying = false;

         if (recordingTrip != null) addTripToMap(recordingTrip);
	 }

    /**
     * Delete pressed on the list. Remove the trip from the data structure, clear the map
     * @param trip
     */
    public void deleteTrip(Trip trip) {
        TripMapInformation remove = null;

        for (int i = 0; i < displayTrips.size(); i++) {
            if (displayTrips.get(i).trip.equals(trip)) {
                displayTrips.remove(displayTrips.get(i));
                break;
            }
        }

        deselctTrip();
    }

	 
	 /**
	  * Select the given trip on the map. Should be a member of the displayed trips (i.e.
	  * in display mode)
	  * @param trip the trip that should be selected
	  */
	 public void selectTrip(Trip trip) {
		 map.clear();

		 for (TripMapInformation cachedTrip: displayTrips) {
//			if (cachedTrip.trip.id == trip.id) {
            if (true) {
				addTripToMap(cachedTrip);

                //zoom the map
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(cachedTrip.marker1.getPosition());
                builder.include(cachedTrip.marker2.getPosition());

                LatLngBounds bounds = builder.build();

                Log.d(TAG, "Bounds: " + bounds.toString());

                try {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                }
                catch (IllegalStateException ex) {
                    Log.e(TAG, "Map view error! " + ex.toString());
                    ex.printStackTrace();
                }
                //map.animateCamera(CameraUpdateFactory.zoomTo(15));

				return;
			}
		 }
	 }
	 
	 /**
	  * Deselect any trips selected on the map and show the fully list of displaytrips
	  */
	 public void deselctTrip() {
		 map.clear();
		 addAllTripsToMap();
	 }

	 
/* Private Helpers */
	 /**
	  * Add the passed info to the map, drawing it on the screen.
	  * @param trip trip to be added to the map
	  */
	 private void addTripToMap(TripMapInformation trip) {
	    if (trip.line == null || trip.marker1 == null)
            return;

        map.addPolyline(trip.line);
		map.addMarker(trip.marker1);

        if (trip.marker2 != null)
            map.addMarker(trip.marker2);
	 }
	 
	 private void addAllTripsToMap() {
		 for (TripMapInformation trip: displayTrips) {
			 addTripToMap(trip);
		 }
	 }

    /**
     * Update the currently recording trip with the new coordinate passed in. Append it to the
     * active trip object and add it to the map.
     * @param coordinate the coordinate to be passed in
     */
    private void updateRecordingTrip(LatLng coordinate) {
        if (recordingTrip == null) {
            Log.e(TAG, "WARN: Update recording trip called while there is no recordingTrip!");
            return;
        }

        if (coordinate == null) {
            Log.e(TAG, "WARN: Update recording trip called with a null coordinate!");
            return;
        }

        recordingTrip.addCoordinate(coordinate);


        //do not update the map with the recording trip if the map is busy displaying trips
        if (displaying) return;

        map.addPolyline(recordingTrip.line);

        if (recordingTrip.marker1 == null) {
            recordingTrip.setMarker1(coordinate);
            map.addMarker(recordingTrip.marker1);
        }

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

        //omit every other coordinate to cut down on crashes from having too many coordinates
        dropOddCoordinates = !dropOddCoordinates;
        if (dropOddCoordinates) return;
		
		//do not follow the user if we are not currently recording the trip
		if (recordingTrip != null)
            updateRecordingTrip(latLng);


	}


/* Misc Android Callbacks */
	@Override
	public void onConnected(Bundle arg0) { 
		LocationRequest locationrequest = new LocationRequest();
		locationrequest.setInterval(3);

        if (BackgroundRecordingService.DEBUG) {
            simulator = new SensorSimulator();
            //simulator.startSendingLocations(this, 120);
        }
        else
            client.requestLocationUpdates(locationrequest, this);
	}

	@Override
	public void onDisconnected() { }
}
