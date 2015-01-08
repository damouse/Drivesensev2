package edu.wisc.drivesense.scoring.projected.processing;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.model.Reading;
import edu.wisc.drivesense.scoring.projected.transformation.PairDouble;

public class Formulas {

    public static double vectorLength(Reading Reading) {
        return vectorLength(Reading, Reading.dimension);
    }

    /**
     * The name vectorSum is very very confusing!!!!!
     * It is actually the length of a vector!
     *
     * @param Reading
     * @param dim
     * @return
     */

    public static double vectorLength(Reading Reading, int dim) {
        double sum = 0.0;
        for (int i = 0; i < dim; ++i) {
            sum += Math.pow(Reading.values[i], 2);
        }
        double res = Math.sqrt(sum);
        return res;
    }

    public static double[] vectorSum(Reading Reading1, Reading Reading2) {
        if (Reading1.dimension != Reading2.dimension) {
            throw new IllegalArgumentException("The dimension of two Reading values must be the same");
        }

        double[] value1 = Reading1.values;
        double[] value2 = Reading2.values;
        double[] sum = new double[Reading1.dimension];

        for (int i = 0; i < Reading1.dimension; i++) {
            sum[i] = value1[i] + value2[i];
        }

        return sum;
    }

    /**
     * Pure mathematical equations. Get the sum of distance squared from the points to the line.
     *
     * @param slope
     * @param sum_x2
     * @param sum_y2
     * @param sum_xy
     * @return
     */

    public static double DistanceSquare(double slope, double sum_x2, double sum_y2, double sum_xy) {
        double res = (sum_y2 - 2 * slope * sum_xy + Math.pow(slope, 2) * sum_x2) / (Math.pow(slope, 2) + 1);
        return res;
    }

    /**
     * Find the unit vector.
     *
     * @param v
     * @return
     */
    public static PairDouble UnitVector(PairDouble v) {
        double length = Math.sqrt(Math.pow(v.y, 2) + Math.pow(v.x, 2));

        if (length != 0) {
            return new PairDouble(v.x / length, v.y / length);
        }
        return new PairDouble(0, 0);
    }

    public static double DotProduct(PairDouble v, PairDouble u) {
        return v.x * u.x + v.y * u.y;
    }


    /*0 to 360
     *
     * degree difference from d2 to d1
     * */
    public static double degreeDifference(double d1, double d2) {
        double diff = d2 - d1;
        if (diff > 180.0) {
            diff = diff - 360.0;
        } else if (diff < -180.0) {
            diff = diff + 360.0;
        } else {
        }

        return diff;
    }

    /**
     * Given a list of Readings and the dimension to Extract, return the list of unit vectors
     * along this DIMENSIONs.
     *
     * @param Readings  the list of Readings
     * @param DIMENSION Defined in Parameters.
     *                  Parameters.kDimensionXY: extract unit vector along x axis and y axis
     *                  Parameters.kDimensionYZ: extract unit vector along y axis and z axis
     *                  Parameters.kDimensionXZ: extract unit vector along x axis and z axis
     * @return a list of pair doubles that contains unit vector along the specified dimension
     * The list order is the same as Readings list
     */
    public static List<PairDouble> extractUnitVector(List<Reading> Readings, int DIMENSION) {
        if (DIMENSION != Parameters.kDimensionXY && DIMENSION != Parameters.kDimensionXZ && DIMENSION != Parameters.kDimensionYZ) {
            throw new IllegalArgumentException("Make sure you only pass in the arguments specified in Parameters");
        }
        List<PairDouble> unitVectors = new ArrayList<PairDouble>();
        for (Reading Reading : Readings) {
            PairDouble unitVector = null;
            switch (DIMENSION) {
                case Parameters.kDimensionXY:
                    unitVector = UnitVector(new PairDouble(Reading.values[0], Reading.values[1]));
                    break;
                case Parameters.kDimensionYZ:
                    unitVector = UnitVector(new PairDouble(Reading.values[1], Reading.values[2]));
                    unitVectors.add(unitVector);
                    break;
                case Parameters.kDimensionXZ:
                    unitVector = UnitVector(new PairDouble(Reading.values[0], Reading.values[2]));
                    unitVectors.add(unitVector);
                    break;
            }
            unitVectors.add(unitVector);
        }
        return unitVectors;
    }

