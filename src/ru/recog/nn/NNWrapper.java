package ru.recog.nn;

import java.util.*;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.opencv.core.Mat;

import ru.recog.feature.*;

public class NNWrapper {
	
	MultipleFeatureExtractor mfx;
	MultiLayerPerceptron nn;
	
	
	String pathToNN;
	
	public String getPathToNN() {
		return pathToNN;
	}
	
	public MultipleFeatureExtractor getFeatureExtractor() {
		return mfx;
	}
//
	public void setPathToNN(String pathToNN) {
		this.pathToNN = pathToNN;
		nn = (MultiLayerPerceptron)NeuralNetwork.createFromFile(pathToNN);
	}

	
	public NNWrapper(String nnPath) {
		setPathToNN(nnPath);
		
		System.out.println(nn.getNetworkType());
		System.out.println(nn.getLearningRule());
		for (Layer layer : nn.getLayers()) {
			System.out.println(layer.getNeuronsCount());
		}
		System.out.println(Arrays.toString(nn.getWeights()));

		List<FeatureExtractor> fexlist = new ArrayList<FeatureExtractor>();
		fexlist.add(new AreaFeatureExtractor());
//		fexlist.add(new EllipseFeatureExtractor());
//		fexlist.add(new XProjectionFeatureExtractor());
//		fexlist.add(new YProjectionFeatureExtractor());
		fexlist.add(new GravityGridFeatureExtractor(10, 20));
		fexlist.add(new SymmetryFeatureExtractor());
		mfx = new MultipleFeatureExtractor(fexlist);
	}
	
	public NNWrapper(String nnPath, MultipleFeatureExtractor mfx) {
		nn = (MultiLayerPerceptron)NeuralNetwork.createFromFile(nnPath);
		
//		System.out.println(nn.getNetworkType());
//		System.out.println(nn.getLearningRule());
//		for (Layer layer : nn.getLayers()) {
//			System.out.println(layer.getNeuronsCount());
//		}
//		System.out.println(Arrays.toString(nn.getWeights()));
		
		this.mfx = mfx;

	}
	
	public NeuralNetwork<?> getNN() {
		return nn;
	}
	
	public List<Double> getNNOutput(Mat m) {
		List<Double> features = mfx.extract(m);
		System.out.println(features);
		double[] f = new double[features.size()];
		for (int i = 0; i < features.size(); i++)
			f[i] = features.get(i);
		nn.setInput(f);
		nn.calculate();
		double[] r = nn.getOutput();
		List<Double>  rl = new ArrayList<Double>(r.length);
		for (double d : r) rl.add(d);
		return rl;
		
	}
	
	public static String description(NeuralNetwork<?> nn) {
		StringBuilder sb = new StringBuilder();
		sb.append(nn.getNetworkType()).append("[").append(nn.getLearningRule().toString()).append("]").append(" Layers: ");
		for (Layer l : nn.getLayers())
			sb.append(l.getNeuronsCount()).append(" ");
		return sb.toString();
	}
	
	public double[] getNNOutputArray(Mat m) {
		List<Double> features = mfx.extract(m);
//		System.out.println(features);
		double[] f = new double[features.size()];
		for (int i = 0; i < features.size(); i++)
			f[i] = features.get(i);
		nn.setInput(f);
		nn.calculate();
		return nn.getOutput();
		
	}
	
	public static String nnOutputToSymbol(double[] nnoutput) {
		StringBuffer sb = new StringBuffer("");
		int found = 0;
		char ch = 0;
		for (int i = 0; i < nnoutput.length; i ++) {
			if (nnoutput[i]> 0.9) {
				found++;
				ch = NNAnalysis.getChar(i);
			}
		}
		if (found == 1 )
			sb.append(ch);
		else sb.append("*");
		
		return sb.toString();
	}
	
	public static double probability(double[] nnoutput) {
		double sum = 0;
		double max = -1.0;
		for (double d : nnoutput) {
			sum+=d;
			if (d > max) max = d;
		}
		
		if (sum > 1) return max/sum;
		else return max;
	}
	
	public List<Double> probList(List<Mat> pieces) {
		List<Double> probs = new ArrayList<>();
		for (Mat piece : pieces)
			probs.add(NNWrapper.probability(getNNOutputArray(piece)));
		return probs;
	}
	
	public static String nnOutputToSymbols(double[] nnoutput, double epsilon) {
		StringBuffer sb = new StringBuffer("[");
		int found = 0;
		char ch = 0;
		for (int i = 0; i < nnoutput.length; i ++) {
			if (nnoutput[i]> epsilon) {
				found++;
				ch = NNAnalysis.getChar(i);
			}
		}
		if (found == 1 )
			sb.append(ch);
		else sb.append("*");
		
		return sb.toString();
	}
	
	public String getLPString(List<Mat> pieces) {
		StringBuilder sb = new StringBuilder();
		
		for (Mat piece : pieces) 
			sb.append(nnOutputToSymbol(getNNOutputArray(piece.clone())));
		
		return sb.toString();
	}
	
	public String getBracketedLPString(List<Mat> pieces) {
		StringBuilder sb = new StringBuilder();
		
		for (Mat piece : pieces) 
			sb.append("[").append(nnOutputToSymbol(getNNOutputArray(piece.clone()))).append("]");
			
		
		return sb.toString();
	}
	
//	public List<Double> getNNOutput(Mat m) {
//		return Arrays.asList(getNNOutput(m));
//	}
	


}
