package ru.recog.feature;

import java.util.List;

import org.opencv.core.Mat;

public abstract class FeatureExtractor {
	
	private int dimension;
	
	public abstract List<Double> extract(Mat m);

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

}
