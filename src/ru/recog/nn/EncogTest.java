package ru.recog.nn;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;

public class EncogTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NeuralNetwork neuroph = NeuralNetwork.createFromFile("/Users/pps/dev/TheBest60.nnet");
		
		
		System.out.println(neuroph);
		System.out.println(neuroph.getNetworkType());
		System.out.println(neuroph.getLearningRule());
		for (Layer layer : neuroph.getLayers())
			System.out.println(layer.getNeuronsCount());
		
	/*	
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, 33));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 75));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 21));
		
		network.getStructure().finalizeStructure();
		network.reset();
		
		MLDataSet trainingSet = new CSVNeuralDataSet("/Users/pps/dev/NNTrain/full1020/trainencog.txt", 33, 21, false); 
		MLTrain train = new Backpropagation(network, trainingSet, 0.7, 0.3);
		
		MLDataSet testSet =  new CSVNeuralDataSet("/Users/pps/dev/NNTrain/full1020/testencog.txt", 33, 21, false); 

		
		
		System.out.println(network);
		System.out.println(trainingSet.size());
		
		int epoch = 1;
		do {
			train.iteration();
			System.out.println("Epoch #"+epoch+" Error: "+train.getError());
			epoch++;
		} while (train.getError() > 0.01 && epoch < 200);
		
		for (MLDataPair data : testSet) {
			MLData output = network.compute(data.getInput());
			System.out.println(data.getInput());
			System.out.println(data.getIdeal());
			System.out.println(output);

		}
		*/




		


	}

}
