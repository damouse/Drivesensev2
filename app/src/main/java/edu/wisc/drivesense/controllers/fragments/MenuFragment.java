package edu.wisc.drivesense.controllers.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.Concierge;
import edu.wisc.drivesense.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MenuFragment extends Fragment {
    private static final String TAG = "Menu Fragment";

    private OnFragmentInteractionListener mListener;

    private MenuOption optionAutomaticRecording;
    private MenuOption optionUploading;


    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_menu, container, false);

        optionAutomaticRecording = (MenuOption) getFragmentManager().findFragmentById(R.id.optionAutomaticRecording);
        optionUploading = (MenuOption) getFragmentManager().findFragmentById(R.id.optionUploading);

        setOptions();

        return result;
    }

    /**
     * Populate the menu options from the saved Demo user. Each line is a seperate line in the
     * menu and a different option for the user
     */
    private void setOptions() {
        User user = Concierge.getCurrentUser();

        //Automatic or Manual Recording
        optionAutomaticRecording.initialize("Automatic Recording", "Record when a trip starts", true, new MenuOption.MenuOptionDelegate() {
            @Override
            public void onMenuOptionClick(String title, boolean newValue) {
                Log.d(TAG, "Menu Button Pressed");
            }
        });

        //Uploads
        optionUploading.initialize("Uploading", "Upload trips to knowmydrive.com", true, new MenuOption.MenuOptionDelegate() {
            @Override
            public void onMenuOptionClick(String title, boolean newValue) {
                Log.d(TAG, "Menu Button Pressed");
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
