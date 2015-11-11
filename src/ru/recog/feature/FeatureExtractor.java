package ru.recog.feature;

import java.util.List;

import org.opencv.core.Mat;

public abstract class FeatureExtractor<T> {
	
	private int dimension;
	
	public abstract List<Double> extract(T data);

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

}
