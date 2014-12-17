package edu.wisc.drivesense.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.activities.MainActivity;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.server.ConnectionManager;
import edu.wisc.drivesense.server.ConnectionManagerCallback;
import edu.wisc.drivesense.server.DrivesensePreferences;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class TripsListViewFragment extends ListFragment implements ConnectionManagerCallback {
	private static final String TAG = "TripsListViewFragment";
	private ArrayList<Trip> trips;
	private TripsListViewFragment thisObject; //what a silly hack.
	
	View openView;
	Trip openTrip = null; //saved in case the open cell is closed as the trip is uploading

    Trip uploadingTrip;
	
	Context context;

    ArrayAdapter<Trip> listAdapter;
	
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        
        thisObject = this;
        
        //tracks which list item was last selected
        openTrip = null;
        openView = null;
    }
	

/* Listeners */
    OnClickListener uploadListener = new Button.OnClickListener() {
    	public void onClick(View v) {
    		//check to make sure the user is logged in
    		Context context = getActivity().getApplicationContext();
    		DrivesensePreferences prefs = new DrivesensePreferences(context);
    		
    		if (!prefs.loggedIn()) {
    			Toast.makeText(context, "You must be logged in to upload trips", Toast.LENGTH_SHORT).show();
    			return;
    		}
    		
    		//ensure the trip has not already been uploaded
    		//if (!openTrip.uploaded)
    		v.setVisibility(View.INVISIBLE);
        	((ProgressBar)openView.findViewById(R.id.spinner)).setVisibility(View.VISIBLE);

            ((MainActivity) getActivity()).setBusy(true);
            uploadingTrip = openTrip;
        	new ConnectionManager(context, thisObject).convertUploadTrip(openTrip, prefs.loggedInUser());
        }
    };

    OnClickListener deleteListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(context, "Deleted trip", Toast.LENGTH_SHORT).show();
            trips.remove(openTrip);
            listAdapter.notifyDataSetChanged();

            //notify activity of change, propogates to map
            ((MainActivity) getActivity()).listFragmentDeletedTrip(openTrip);

//            BackgroundRecordingService.getInstance().deleteTrip(openTrip);

            openView = null;
            openTrip = null;
        }
    };

    OnClickListener facebookListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(context, "Social functionality disabled for testing", Toast.LENGTH_SHORT).show();
        }
    };

    OnClickListener twitterListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(context, "Social functionality disabled for field testing", Toast.LENGTH_SHORT).show();
        }
    };
	
    
/* Connection Manage Delegate Methods */
    @Override
    public void onLoginCompletion(boolean success, User user, String response) { }
    @Override
    public void onSessionCompletion(boolean success) {  }

    @Override
    public void onUploadCompletion(boolean success, String response) {
        ((MainActivity) getActivity()).setBusy(false);

        if (!success) {
            Toast.makeText(context, "There was a problem uploading the trip", Toast.LENGTH_SHORT).show();
            return;
        }

        if (openView == null) {
            Log.e(TAG, "The open view was switched out!");
            return;
        }

        if (((Button)openView.findViewById(R.id.uploadButton)).getVisibility() == View.VISIBLE) {
            Log.e(TAG, "The open view was switched out! Open view has a visible button!");
            return;
        }

        ((Button)openView.findViewById(R.id.uploadButton)).setVisibility(View.VISIBLE);
        ((ProgressBar)openView.findViewById(R.id.spinner)).setVisibility(View.INVISIBLE);

        setItemUpdated(openView);
    }


    private void setItemUpdated(View view) {
		TextView status = (TextView) view.findViewById(R.id.upload);
    	status.setText("Trip Uploaded");
    	status.setTextColor(getResources().getColor(R.color.green));
	}
	
	
	/**
	 * Expands the given cell on click. Saves the "open" trip represented by the given cell.
	 * 
	 * If no cell is open, open the current cell. 
	 * If a cell is opened and it is the newly pressed cell, close the cell
	 * If the touched cell is not the previously opened cell, close that one and open the new one
	 */
	@Override
	public void onListItemClick(ListView l, View view, int position, long id) {						
		//touched cell is animated regardless
		View toolbar = view.findViewById(R.id.toolbar);
        ExpandAnimation expandAni = new ExpandAnimation(toolbar, 300);
        toolbar.startAnimation(expandAni);
        
      //touch on previously opened cell- close open cenn
        if (openView == view) {
    		openView = null;
    		openTrip = ((MainActivity) getActivity()).listFragmentItemClicked(position, false);
        }	
        else {
        	//touch on new cell with another cell already opened
        	if (openView != null) {
        		View oldToolbar = openView.findViewById(R.id.toolbar);
                expandAni = new ExpandAnimation(oldToolbar, 300);
                oldToolbar.startAnimation(expandAni);
        	}
        	
        	//last cell is recorded regardless if the openView was either null or newview didnt match the last open view
        	openView = view;
        	openTrip = ((MainActivity) getActivity()).listFragmentItemClicked(position, true);
        	
        	Button upload = (Button) openView.findViewById(R.id.uploadButton);
        	upload.setOnClickListener(uploadListener);

            Button delete = (Button) openView.findViewById(R.id.delete);
            delete.setOnClickListener(deleteListener);

            Button facebook = (Button) openView.findViewById(R.id.facebook);
            facebook.setOnClickListener(facebookListener);

            Button twitter = (Button) openView.findViewById(R.id.twitter);
            twitter.setOnClickListener(twitterListener);
        }
    }

	
	/**
	 * Refresh the list
	 */
	public void loadTrips(List<Trip> trips) {
        this.trips = new ArrayList<Trip>(trips);
		listAdapter = new CustomListAdapter(context, this.trips);
	    this.setListAdapter(listAdapter);
	}

	
	
