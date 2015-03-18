package edu.wisc.drivesense.controllers;

import android.app.Application;
import com.ubertesters.common.models.ActivationMode;
import com.ubertesters.common.models.LockingMode;
import com.ubertesters.sdk.Ubertesters;
import edu.wisc.drivesense.businessLogic.Concierge;

public class DrivesenseApplication extends Application {
	@Override
	public void onCreate() {
	    super.onCreate();
        Ubertesters.initialize(this, LockingMode.LockApplication, ActivationMode.Widget);

        //loads the demo user before anyone needs it. Loads the demo user on first run
//        Concierge.initializeConcierge();
	}
}
