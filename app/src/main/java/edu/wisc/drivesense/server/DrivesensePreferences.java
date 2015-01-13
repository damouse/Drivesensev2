package edu.wisc.drivesense.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import edu.wisc.drivesense.model.User;

/**
 * Wrapper class for preferences. The majority of the calls are accessor functions, but there are a
 *
 * @author Damouse
 */
public class DrivesensePreferences {
    private final static String TAG = "DrivesensePreferences";
    private SharedPreferences preferences = null;
    private Context context;

    /* Boilerplate */
    public DrivesensePreferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

	
    /* Setters */
//	public void logIn(User user) {
//		Editor editor = preferences.edit();
////        editor.putInt("id", user.id);
//        editor.putBoolean("loggedIn", true);
//        editor.putString("email", user.email);
//        editor.commit();
//
//        //Save the user to the Database if does not exist
////        Sugar databaseManager = new Sugar(context);
////        if(databaseManager.getUser(user.id) == null)
////            databaseManager.createUser(user);
//	}

    /**
     * Log the user out, deleting all saved state
     */
//    public void logOut() {
//        Editor editor = preferences.edit();
//        editor.putInt("id", -1);
//        editor.putBoolean("loggedIn", false);
//        editor.commit();
//    }

//    public void setManualRecording(boolean set) {
//        Editor editor = preferences.edit();
//        editor.putBoolean("manualRecording", set);
//        editor.commit();
//    }
//
//	public void setBackgroundRecording(boolean set) {
//		Editor editor = preferences.edit();
//        editor.putBoolean("backgroundRecording", set);
//        editor.commit();
//	}
//
//    public void setBackgroundRecordingUnpowered(boolean set) {
//        Editor editor = preferences.edit();
//        editor.putBoolean("backgroundRecordingPower", set);
//        editor.commit();
//    }
//
//	public void setAutoUpload(boolean set) {
//		Editor editor = preferences.edit();
//        editor.putBoolean("autoUpload", set);
//        editor.commit();
//	}
//
//    public void setLoginFlag(boolean set) {
//        Editor editor = preferences.edit();
//        editor.putBoolean("logInTrigger", set);
//        editor.commit();
//    }
    public void setEmail(String email) {
        Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.commit();
    }

    public void setPassword(String password) {
        Editor editor = preferences.edit();
        editor.putString("password", password);
        editor.commit();
    }
/* Accessors */
    /*
     * Return the currently logged in user or null if no one is logged in
     */
//    public User loggedInUser() {
//        if (!preferences.getBoolean("loggedIn", false))
//            return null;
//
////        Sugar databaseManager = new Sugar(context);
////        return databaseManager.getUser(preferences.getInt("id", -1));
//        return  null; //TEMP
//    }
//
//    public String userEmail() { return preferences.getString("email", null); }
//    public String userPassword() { return preferences.getString("password", null); }
//
//    public boolean loggedIn() { return preferences.getBoolean("loggedIn", false); }
//    public boolean loginFlag() { return preferences.getBoolean("logInTrigger", false); }
//
//    public boolean manualRecording() { return preferences.getBoolean("manualRecording", false); }
//	public boolean backgroundRecording() { return preferences.getBoolean("backgroundRecording", false); }
//    public boolean backgroundRecordingPower() { return preferences.getBoolean("backgroundRecordingPower", false); }
//
//	public boolean autoUpload() {
//		return preferences.getBoolean("autoUpload", false);
//	}
//    public boolean autoUploadWifi() {
//        return preferences.getBoolean("autoUploadWifi", false);
//    }
//    public boolean autoUploadDelete() {
//        return preferences.getBoolean("autoUploadDelete", false);
//    }
}
