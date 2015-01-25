package edu.wisc.drivesense.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
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

    /**
     * TODO: return a pastel that ranges between red and green, proportionally moving based on the
      input Score: 0 is red and 100 is green.

     * @param score score value from 0 to 100
     * @return a Color that ranged from red to green based on Score-- faded to be light, drawn from pastel palette
     */
    public static int colorForScore(double score) {

        int percent = (int) Math.abs(100 - score);

        int r = 244 - 168 * percent/100;
        int g = 67 +108 * percent/100;
        int b = 54 + 26 * percent/100;

        return Color.rgb(r, g, b);

    }

    int getTrafficlightColor(double value){
        return android.graphics.Color.HSVToColor(new float[]{(float)value*120f,1f,1f});
    }
}
