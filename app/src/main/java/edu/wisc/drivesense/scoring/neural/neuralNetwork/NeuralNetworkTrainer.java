package edu.wisc.drivesense.scoring.neural.neuralNetwork;

import org.encog.Encog;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import java.util.ArrayList;

import edu.wisc.drivesense.scoring.neural.modelObjects.DataSetInput;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;

import static edu.wisc.drivesense.scoring.DrivingAnalyst.log;

/**
 * Created by Damouse on 12/9/2014.
 * <p/>
 * Train and test the neural network.
 */
public class NeuralNetworkTrainer {
    NeuralNetwork network;
    MLDataSet trainingSet;

    /**
     * Create a new network with the passed number of nodes
     */
    public NeuralNetworkTrainer(int input, int hidden) {
        network = new NeuralNetwork(input, hidden);
        trainingSet = new BasicMLDataSet();
    }

    //This may not be necasary, but since the examples have it...
    public void stopNetwork() {
        Encog.getInstance().shutdown();
    }

    /**
     * Take a data wrapper, load the linked lists with the data from DataReceiver
     * @param data
     */
//    public ArrayList<DataSetInput> periodizeAndProcessRaw(DataSetInput data, int period, int memory) {
//        DataReceiver receiver = new DataReceiver(null, period, memory);
//
//        receiver.acceleration = data.acceleration;
//        receiver.magnet = data.magnet;
//        receiver.gravity = data.gravity;
//        receiver.gyroscope = data.gyroscope;
//        receiver.gps = data.gps;
//        receiver.labels = data.labels;
//
//        //get the earliest, last timestamp and use that as a guide for how long to ask for periods
//
//
//        ArrayList<DataSetInput> allPeriods = new ArrayList<DataSetInput>();
//
//        for (int i = 0; i < earliestLastReadingTime / period; i++) {
//            DataSetInput onePeriod = receiver.getProcessedPeriod();
//            if (onePeriod != null)
//                allPeriods.add(onePeriod);
//        }
//
//        return allPeriods;
//    }


    /* Training and testing */

    /**
     * Sets the training set with the passed array
     */
    public void addTrainingData(TimestampQueue<DataSetInput> dataSet) {
        for (DataSetInput set : dataSet) {
            if (set == null)
                log("ERROR- NULL SET");
            else {
                trainingSet.add(set.convertNetworkTrainingInput());
            }
        }
    }

    public void trainNetwork(int epochs) {
        ResilientPropagation train = new ResilientPropagation(network.network, trainingSet);

        for (int i = 0; i < epochs; i++) {
            train.iteration();
//            System.out.println("Epoch #" + i + " Error:" + train.getError());
        }

        train.finishTraining();
    }

    public void trainAndCrossValidate(int epochs, int folds) {

    }

    /**
     * Test network accuracy
     */
    public void testNetwork() {
        ArrayList<LabelPerformance> performances = new ArrayList<LabelPerformance>();
        performances.add(new LabelPerformance(0));
        performances.add(new LabelPerformance(1));
        performances.add(new LabelPerformance(2));

        for (MLDataPair pair : trainingSet) {
            MLData output = network.evaluate(pair.getInput());
            for (LabelPerformance performance : performances)
                performance.evaluateResult(pair, output);

        }

        for (LabelPerformance performance : performances) {
            performance.evaluatePerformance();
            log(performance.toString());
        }
    }
}


/* The performance of one type of label */
class LabelPerformance {
    float truePositiveRate = 0;
    float falsePositiveRate = 0;

    float accuracyForLabel = 0;
    float totalAccuracy = 0;

    float truePostive = 0;
    float trueNegative = 0;
    float falsePositive = 0;
    float falseNegative = 0;

    float totalLabelOccurances = 0;
    float totalInstances = 0;

    int indexOfLabel;


    LabelPerformance(int index) {
        indexOfLabel = index;
    }

    public static String stringToCSV(Object... strings) {
        StringBuilder sb = new StringBuilder();

        for (Object s : strings) {
            sb.append("" + s + "\t");
        }

        return sb.toString();
    }

    void evaluateResult(MLDataPair input, MLData output) {
        totalInstances++;

        if (input.getIdeal().getData(indexOfLabel) == 1) {
            totalLabelOccurances++;

            if (output.getData(indexOfLabel) == 1)
                truePostive++;
            else
                falseNegative++;
        } else {
            if (output.getData(indexOfLabel) == 1)
                falsePositive++;
            else
                trueNegative++;
        }
    }

    /* Evaluate our performance*/
    void evaluatePerformance() {
        truePositiveRate = truePostive / (truePostive + falseNegative);
        falsePositiveRate = falsePositive / (trueNegative + falsePositive);

        totalAccuracy = (truePostive + trueNegative) / totalInstances;
        accuracyForLabel = truePostive / totalLabelOccurances;
    }

    @Override
    public String toString() {

        String label = "Accelerations";
        if (indexOfLabel == 1) label = "Brakes";
        if (indexOfLabel == 2) label = "Turns";

        return stringToCSV(label, totalAccuracy, accuracyForLabel, truePositiveRate, falsePositiveRate, truePostive, trueNegative, falsePositive, falseNegative);

//        sb.append("" + label + " Results\n");
//        sb.append("Total Accuracy: " + totalAccuracy + " Label Accuracy: " + accuracyForLabel + "\n");
//        sb.append("TPR: " + truePositiveRate + " FPR: " + falsePositiveRate + "\n");
//        sb.append("TP: " + truePostive + " TN: " + trueNegative + " FP: " + falsePositive + " FN: " + falseNegative);

//        return sb.toString();
    }
}

//            log("Expected input: " + pair.getIdeal() + " actual " + output);
//            System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
//                    + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));