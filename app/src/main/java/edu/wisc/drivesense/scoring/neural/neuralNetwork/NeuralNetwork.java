package edu.wisc.drivesense.scoring.neural.neuralNetwork;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

/**
 * Created by Damouse on 12/9/2014.
 * <p/>
 * Implementation of DS Neural Network. This class is the public interface of the neural network, but doesn't actually
 * implement NN functionality-- see class below for. that.
 */
public class NeuralNetwork {
    public NeuralNetworkImplementation network;

    /**
     * Initialize with a saved neural network- provide path to the saved network
     */
    public NeuralNetwork(String path) {

    }

    /**
     * Create a new network with the given number of nodes.
     */
    public NeuralNetwork(int inputNodes, int hiddenNodes) {
        network = new NeuralNetworkImplementation();

        network.addLayer(new BasicLayer(null, true, inputNodes));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, hiddenNodes));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 3));

        network.getStructure().finalizeStructure();
        network.reset();
    }


    /* Input */

    /**
     * Feed an input term into the network and return the output.
     */
    public MLData evaluate(MLData input) {
        MLData output = network.compute(input);

        for (int i = 0; i < output.size(); i++) {
            double rounded = Math.round(output.getData(i));
            output.setData(i, rounded);
        }

        return output;
    }
}

class NeuralNetworkImplementation extends BasicNetwork {

}
