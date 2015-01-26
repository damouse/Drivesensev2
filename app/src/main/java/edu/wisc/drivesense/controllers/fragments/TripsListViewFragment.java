package edu.wisc.drivesense.controllers.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import edu.wisc.drivesense.R;
import edu.wisc.drivesense.model.Trip;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.utilities.Utils;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.wisc.drivesense.views.BitmapLoader;

public class TripsListViewFragment extends ListFragment  {
	private static final String TAG = "TripsListViewFragment";

    private List<Trip> trips;
    private ArrayList<Trip> tripsInScope;

    private TripSelectedListener delegate;

	Context context;
    ArrayAdapter<Trip> listAdapter;
	

    /* Boilerplate */
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        delegate = (TripSelectedListener) activity;
        context = activity;
    }

    @Override
    public void onResume() {
        super.onResume();

        //remove the white divider between cells
        getListView().setDivider(null);

        //use along with "setListShown" to toggle empty list and spinner behavior.
        //See: http://stackoverflow.com/questions/9384792/replace-loading-message-in-listfragment-when-list-is-empty
        setListShown(true);
        setEmptyText("No trips for this week");
    }

    public interface TripSelectedListener {
        public void onTripSelected(Trip trip);
    }


    /* Trip Data Management */
    /**
     * Setting a user for the list will autoload the trips for that user and register the
     * activity for callbacks
     * @param user
     */
    public void setUser(User user) {
        trips = Trip.find(Trip.class, "user = ?", "" + user.getId());
        tripsInScope = applyScope(trips);

        Log.d(TAG, "Loaded " + tripsInScope.size() + " trips for user " + user.getId());

        //reload list
        listAdapter = new CustomListAdapter(context, this.tripsInScope);
        this.setListAdapter(listAdapter);
    }

    /**
     * Take the current scope, apply it to all trips, and return all of the trips that fall into the scope.
     *
     * The scope is a date or a date range.
     * @return
     */
    private ArrayList<Trip> applyScope(List<Trip> trips) {
        return new ArrayList<Trip>(trips);
    }

    /**
	 * Callback to the activity when a trip is clicked with the clicked trip
	 */
	@Override
	public void onListItemClick(ListView l, View view, int position, long id) {
        delegate.onTripSelected(tripsInScope.get(position));
    }


    /* ListView Methods */
	/**
	 * Viewholder pattern for speeding up population of listview
	 * @author Damouse
	 *
	 */
	private static class ViewHolder {
        View cell;
        
        TextView day;
        TextView time;
        TextView distance;
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

                holder.distance = (TextView) convertView.findViewById(R.id.distance);
                holder.day = (TextView) convertView.findViewById(R.id.day);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.cell = (View) convertView.findViewById(R.id.cell);
                
                convertView.setTag(holder);
            }
            else {
            	holder = (ViewHolder) convertView.getTag();
            }

            Trip current = getItem(position);

            holder.day.setText(Utils.dayOfWeek(current.timestamp));
            holder.time.setText(Utils.startEndTime(current.timestamp, current.duration));
            holder.distance.setText(Utils.formatDistance(current.distance));
            holder.cell.setBackgroundColor(BitmapLoader.colorForScore(current.score));

            return convertView;
        }
    }
}
