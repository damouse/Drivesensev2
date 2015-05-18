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

    private int size = 700;
    private int strokeBuffer;

    private Path triangle;
    private Path diamond;

    private Context context;


    public BitmapLoader(Context context) {
        this.context = context;

        //The triangles should be a little bigger than "size" in order to look proportional with the
        //circles. This does change the size of the resulting bitmap,
        int strokeBuffer = 0;
        size += 2 * strokeBuffer;

        int triSize = size;//(int) (((double)size) * 1.1);
        int height = (int) Math.sqrt(Math.pow(triSize, 2) - Math.pow((triSize / 2), 2));
        int halfSize = (int) triSize / 2;


        triangle = new Path();
        triangle.setFillType(Path.FillType.EVEN_ODD);
        triangle.lineTo(halfSize + strokeBuffer, strokeBuffer);
        triangle.lineTo(triSize + strokeBuffer, height + strokeBuffer);
        triangle.lineTo(strokeBuffer, height + strokeBuffer);
        triangle.lineTo(halfSize + strokeBuffer,  strokeBuffer);
        triangle.close();

        diamond = new Path();
        diamond.setFillType(Path.FillType.EVEN_ODD);
        diamond.lineTo(halfSize + strokeBuffer, 0 + strokeBuffer);
        diamond.lineTo(triSize + strokeBuffer, halfSize + strokeBuffer);
        diamond.lineTo(halfSize + strokeBuffer, triSize + strokeBuffer);
        diamond.lineTo(0 + strokeBuffer, halfSize + strokeBuffer);
        diamond.lineTo(halfSize + strokeBuffer, 0 + strokeBuffer);
        diamond.close();

        //stroking the canvas makes the image clip. Stroke on a smaller region.

    }

    /* Bitmap getters */
    public Bitmap getBitmap(MappableEvent event) {

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(colorForScore(event.score));

        Paint outline = new Paint();
        outline.setStyle(Paint.Style.STROKE);
        outline.setColor(Color.BLACK);
        outline.setStrokeWidth(5);

        if (event.type == MappableEvent.Type.acceleration) {
            canvas.drawCircle(size / 2 + strokeBuffer, size / 2 + strokeBuffer, size / 2 + strokeBuffer, paint);
            canvas.drawCircle(size / 2 + strokeBuffer, size / 2 + strokeBuffer, size / 2 + strokeBuffer, outline);
        }

        else if (event.type == MappableEvent.Type.brake) {
            canvas.drawRect(0, 0, size, size, paint);
            canvas.drawRect(0, 0, size, size, outline);
        }

        else if (event.type == MappableEvent.Type.turn) {
            canvas.drawPath(triangle, paint);
            canvas.drawPath(triangle, outline);
        }

        else if (event.type == MappableEvent.Type.lanechange) {
            canvas.drawPath(diamond, paint);
            canvas.drawPath(diamond, outline);
        }
        return bitmap;
    }


    /**
     * Return a pastel that ranges between red and green, proportionally moving based on the
     * input Score: 0 is red and 100 is green.
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
        return android.graphics.Color.HSVToColor(new float[]{(float) value * 120f, 1f, 1f});
    }

}
