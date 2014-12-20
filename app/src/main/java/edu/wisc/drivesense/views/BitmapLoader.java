package edu.wisc.drivesense.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.model.MappableEvent;

/**
 * Created by Damouse on 12/19/2014.
 *
 * Hitting the resource path is expensive. Factory here preloads and stores
 * bitmaps on demand. Colors each returned bitmap using a spectrum based on
 */
public class BitmapLoader {
    private Bitmap acceleration = null;
    private Bitmap brake = null;
    private Bitmap turn = null;
    private Bitmap laneChange = null;

    private Context context;


    public BitmapLoader(Context context) {
        this.context = context;
    }


    /* Bitmap getters */
    public Bitmap getBitmap(MappableEvent event) {
        Bitmap result = null;

        if (event.type == MappableEvent.Type.ACCELERATION)
            result = getAccelerationBitmap();

        else if (event.type == MappableEvent.Type.BRAKE)
            result = getBrakeBitmap();

        else if (event.type == MappableEvent.Type.TURN)
            result = getTurnnBitmap();

        else if (event.type == MappableEvent.Type.LANE_CHANGE)
            result = getLaneChangeBitmap();


        //color based on score
        return result;
    }


    private Bitmap getAccelerationBitmap() {
        if (acceleration == null) {
            acceleration = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_acceleration);
        }

        return acceleration;
    }

    private Bitmap getBrakeBitmap() {
        if (brake == null) {
            brake = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_brake);
        }

        return brake;
    }

    private Bitmap getTurnnBitmap() {
        if (turn == null) {
            turn = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_turn);
        }

        return turn;
    }

    private Bitmap getLaneChangeBitmap() {
        if (laneChange == null) {
            laneChange = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_stop);
        }

        return laneChange;
    }


    /* Color Changing */
//    private int[] colorBitmap(int color, Bitmap bitmap) {
//        int pixels[] = null;
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        bitmap.getPixels(pixels, 0, 0, 0, 0, width, height);
//
//        for (int i = 0; i < width; i++) {
//            for (int j = 0; j < height; j++) {
//                if (pixels[i][j] == 0) {
//
//                }
//            }
//        }
//    }
//
    private int colorForScore(double score) {
        int percent = (int) Math.abs(100 - score);

        int r = ( (255 * percent) / 100 );
        int g = ( 255 * (100) ) / 100;
        int b = 0;

        return ((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff);

//        blue = 0;
//        green = 255 * sqrt( cos ( power * PI / 200 ));
//        red = 255 * sqrt( sin ( power * PI / 200 ));
    }

    int getTrafficlightColor(double value){
        return android.graphics.Color.HSVToColor(new float[]{(float)value*120f,1f,1f});
    }
}
