package edu.wisc.drivesense.controllers;

import android.app.Application;
import edu.wisc.drivesense.businessLogic.Concierge;

public class DrivesenseApplication extends Application {
	@Override
	public void onCreate() {
	    super.onCreate();

        //loads the demo user before anyone needs it. Loads the demo user on first run
//        Concierge.initializeConcierge();
	}
}