/* ListView Methods */
	/**
	 * Viewholder pattern for speeding up population of listview
	 * @author Damouse
	 *
	 */
	private static class ViewHolder {
        View toolbar;
        
        TextView name;
        TextView date;
        TextView distance;
        TextView duration;
        TextView uploadStatus;
        TextView score;
    }

    /**
     * A simple implementation of list adapter.
     */
    class CustomListAdapter extends ArrayAdapter<Trip> {
    	public CustomListAdapter(Context context, ArrayList<Trip> trips) {
    	       super(context, R.layout.list_item, trips);
    	}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	ViewHolder holder;
        	
            if (convertView == null) {
            	holder = new ViewHolder();
            	LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.list_item, parent, false);
                
                holder.toolbar = convertView.findViewById(R.id.toolbar);
                holder.name = (TextView) convertView.findViewById(R.id.title);
                holder.date = (TextView) convertView.findViewById(R.id.trip_date);
                holder.duration = (TextView) convertView.findViewById(R.id.trip_duration);
                holder.distance = (TextView) convertView.findViewById(R.id.trip_distance);
                holder.uploadStatus = (TextView) convertView.findViewById(R.id.upload);
                holder.score = (TextView) convertView.findViewById(R.id.trip_score);
                
                convertView.setTag(holder);
            }
            else {
            	holder = (ViewHolder) convertView.getTag();
            }

            Trip current = getItem(position);
            
            //format date
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMMM dd, hh:mma");
            
//            holder.name.setText(current.name);
//            holder.date.setText(formatter.format(current.date));
//            holder.distance.setText(current.formattedDistance());
//            holder.duration.setText(current.formattedDuration());
//
//            if (current.score == null)
//            	holder.score.setText("X");
//            else
//            	holder.score.setText(Integer.toString(current.score.score));
//
//            if (current.uploaded)
//            	setItemUpdated(holder.uploadStatus);
//            else
//            	holder.uploadStatus.setText("Trip has not been uploaded");

            // Resets the toolbar to be closed
            View toolbar = convertView.findViewById(R.id.toolbar);
            ((LinearLayout.LayoutParams) toolbar.getLayoutParams()).bottomMargin = -50;
            toolbar.setVisibility(View.GONE);

            return convertView;
        }
    }
    
    class ExpandAnimation extends Animation {
        private View mAnimatedView;
        private LayoutParams mViewLayoutParams;
        private int mMarginStart, mMarginEnd;
        private boolean mIsVisibleAfter = false;
        private boolean mWasEndedAlready = false;

        /**
         * Initialize the animation
         * @param view The layout we want to animate
         * @param duration The duration of the animation, in ms
         */
        public ExpandAnimation(View view, int duration) {

            setDuration(duration);
            mAnimatedView = view;
            mViewLayoutParams = (LayoutParams) view.getLayoutParams();

            // decide to show or hide the view- False when open, true when closed
            mIsVisibleAfter = (view.getVisibility() == View.VISIBLE);

            mMarginStart = mViewLayoutParams.bottomMargin;
            mMarginEnd = (mMarginStart == 0 ? (0- view.getHeight()) : 0);

            view.setVisibility(View.VISIBLE);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);

            if (interpolatedTime < 1.0f) {
                // Calculating the new bottom margin, and setting it
                mViewLayoutParams.bottomMargin = mMarginStart + (int) ((mMarginEnd - mMarginStart) * interpolatedTime);

                // Invalidating the layout, making us seeing the changes we made
                mAnimatedView.requestLayout();

            // Making sure we didn't run the ending before
            } else if (!mWasEndedAlready) {
                mViewLayoutParams.bottomMargin = mMarginEnd;
                mAnimatedView.requestLayout();

                if (mIsVisibleAfter) {
                    mAnimatedView.setVisibility(View.GONE);
                }
                mWasEndedAlready = true;
            }
        }
    }
}
