package ru.recog.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Mat;

public class MultipleFeatureExtractor extends FeatureExtractor {
	
	List<FeatureExtractor> featureList = new ArrayList<FeatureExtractor>();

	@Override
	public List<Double> extract(Mat m) {
		List<Double> vals = new ArrayList<Double>();
		for (FeatureExtractor fe : featureList) 
			vals.addAll(fe.extract(m));
		return vals;
	}
	
	public List<FeatureExtractor> getFeatureExtractors() {
		return featureList;
	}
	
	public void addExtractor(FeatureExtractor fe) {
		featureList.add(fe);
	}
	
	public int getDimension() {
		int dim = 0;
		for (FeatureExtractor fe : featureList) 
			dim = dim + fe.getDimension();
		return dim;
	}
	
	public void setDimension(int dimension) {
		throw new IllegalArgumentException("Cannot set dimension on MultipleFeatureExtractor");
	}

}
