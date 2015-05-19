package edu.wisc.drivesense.controllers.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.Concierge;
import edu.wisc.drivesense.model.User;


public class SettingsFragment extends Fragment {
    private MenuFragment.MenuDelegate delegate;
    private User user;

    private MenuOption optionUnpoweredRecording;
    private MenuOption optionWifiOffUploading;
    private MenuOption optionDelete;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_settings, container, false);
        user = Concierge.getCurrentUser();

        optionUnpoweredRecording = (MenuOption) getChildFragmentManager().findFragmentById(R.id.optionUnpoweredRecording);
        optionWifiOffUploading = (MenuOption) getChildFragmentManager().findFragmentById(R.id.optionWifiOff);
        optionDelete = (MenuOption) getChildFragmentManager().findFragmentById(R.id.optionDelete);

        setOptions(user);

        return result;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            delegate = (MenuFragment.MenuDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        delegate = null;
    }

    public void setOptions(final User user) {
        optionUnpoweredRecording.initialize("Unpowered Recording", "Record when not plugged in", user.isAutomaticUnpoweredRecording(), new MenuOption.MenuOptionDelegate() {
            @Override
            public void onMenuOptionClick(String title, boolean newValue) {
                user.setAutomaticUnpoweredRecording(newValue);
                user.save();
                delegate.userStateChanged();
            }
        });

        optionWifiOffUploading.initialize("Upload over Cellular", "Dont wait for WiFi", user.isAutomaticUploadOffWifi(), new MenuOption.MenuOptionDelegate() {
            @Override
            public void onMenuOptionClick(String title, boolean newValue) {
                user.setAutomaticUploadOffWifi(newValue);
                user.save();
                delegate.userStateChanged();
            }
        });

        optionDelete.initialize("Delete Trips", "Delete after uploading", user.isAutomaticDelete(), new MenuOption.MenuOptionDelegate() {
            @Override
            public void onMenuOptionClick(String title, boolean newValue) {
                user.setAutomaticDelete(newValue);
                user.save();
                delegate.userStateChanged();
            }
        });
    }
}
