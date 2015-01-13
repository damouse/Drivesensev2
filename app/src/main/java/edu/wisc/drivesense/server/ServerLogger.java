package edu.wisc.drivesense.server;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import edu.wisc.drivesense.sensors.PowerListener;
import edu.wisc.drivesense.sensors.WifiListener;

/**
 * Created by Damouse on 11/4/14.
 * <p/>
 * Listen to WiFi and battery, if supposed to report location to server, upload this device's
 * IP address and power level.
 */
public class ServerLogger {
    private final String TAG = "ServerLogger";
    //turn off for production, use only with controlled environment and devices for remote SSH
    private final boolean shouldUpdateServer = false;
    //time to wait between logs. 10 minutes currently.
    private final int uploadDelay = 600000;
    Handler timerHandler;
    private Context context;
    //Timer-- main and most interesting method
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!WifiListener.isConnected(context)) {
                Log.d(TAG, "No wifi. Not logging device");
                return;
            }

            //issue new upload call
//            ConnectionManager api = new ConnectionManager(context, null);
//
//            float power = PowerListener.batteryLevel(context);
//            String ip = WifiListener.ipAddress(context);
//
//            WifiManager m_wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//            String mac = m_wm.getConnectionInfo().getMacAddress();
//
//
//            api.logDeviceWithServer(mac, ip, power, uploadDelay);
        }
    };

    public ServerLogger(Context context) {
        if (!shouldUpdateServer)
            return;

        timerHandler.postDelayed(timerRunnable, uploadDelay);
    }
}
