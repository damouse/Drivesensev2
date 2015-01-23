package edu.wisc.drivesense.controllers.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.pedant.SweetAlert.SuccessTickView;
import edu.wisc.drivesense.R;

/**
 * Created by Damouse on 1/16/15.
 *
 * Class is a custom implementation of a "settings" line as found in a preference
 * fragment. Adds a custom checkbox for toggling state with animations.
 */
public class MenuOption extends Fragment {
    private TextView textviewTitle;
    private TextView textviewSubtitle;

    private String title;
    private String subtitle;

    private boolean isSet;
    private MenuOptionDelegate delegate;

    FrameLayout frameSuccess;
    FrameLayout frameError;

    SuccessTickView iconSuccess;
    AnimationSet mErrorXInAnim;
    ImageView iconError;


    public MenuOption() {

    }

    public interface MenuOptionDelegate {
        public void onMenuOptionClick(String title, boolean newValue);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.menu_line, container, false);

        textviewTitle = (TextView) result.findViewById(R.id.title);
        textviewSubtitle = (TextView) result.findViewById(R.id.subtitle);

        return result;
    }

    public void initialize(String title, String subtitle, boolean set, MenuOptionDelegate callback) {
        this.title = title;
        this.subtitle = subtitle;

        this.textviewTitle.setText(title);
        this.textviewSubtitle.setText(subtitle);

        isSet = set;
        delegate = callback;
    }
}
