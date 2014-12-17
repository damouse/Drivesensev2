package edu.wisc.drivesense.scoring.projected.transformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.processing.*;

public class AccelerationDetection {

	/*TODO tobe changed/removed*/

    private double threshold_;
    private double percent_;
    private int window_;


    public AccelerationDetection() {
        threshold_ = 0.05;
        percent_ = 0.7;
    }

    public AccelerationDetection(double threshold, int wsz) {
        threshold_ = threshold;
        window_ = wsz;
        percent_ = 0.7;
    }

    public void setValueThreshold(double threshold) {
        threshold_ = threshold;
    }

    public void setPercentage(double percent) {
        percent_ = percent;
    }


    /**
     * This function returns index of Readings where the car starts moving.
     * Basically whether a car moves is determined by whether within a window
     * length, 70% of Readings have sum of acceleration > 0.05
     *
     * @param smoothed_before
     * @return
     */
    public int startMoving(List<Reading> smoothed_before) {
        int countMove = 0;
        for (int i = 0; i < smoothed_before.size(); i++) {
            if (Formulas.vectorLength(smoothed_before.get(i), 2) > threshold_) {
                countMove++;
            }
            if (i >= window_) {
                if (Formulas.vectorLength(smoothed_before.get(i - window_), 2) > threshold_) {
                    countMove--;
                }
                if (((double) countMove) / window_ > percent_) {
                    int index = i + 1 - window_;
                    return index;
                }
            }
        }
        return -1; // return -1 means no movement.
    }

    /**
     * Get the index of Readings from which the car start moving
     *
     * @param calculated Readings after calculated with the rotation method
     * @param wnd        window size
     * @param threshold  threshold for the standard deviation
     * @return movement's starting index
     */
    @SuppressWarnings("unchecked")
    public int getInitialAccelerationPeakIndex(List<Reading> calculated, int wnd, double threshold) {
        //first calculate the vector sum for all Readings
        List<Reading> sumList = new ArrayList<Reading>();
        for (Reading t : calculated) {
            Reading curr = new Reading(t);
            double[] values = {Formulas.vectorLength(t, 2)};
            curr.setValues(values);
            sumList.add(curr);
        }

        //ReadWriteReading.writeFile(sumList, AllenTest.outputPath.concat("vectorSum_AfterCalculated.dat"));
        int sz = sumList.size(), startIndex = 0;
        List<Reading> targetSliding = new LinkedList<Reading>();
        LinkedList<Reading> sliding = new LinkedList<Reading>();
        for (int i = 0; i < sz; i++) {
            sliding.add(sumList.get(i));
            int len = sliding.size();
            if (len == wnd) {
                double[] deviations = Formulas.standardDeviation(sliding);
                if (deviations[0] > threshold) {
                    targetSliding = (List<Reading>) sliding.clone();
                    startIndex = i - wnd + 1;
                    break;
                }
                sliding.removeFirst();
            }
        }
        //ReadWriteReading.writeFile(targetSliding, AllenTest.outputPath.concat("selected_peak.dat"));
        double largest = Double.NEGATIVE_INFINITY;
        int targetIndex = startIndex;
        //Reading target = null;
        for (Reading t : targetSliding) {
            if (Math.abs(t.values[0]) > largest) {
                largest = Math.abs(t.values[0]);
                targetIndex = startIndex;
                //target = t;
            }
            startIndex++;
        }
        return targetIndex;
    }

    /**
     * Return the start index of Readings where the more than 70% of the Readings in the current
     * window has its current acceleration less than the that immediate before it.
     *
     * @param Readings
     * @param start
     * @return
     */
    public int SwitchMovement(List<Reading> Readings, int start) {
        int cntDec = 0;
        for (int i = start + 1; i < Readings.size(); i++) {
            if (Formulas.vectorLength(Readings.get(i), 2) < Formulas.vectorLength(Readings.get(i - 1), 2)) {
                cntDec++;
            }

            if (i > start + window_) {
                if (Formulas.vectorLength(Readings.get(i - window_), 2) < Formulas.vectorLength(Readings.get(i - window_ - 1), 2)) {
                    cntDec--;
                }
                if ((double) cntDec / window_ > percent_) {
                    return i + 1 - window_;
                }
            }
        }

        return -1;
    }
}
