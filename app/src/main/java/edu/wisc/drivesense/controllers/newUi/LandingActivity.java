package edu.wisc.drivesense.controllers.newUi;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.wisc.drivesense.R;

import edu.wisc.drivesense.views.newUi.MenuFragment;
import edu.wisc.drivesense.views.newUi.SettingsFragment;
import edu.wisc.drivesense.views.newUi.resideMenu.ResideMenu;


public class LandingActivity extends FragmentActivity implements View.OnClickListener,
        MenuFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener {
    private static final String TAG = "LandingActivity";

    private ResideMenu resideMenu;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        setUpMenu();
    }

    private void setUpMenu() {
        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {
        Log.d("Menu", "Touch");
//        resideMenu.closeMenu();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            Log.d(TAG, "Menu Opened");
//            Toast.makeText(getApplication().getApplicationContext(), "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            Log.d(TAG, "Menu closed");
//      Toast.makeText(getApplication().getApplicationContext(), "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };

    private void changeFragment(Fragment targetFragment){
//        resideMenu.clearIgnoredViewList();
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.main_fragment, targetFragment, "fragment")
//                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                .commit();
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu(){
        return resideMenu;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "Something happened in a menu fragment");
    }
}
