package edu.wisc.drivesense.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.server.DrivesensePreferences;

/**
 * Created by Damouse on 10/1/14.
 */
public class BootStarterReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DrivesensePreferences prefs = new DrivesensePreferences(context);

        //do not boot if automati recording is not turned on
        if (prefs.manualRecording())
            return;

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, BackgroundRecordingService.class);
            context.startService(serviceIntent);
        }
    }
}