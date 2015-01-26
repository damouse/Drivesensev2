package edu.wisc.drivesense.views;

import android.content.Context;
import android.graphics.*;

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

        if (event.type == MappableEvent.Type.acceleration)
            result = getAccelerationBitmap();

        else if (event.type == MappableEvent.Type.brake)
            result = getBrakeBitmap();

        else if (event.type == MappableEvent.Type.turn)
            result = getTurnnBitmap();

        else if (event.type == MappableEvent.Type.lanechange)
            result = getLaneChangeBitmap();


        result = changeColor(result, colorForScore(event.score));
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


    private Bitmap changeColor(Bitmap src, int color) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        int replace = Color.rgb(0, 0, 0);

        // get pixel array from source
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int pixel;

        // iteration through pixels
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                int index = y * width + x;
                pixel = pixels[index];

                if(pixel == replace){
                    pixels[index] = color;
                }
            }
        }
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }
//

    /**
     * TODO: return a pastel that ranges between red and green, proportionally moving based on the
      input Score: 0 is red and 100 is green.

     * @param score score value from 0 to 100
     * @return a Color that ranged from red to green based on Score-- faded to be light, drawn from pastel palette
     */
    public static int colorForScore(double score) {
        int percent = (int) score;

        int r = 244 - 168 * percent/100;
        int g = 67 +108 * percent/100;
        int b = 54 + 26 * percent/100;

        return Color.rgb(r, g, b);
    }

    int getTrafficlightColor(double value){
        return android.graphics.Color.HSVToColor(new float[]{(float)value*120f,1f,1f});
    }
}
