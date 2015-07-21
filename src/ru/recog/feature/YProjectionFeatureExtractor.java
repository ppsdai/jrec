package ru.recog.feature;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class YProjectionFeatureExtractor extends FeatureExtractor {

	public YProjectionFeatureExtractor() {
		//FIXME	dimension depends on picture size
		setDimension(20);
	}

	@Override
	public List<Double> extract(Mat m) {
		if (m.rows() != getDimension()) 
			throw new IllegalArgumentException("Expecting picture height "+getDimension()+" and got "+m.rows()); 
		List<Double> projections = new ArrayList<Double>();
		for (int row = 0; row < m.rows(); row++ ) 
			projections.add( (double) Core.countNonZero(m.row(row)) / m.cols());
		return projections;
	}
	
	@Override
	public String toString() {
		return "YProjection(".concat(String.valueOf(getDimension())).concat(")");
	}

}
