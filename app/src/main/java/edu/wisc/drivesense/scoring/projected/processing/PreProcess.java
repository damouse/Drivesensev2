package edu.wisc.drivesense.scoring.projected.processing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;

public class PreProcess {

    /**
     * Calculate rotation matricies from accel and magnetic readings. Need context to access the sensor monitor.
     */
    public static TimestampQueue<Reading> calculateRotationMatricies(DataSetInput period, Context context) {
        Log.i("Preprocess", "Calculating rotation matricies...");
        TimestampQueue<Reading> result = new TimestampQueue<Reading>();
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        for (Reading accel: period.acceleration) {
            Reading magnet = period.magnet.getClosestTimestamp(accel.timestamp);
            float[] rotation = new float[9];

            sensorManager.getRotationMatrix(rotation, null, accel.getFloatValues(), magnet.getFloatValues());
            result.push(new Reading(rotation, accel.timestamp, Reading.Type.ROTATION_MATRIX));
        }

        return result;
    }


    /**
     * @param raw  the input data to be interpolated
     * @param rate samples per second
     * @return
     */
    public static List<Reading> interpolate(List<Reading> raw, double rate) {
        List<Reading> res = new ArrayList<Reading>();

        int sz = raw.size();
        assert sz > 0 && rate >= 1;
        long x = raw.get(0).timestamp / 1000 * 1000 + 1000;
        for (int i = 0; i < sz - 1; ++i) {
            Reading cur = raw.get(i);
            Reading next = raw.get(i + 1);
            if (x >= cur.timestamp && x < next.timestamp) {
                Reading inter = new Reading(cur);
                inter.timestamp = x;

                //Log.log(x/1000 - 1379879638, cur.values[2]);
                //assert (x/1000 - 1379879638) < 190;
                for (int j = 0; j < inter.dimension; ++j) {
                    long x1 = cur.timestamp, x2 = next.timestamp;
                    double y1 = cur.values[j], y2 = next.values[j];

                    double v1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
                    //double v2 = y2 - (x2 - x) * (y2 - y1) / (x2 - x1);
                    inter.values[j] = v1;
                }
                res.add(inter);
                x += (1000.0 / rate);
                --i;
            }
        }

        return res;
    }
	

	/*
	 * Link: http://en.wikipedia.org/wiki/Moving_average
	 * */

    /*
     * get the average of given input
     * */
    public static Reading getAverage(List<Reading> input) {
        return simpleMovingAverage(input, input.size()).get(0);
    }

    /*
     * given a window size, get the moving average of the Reading values
     * e.g., for an array {1, 2, 3, 4, 5}, and wnd = 3
     * the moving result is {(1+2+3)/3, (2+3+4)/3, (3+4+5)/3}
     * return the overall average if wnd = input.size();
     * */
    public static List<Reading> simpleMovingAverage(List<Reading> input, int wnd) {
        List<Reading> res = new ArrayList<Reading>();
        int sz = input.size();
        int d = input.get(sz - 1).dimension;
        double[] sum = new double[d];
        for (int j = 0; j < d; ++j) sum[j] = 0.0;

        for (int i = 0, len = 1; i < sz; ++i, ++len) {
            Reading temp = input.get(i);
            for (int j = 0; j < d; ++j) {
                sum[j] += temp.values[j];
            }
			/**/

            if (len == wnd) {
                --len;
                double values[] = new double[d];

                for (int j = 0; j < d; ++j) {
                    values[j] = sum[j] / wnd;
                    sum[j] -= input.get(i - wnd + 1).values[j];
                }

                res.add(new Reading(values, temp.timestamp, temp.type));
            }

        }
        return res;
    }


    /*get the weighted moving average
     *  e.g., for an array {1, 2, 3, 4, 5}, and wnd = 3
     *  accu = wnd * (wnd + 1)/2 = 6
     * the moving result is {(1*1+2*2+3*3)/accu, (2*1+3*2+4*3)/accu, (3*1+4*2+5*3)/accu}
     * the length of result List<Reading> is wnd shorter than input Readings
     * */
    public static List<Reading> weightedMovingAverage(List<Reading> Readings, int wnd) {

        int sz = Readings.size();
        LinkedList<Reading> sliding = new LinkedList<Reading>();

        List<Reading> res = new ArrayList<Reading>();

        for (int i = 0; i < sz; ++i) {
            Reading Reading = Readings.get(i);
            sliding.add(Reading);
            int len = sliding.size();

            if (len == wnd) {
                double[] average = weightedAverage(sliding, wnd);
                sliding.removeFirst();
                Reading temp = new Reading(Reading);
                temp.values = average.clone();
                res.add(temp);
            }
        }
        return res;

    }

    static public double[] weightedAverage(List<Reading> Readings, int wnd) {
        int sz = Readings.size();
        int d = Readings.get(sz - 1).dimension;
        double accu = (double) wnd * (double) (wnd + 1) / 2.0;

        double[] res = new double[d];
        for (int j = 0; j < d; ++j) {
            res[j] = 0.0;
        }
        int i = 1;
        for (Reading Reading : Readings) {
            for (int j = 0; j < d; ++j) {
                res[j] += Reading.values[j] * i;
            }
            ++i;
        }
        for (int j = 0; j < d; ++j) {
            res[j] /= accu;
        }

        return res;
    }

    /** alpha is from 0 to 1
     * if alpha is 1, the result List is exactly the same to input Readings
     * if alpha is 0, the result List is a List of the first value of input Readings
     * */
    public static TimestampQueue exponentialMovingAverage(TimestampQueue<Reading> readings) {
        double alpha = Parameters.kExponentialMovingAverageAlpha;
        TimestampQueue result = new TimestampQueue();

        for (Reading oldReading: readings) {
            Reading reading = new Reading(oldReading);

            if (result.size() != 0) {
                for (int i = 0; i < reading.dimension; i++)
                    reading.values[i] = alpha * reading.values[i] + (1.0 - alpha) * readings.peekLast().values[i];
            }

            result.push(reading);
        }

        return result;
    }
	

	/*
	 * make the Reading starts from time 0
	 * 
	 * */
    public static List<Reading> ClearTimeOffset(List<Reading> Readings) {
        int offset = (int) Readings.get(0).timestamp;
        List<Reading> res = new ArrayList<Reading>();

        for (int i = 0; i < Readings.size(); i++) {
            Reading reading = Readings.get(i);
            long timestamp = reading.timestamp + offset;
            res.add(new Reading(reading.values, timestamp, reading.type));
        }
        return res;
    }
	
	
	
	/*extract a sublist of a given List<Reading>, using binary search 
	 * 
	 * any interval in the implementation is [si, ei), from si, inclusive, to ei, exclusive.
	 * */

    private static int binarySearch(List<Reading> raw, int si, int ei, long target) {
        int mid = 0;
        ei -= 1;
        while (si <= ei) {
            mid = si + (ei - si) / 2;
            if (raw.get(mid).timestamp == target) {
                break;
            } else if (raw.get(mid).timestamp < target) {
                si = mid + 1;
            } else {
                ei = mid - 1;
            }
        }
        return mid;
    }

    public static List<Reading> extractSubList(List<Reading> raw, long start, long end) {

        int si = binarySearch(raw, 0, raw.size(), start);
        //Log.error(start, raw.get(si).timestamp);
        int ei = binarySearch(raw, si, raw.size(), end);
        //Log.error(end, raw.get(ei).timestamp);
        return raw.subList(si, ei + 1);
    }


}
