package edu.wisc.drivesense.businessLogic;

import android.content.Context;
import android.location.LocationManager;
import android.widget.Toast;

import cn.pedant.SweetAlert.SweetAlertDialog;
import edu.wisc.drivesense.model.User;
import edu.wisc.drivesense.sensors.PowerListener;

/**
 * Created by Damouse on 12/19/2014.
 *
 * The Toast class. Tracks and provides information about current
 * service status and availiablilty, and shows Toasts and Alerts
 * as needed.
 *
 * Should be used statically, but this may not be possible.
 */
public class Seatbelt {

    //should seatbelt show an alert or a toast on errors? Note-- alert overrides toast.
    public static boolean showAlert = true;
    public static boolean showToast = false;

    /**
     * Given a user, check to make sure recording is cleared to occur-- ensure the device is
     * powered, gps is on, a gyroscope exists, etc. If any of these checks fail, return false
     * and show an alert.
     *
     * @param user the active user
     * @return true if recording is clear to go ahead, false otherwise
     */
    public static boolean manualRecordingCheck(User user, Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            notifyUser("Gps is turned off!", context);
            return false;
        }

        //check for accelerometer and gyroscope presence

        return true;
    }

    /**
     * Same as the above method, but for automatic recording.
     */
    public static boolean automaticRecordingCheck(User user, Context context) {
        boolean allClear = true;

        allClear = allClear && (PowerListener.isPluggedIn(context) || user.isAutomaticUnpoweredRecording());

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        allClear = allClear && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //check for Accelerometer and gps presence

        return allClear;
    }


    /* Notifications */
    private static void notifyUser(String message, Context context) {
        if (showAlert) {
            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Oops...")
                    .setContentText(message)
                    .show();
        }
        else if (showToast) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
