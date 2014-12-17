package edu.wisc.drivesense.scoring.projected.transformation;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;

public class TransformationHelper {

    private Reading rotation_matrix = new Reading(new double[9], 0, Reading.Type.GYROSCOPE);
    private PairDouble newAxes[] = new PairDouble[2];
    private boolean reversed = false;


    private boolean meta_set = false;

    public TransformationHelper() {
    }

    public TransformationHelper(Reading rm, PairDouble na[], boolean rev) {
        assert rm.dimension == 9;
        rotation_matrix = rm;
        assert na.length == 2;
        newAxes = na.clone();
        reversed = rev;


        meta_set = true;
    }

    public List<Reading> transformByAccelerometer(List<Reading> accelerometer, Reading rotation) {
        List<Reading> calculated = CoordinateTransformation.Calculate(accelerometer, rotation, "rotation");
        rotation_matrix = rotation;

        List<Reading> head_straight = CoordinateTransformation.getHeadingStraightReadings(calculated);
        ArrayList<PairDouble> raw_xy = new ArrayList<PairDouble>();

        for (Reading t : head_straight) {
            PairDouble xy = new PairDouble(t.values[0], t.values[1]);
            raw_xy.add(xy);
        }

        double slope = CoordinateTransformation.BestFitOrigin(raw_xy);

		/*two unit vectors of mapped x and y*/
        PairDouble[] rawAxes = CoordinateTransformation.AxesUnitVector(raw_xy, slope);
        newAxes = rawAxes.clone();

        //adjust the coordinate system
        ArrayList<Reading> mapped = new ArrayList<Reading>();

        for (Reading tr : calculated) {
            PairDouble vec = new PairDouble(tr.values[0], tr.values[1]);
            Reading newtr = new Reading(tr);

            newtr.values[0] = Formulas.DotProduct(vec, rawAxes[0]);
            newtr.values[1] = Formulas.DotProduct(vec, rawAxes[1]);
            newtr.values[2] = tr.values[2];
            mapped.add(newtr);
        }

        if (CoordinateTransformation.isCoordinateMappingReversed(mapped)) {
            reversed = true;

            for (Reading t : mapped) {
                t.values[1] *= -1;
                t.values[0] *= -1;
            }
        }

        meta_set = true;
        return mapped;
    }

    public List<Reading> transform(List<Reading> raw, boolean is_gyro) {
        if (meta_set == false) {
            Log.e("Transformation Helper", "TransformationHelper has not been set yet!");
            assert 0 == 1;
        }
        double factor = 9.4;
        if (is_gyro) {
            /*
			for(int i = 0; i < rotation_matrix.dimension; ++i)
				rotation_matrix.values[i]/=9.4;
				*/
			/*
			rotation_matrix.values[6]/=9.4;
			rotation_matrix.values[7]/=9.4;
			rotation_matrix.values[8]/=9.4;
			*/
        }

        //Log.log(rotation_matrix);

        List<Reading> input = CoordinateTransformation.Calculate(raw, rotation_matrix, "rotation");
        List<Reading> projected = new ArrayList<Reading>();
        int coeff = 1;
        if (reversed) coeff = -1;
        for (Reading tr : input) {
            PairDouble vec = new PairDouble(tr.values[0], tr.values[1]);
            Reading newtr = new Reading(tr);

            newtr.values[0] = coeff * Formulas.DotProduct(vec, newAxes[0]);
            newtr.values[1] = coeff * Formulas.DotProduct(vec, newAxes[1]);
            newtr.values[2] = tr.values[2];

            projected.add(newtr);
        }
        return projected;
    }
}
