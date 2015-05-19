package edu.wisc.drivesense.controllers.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.wisc.drivesense.R;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.utilities.Utils;
import edu.wisc.drivesense.views.BitmapLoader;

public class StatsFragment extends Fragment {
    private static final String TAG = "StatsFragment";

    private LinearLayout layoutRoot;
    private Trip trip;

    private TextView textviewScore;
    private TextView textviewLeft;
    private TextView textviewCenter;
    private TextView textviewRight;


    /* Boilerplate */
    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layoutRoot = (LinearLayout) inflater.inflate(R.layout.fragment_stats, container, false);
        layoutRoot.setBackgroundColor(getResources().getColor(R.color.background));

        textviewScore = (TextView) layoutRoot.findViewById(R.id.textviewScore);
        textviewLeft = (TextView) layoutRoot.findViewById(R.id.textviewLeft);
        textviewCenter = (TextView) layoutRoot.findViewById(R.id.textviewCenter);
        textviewRight = (TextView) layoutRoot.findViewById(R.id.textviewRight);

        return layoutRoot;
    }


    /* Communication with Activity */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Update this view the the given trip. If null, show default state. Else update views with the trip's
     * status.
     */
    public void setTrip(Trip trip) {
        if (trip == null) {
            layoutRoot.setBackgroundColor(getResources().getColor(R.color.background));

            textviewScore.setText("");
            textviewLeft.setText("");
            textviewCenter.setText("No Trips");
            textviewRight.setText("");
        }
        else {
            layoutRoot.setBackgroundColor(BitmapLoader.colorForScore(trip.score));

            textviewScore.setText("" + trip.score);
            textviewLeft.setText(Utils.startTime(trip.timestamp));
            textviewCenter.setText(Utils.formatDistance(trip.distance));
            textviewRight.setText(Utils.formatSignificantDuration(trip.duration));
        }
    }
}
