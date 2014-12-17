package edu.wisc.drivesense.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class OpenSansLightTextview extends TextView {

	public OpenSansLightTextview(Context context) {
		super(context);
		initFont(context);
	}
    
	public OpenSansLightTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFont(context);
    }

    public OpenSansLightTextview(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         initFont(context);
    }
    
	private void initFont(Context context) {
		String otfName = "OpenSans-Light.ttf";
	    Typeface font = Typeface.createFromAsset(context.getAssets(), otfName);
	    this.setTypeface(font);
	}
}
