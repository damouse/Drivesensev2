package edu.wisc.drivesense.views;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.model.MappableEvent;
import edu.wisc.drivesense.model.Trip;

/* Background task for creating polylines-- consider caching for performance*/
public class CalculateMapInfo extends AsyncTask<Trip, Integer, TripMapInformation> {
    private final static String TAG = "TripMapInfo";
    private Trip trip;
    private BitmapLoader loader;

    public CalculateMapInfo(BitmapLoader bitmap) {
        loader = bitmap;
    }

    @Override
    protected TripMapInformation doInBackground(Trip... params) {
        trip = params[0];

        List<MappableEvent> readings = trip.getEvents();
        TripMapInformation info = new TripMapInformation();

        info.trip = trip;

        if(readings.size() < 1)
            return null;

        for(MappableEvent reading : readings) {
            LatLng coord = new LatLng(reading.latitude, reading.longitude);
            info.addCoordinate(coord);

            if (reading.type != MappableEvent.Type.gps) {
                info.patterns.add(createOverlay(reading, coord));
            }
        }

        //add pins
        MappableEvent startReading = readings.get(0);
        MappableEvent endReading = readings.get(readings.size() - 1);

        LatLng start = new LatLng(startReading.latitude, startReading.longitude);
        LatLng end = new LatLng(endReading.latitude, endReading.longitude);

        MarkerOptions marker1 = new MarkerOptions().title("Start")
                .snippet(trip.name())
                .position(start)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_map));

        MarkerOptions marker2 = new MarkerOptions().title("End")
                .snippet(trip.name())
                .position(end)
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

        if (event.type == MappableEvent.Type.acceleration)
            marker.title("Acceleration");

        else if (event.type == MappableEvent.Type.brake)
            marker.title("Brake");

        else if (event.type == MappableEvent.Type.turn)
            marker.title("Turn");

        else if (event.type == MappableEvent.Type.lanechange)
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

    public boolean equals(Object obj) {
        if (!(obj instanceof CalculateMapInfo))
            return false;

        CalculateMapInfo other = (CalculateMapInfo) obj;
        return other.trip.getId() == this.trip.getId();
    }
}