package edu.wisc.drivesense.controllers.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.businessLogic.BackgroundState;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.server.ConnectionManager;
import edu.wisc.drivesense.server.DrivesensePreferences;

/**
 * Created by Damouse on 7/21/14.
 */
public class PreferenceActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "PreferenceActivity";
    PrefsFragment fragmentPreferences;
    //We manually control the state of the shared preference "loggedIn" in this activity not
    //based on the user's touch but instaed the actual login state. When the state is changed,
    //the callback method is called-- if this is false then disregard that change
    private boolean ignoreLoginChange;
    private boolean ignoreRecordingChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
//        FragmentManager mFragmentManager = getFragmentManager();
//        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
//        fragmentPreferences = new PrefsFragment();
//
//        mFragmentTransaction.replace(android.R.id.content, fragmentPreferences);
//        mFragmentTransaction.commit();
//
//        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
//
//        ignoreLoginChange = false;
//        ignoreRecordingChange = false;
//
//        //set default recording settings
//        DrivesensePreferences prefs = new DrivesensePreferences(this);
//
//        if (!prefs.manualRecording() && !prefs.backgroundRecording() && !prefs.backgroundRecordingPower()) {
//            ignoreRecordingChange = true;
//            prefs.setManualRecording(true);
//            ignoreRecordingChange = false;
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckBoxPreference login = (CheckBoxPreference) fragmentPreferences.findPreference("logInTrigger");
//
//        DrivesensePreferences drivesensePreferences = new DrivesensePreferences(this);
//        if (drivesensePreferences.loggedIn()) {
//            login.setSummary("Logged in as " + drivesensePreferences.userEmail());
//            login.setTitle("Logged In");
//
//            drivesensePreferences.setLoginFlag(true);
//        }
//        else {
//            login.setTitle("Log In");
//            drivesensePreferences.setLoginFlag(false);
//        }
    }

    /* Preference change interface implementation */
    /**
     * Called when a preference is changed in the activity

     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        DrivesensePreferences drivesensePreferences = new DrivesensePreferences(this);
//
//        if (s.equals("logInTrigger")) {
//            //if flag is set, disregard the login change
//            if (ignoreLoginChange) {
//                ignoreLoginChange = false;
//                return;
//            }
//
//            CheckBoxPreference login = (CheckBoxPreference)fragmentPreferences.findPreference("logInTrigger");
//
//            //If logged in log the current user out
//            if (drivesensePreferences.loggedIn()) {
//                login.setSummary("");
//                login.setTitle("Log In");
//                Toast.makeText(this.getApplicationContext(), "You have been signed out", Toast.LENGTH_SHORT);
//                drivesensePreferences.logOut();
//            }
//            else {
//                showLoginDialog();
//                ignoreLoginChange = true;
//                login.setChecked(false);
//            }
//        }
//
//        else {
//            if (!ignoreRecordingChange) {
//                CheckBoxPreference manual = (CheckBoxPreference) fragmentPreferences.findPreference("manualRecording");
//                CheckBoxPreference background = (CheckBoxPreference) fragmentPreferences.findPreference("backgroundRecording");
//                CheckBoxPreference unpowered = (CheckBoxPreference) fragmentPreferences.findPreference("backgroundRecordingPower");
//
//                Log.d(TAG, "Checkbox: " + s);
//                Log.d(TAG, "Manual, background, unpowered" + manual.isChecked() + " " + background.isChecked() + " " + unpowered.isChecked() + " " );
//
//                ignoreRecordingChange = true;
//
//                //switch between the three visible options, making them act like radio buttons
//                if (s.equals("manualRecording") || (!manual.isChecked() && !background.isChecked() && !unpowered.isChecked())) {
//                    if (BackgroundState.getState() != BackgroundState.State.UNINITIALIZED) {
//                        stopService(new Intent(this, BackgroundRecordingService.class));
//                    }
//
//                    manual.setChecked(true);
//                    background.setChecked(false);
//                    unpowered.setChecked(false);
//                } else {
//                    if (BackgroundState.getState() == BackgroundState.State.UNINITIALIZED) {
//                        startService(new Intent(this, BackgroundRecordingService.class));
//                    }
//
//                    if (s.equals("backgroundRecording")) {
//                        manual.setChecked(false);
//                        unpowered.setChecked(false);
//                    } else if (s.equals("backgroundRecordingPower")) {
//                        manual.setChecked(false);
//                        background.setChecked(false);
//                    }
//                }
//
//                ignoreRecordingChange = false;
//
//
//            }
//        }
    }


    /* Connection Manager Callbacks */
    public void onLoginCompletion(boolean success, User user, String response) {
//        final DrivesensePreferences preferences = new DrivesensePreferences(this.getApplicationContext());
//
//        //determine the cause of failure and alert the user
//        //Possible responsese:
////        'missing parameters'
////        'user not found'
////        'wrong password'
//        if (!success) {
//
//            if (response.equals("missing parameters")) {
//                new AlertDialog.Builder(this)
//                        .setTitle("Error")
//                        .setMessage("Looks like something went wrong-- you need to provide both email and password!")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//            }
//
//            else if (response.equals("user not found")) {
//                new AlertDialog.Builder(this)
//                        .setTitle("User not found")
//                        .setMessage("User \'" + preferences.userEmail() + "\' not found. Would you like to create a new account?")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                regiseterRequest();
//                            }
//                        })
//                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // do nothing
//                            }
//                        })
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//            }
//
//            else if (response.equals("wrong password")) {
//                new AlertDialog.Builder(this)
//                        .setTitle("Wrong password")
//                        .setMessage("The password you provided was incorrect!")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//            }
//
//            else if (response.equals("user already exists with that email")) {
//                new AlertDialog.Builder(this)
//                        .setTitle("Email taken")
//                        .setMessage("The email \'" + preferences.userEmail() + "\' is already in use. If this is your email and you've forgotten your password, please reset it at knowmydrive.com")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) { }
//                        })
//                        .show();
//            }
//
//            else if (response.equals("registration failed")) {
//                new AlertDialog.Builder(this)
//                        .setTitle("Registration Error")
//                        .setMessage("An error occured registering your account. Please enter a password that is at least 8 characters.")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) { }
//                        })
//                        .show();
//            }
//
//            else {
//                new AlertDialog.Builder(this)
//                        .setTitle("Error")
//                        .setMessage("It looks like something is wrong with the server or your internet connection")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();
//            }
//        }
//        else {
//            Log.d(TAG, "Login returned to prefsactivity");
//            preferences.logIn(user);
//
//            Toast.makeText(this.getApplicationContext(), "You have been signed in", Toast.LENGTH_SHORT);
//            CheckBoxPreference preference = (CheckBoxPreference) fragmentPreferences.findPreference("logInTrigger");
//
//            ignoreLoginChange = true;
//            preferences.setLoginFlag(true);
//            preference.setChecked(true);
//            preference.setTitle("Logged in");
//
//            preference.setSummary("Logged in as " + preferences.userEmail());
//        }
    }

    private void loginRequest() {
        DrivesensePreferences preferences = new DrivesensePreferences(this.getApplicationContext());
//        new ConnectionManager(getApplicationContext(), this).logIn(preferences.userEmail(), preferences.userPassword());
    }

    private void regiseterRequest() {
        DrivesensePreferences preferences = new DrivesensePreferences(this.getApplicationContext());
//        new ConnectionManager(getApplicationContext(), this).register(preferences.userEmail(), preferences.userPassword());
    }

    private void showLoginDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Login or Register");

        final EditText email = new EditText(this);
        final EditText password = new EditText(this);
        final Context context = getApplicationContext();

        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Password");

        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setHint("Email");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(email);
        layout.addView(password);
        alertDialog.setView(layout);

        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Login",  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DrivesensePreferences preferences = new DrivesensePreferences(context);
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();

                //Check to make sure all fields are filled
                if (emailString.equals("") || passwordString.equals(""))
                    Toast.makeText(context, "Email or Password was blank", Toast.LENGTH_SHORT);
                else {
                    preferences.setEmail(emailString);
                    preferences.setPassword(passwordString);
                    loginRequest();
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }



    /* Fragment implementation */
    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
