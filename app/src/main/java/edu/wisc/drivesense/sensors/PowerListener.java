package edu.wisc.drivesense.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;

/**
 * Created by Damouse on 6/25/14.
 */
public class PowerListener extends BroadcastReceiver {
    private final String TAG = "Power Listener";
    private Context savedContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        BackgroundRecordingService.getInstance().stateManager.setPowered(isPluggedIn(context));
    }

    /* Static Methods */
    public PowerListener(BackgroundRecordingService context) {

        IntentFilter filter = new IntentFilter();

        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");

        context.registerReceiver(this, filter);
        savedContext = context;
    }

    public PowerListener() {

    }


    public void unregister() {
        savedContext.unregisterReceiver(this);
    }


    public static boolean isPluggedIn(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

       return isCharging;
    }

    public static float batteryLevel(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level / (float)scale;
    }


}
