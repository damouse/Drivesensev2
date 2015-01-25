package edu.wisc.drivesense.controllers.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.Concierge;
import edu.wisc.drivesense.model.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link edu.wisc.drivesense.controllers.fragments.MenuFragment.MenuDelegate} interface
 * to handle interaction events.
 */
public class MenuFragment extends Fragment {
    private static final String TAG = "Menu Fragment";

    private MenuDelegate delegate;
    private User user;

    private MenuOption optionAutomaticRecording;
    private MenuOption optionUploading;

    private Button buttonLogin;
    private TextView textviewName;

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
        user = Concierge.getCurrentUser();

        optionAutomaticRecording = (MenuOption) getFragmentManager().findFragmentById(R.id.optionAutomaticRecording);
        optionUploading = (MenuOption) getFragmentManager().findFragmentById(R.id.optionUploading);

        setOptions(user);
        setLogin(user);

        buttonLogin = (Button) result.findViewById(R.id.login);
        buttonLogin.setOnClickListener(new ButtonListner());
        textviewName = (TextView) result.findViewById(R.id.textviewName);

        return result;
    }

    /**
     * Populate the menu options from the saved Demo user. Each line is a seperate line in the
     * menu and a different option for the user
     */
    private void setOptions(User user) {

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            delegate = (MenuDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        delegate = null;
    }


    /* Login Button State */
    /**
     * Set the text of the login button, the login text, and the user's image (if needed)
     * Note that there is no "logged out" state-- no logged in user means a demo user is present
     */
    private void setLogin(User user) {
        if (user.demoUser()) {
            textviewName.setText("Not logged in");
            buttonLogin.setText("log in");
        }
        else {
            textviewName.setText(user.email);
            buttonLogin.setText("log out");
        }
    }


    /* Menu Callbacks */
    /**
     * Called when the login/logout button is pressed.
     *
     * If the user is logged in, log the user out. Else present login dialog.
     */
    private class ButtonListner implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {
            if (user.demoUser()) {
                Concierge.logOut();
                user = Concierge.getCurrentUser();
                setLogin(user);
                delegate.loadUser();
            }
            else {

            }
        }
    }


    /* Activity Callbacks */
    public interface MenuDelegate {
        // TODO: Update argument type and name
        public void loadUser();
    }
}
