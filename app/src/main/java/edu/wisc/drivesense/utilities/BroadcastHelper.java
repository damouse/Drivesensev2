package edu.wisc.drivesense.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * This is a convenience class for making interacting with Broadcasts more pleasant.
 *
 * Doesn't do anything magical, just allows for cleaner broadcast code.
 */
public class BroadcastHelper {
    private static final String TAG = "BroadcastHelper";

    public static void sendBroadcast(String filter, Context context) {
        Intent startupIntent = new Intent(filter);

        try {
            context.sendBroadcast(startupIntent);
        } catch (NullPointerException ex) {
            Log.e(TAG, "Intent broadcast failed. Error: " + ex.toString());
        }
    }

    public static BroadcastReceiver registerForBroadcast(String filter, Context context, BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter(filter);
        context.registerReceiver(receiver, intentFilter);
        return receiver;
    }

    /**
     * Check and see if the receiver is null, then unregister it.
     */
    public static void unregisterReceiver(BroadcastReceiver receiver, Context context) {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }
}