    /**
     * @param Readings
     * @return [deviation]
     */
    public static double[] absoluteDeviation(List<Reading> Readings) {
        int sz = Readings.size();
        int d = Readings.get(sz - 1).dimension;

        double[] average = new double[d];
        double[] deviation = new double[d];
        for (int j = 0; j < d; ++j) {
            average[j] = 0.0;
            deviation[j] = 0.0;
        }
        for (Reading Reading : Readings) {
            for (int j = 0; j < d; ++j) {
                average[j] += Reading.values[j];
            }
        }
        for (int j = 0; j < d; ++j) {
            average[j] /= sz;
        }
        for (Reading Reading : Readings) {
            for (int j = 0; j < d; ++j) {
                deviation[j] += Math.abs(average[j] - Reading.values[j]);
            }
        }
        /*
        double [][] res = new double[2][d];
		for(int j = 0; j < d; ++j) {
			deviation[j] /= sz;
			res[0][j] = average[j];
			res[1][j] = deviation[j];
		}
		*/
        return deviation;
    }

    /*
     * For a given Reading (preferably the raw accelerometer data, but apply to all)
     * return the standard deviation of the Readings
     * */
    public static double[] standardDeviation(List<Reading> Readings) {
        int sz = Readings.size();
        int d = Readings.get(sz - 1).dimension;

        double[] average = new double[d];
        double[] res = new double[d];
        for (int j = 0; j < d; ++j) {
            average[j] = 0.0;
            res[j] = 0.0;
        }
        for (Reading Reading : Readings) {
            for (int j = 0; j < d; ++j) {
                average[j] += Reading.values[j];
            }
        }
        for (int j = 0; j < d; ++j) {
            average[j] /= sz;
        }
        for (Reading Reading : Readings) {
            for (int j = 0; j < d; ++j) {
                res[j] += Math.pow((average[j] - Reading.values[j]), 2.0);
            }
        }
        for (int j = 0; j < d; ++j) {
            res[j] = Math.sqrt(res[j] / sz);
        }

        return res;
    }


    public static double linear_correlation(double[] x, double[] y) {
        double corr = 0.0;
        int sz = x.length;
        double average_x = 0.0;
        double average_y = 0.0;
        for (int i = 0; i < sz; ++i) {
            average_x += x[i];
            average_y += y[i];
        }
        average_x /= sz;
        average_y /= sz;

        double upper = 0.0;
        double m_x = 0.0, m_y = 0.0;
        for (int i = 0; i < sz; ++i) {
            upper += (x[i] - average_x) * (y[i] - average_y);
            m_x += (x[i] - average_x) * (x[i] - average_x);
            m_y += (y[i] - average_y) * (y[i] - average_y);
        }
        if (m_x * m_y == 0 || m_x * m_y != m_x * m_y) corr = 1;
        else corr = upper / Math.sqrt(m_x * m_y);

        return corr;
    }


    /**
     * Given a list of unit vectors, calculate the standard deviation
     *
     * @param unitVectors the list of unit vectors
     * @return 2d array that stores the standard deviation value
     */
	/*
	public static double[] standardDeviationUnitVector(List<PairDouble> unitVectors) {
		int sz = unitVectors.size();
		double[] average = new double[2];
		double[] res = new double [2];
		for (int i = 0; i < 2; i++)
		{
			average[i] = 0;
			res[i] = 0;
		}
		for (int i = 0; i < sz; i++)
		{
			average[0] += unitVectors.get(i).x;
			average[1] += unitVectors.get(i).y;
		}
		average[0] /= sz;
		average[1] /= sz;
		for (PairDouble vector : unitVectors)
		{
			res[0] += Math.pow(vector.x - average[0], 2.0);
			res[1] += Math.pow(vector.y - average[1], 2.0);
		}
		res[0] = Math.sqrt(res[0]/sz);
		res[1] = Math.sqrt(res[1]/sz);
		return res;
	}*/
	
	/*
	 * For a given Reading (preferably the raw accelerometer data, but apply to all)
	 * return the standard deviation of the Readings
	 * */
    public static double standardDeviationDegree(List<Reading> Readings) {
        int sz = Readings.size();
        double sum = 0.0;

        for (Reading t : Readings) {
            t.calculateDegrees();
            sum += t.degrees;
        }
        double average = sum / sz;
        double res = 0.0;
        for (Reading t : Readings) {
            res += Math.pow(t.degrees - average, 2.0);
        }
        res = Math.sqrt(res / sz);
        return res;
    }
}
