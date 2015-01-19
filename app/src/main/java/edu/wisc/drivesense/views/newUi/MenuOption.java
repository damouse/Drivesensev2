package edu.wisc.drivesense.views.newUi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.pedant.SweetAlert.OptAnimationLoader;
import cn.pedant.SweetAlert.SuccessTickView;
import edu.wisc.drivesense.R;

/**
 * Created by Damouse on 1/16/15.
 *
 * Class is a custom implementation of a "settings" line as found in a preference
 * fragment. Adds a custom checkbox for toggling state with animations.
 */
public class MenuOption extends LinearLayout {
    private TextView title;
    private TextView subtitle;

    FrameLayout frameSuccess;
    FrameLayout frameError;

    SuccessTickView iconSuccess;
    AnimationSet mErrorXInAnim;
    ImageView iconError;


    public MenuOption(Context context) {
        super(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_line, this);

        title = (TextView) findViewById(R.id.title);
        subtitle = (TextView) findViewById(R.id.subtitle);


    }
}
