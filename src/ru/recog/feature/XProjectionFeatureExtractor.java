package ru.recog.feature;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class XProjectionFeatureExtractor extends FeatureExtractor<Mat> {
	
	public XProjectionFeatureExtractor() {
		//FIXME	dimension depends on picture size
		setDimension(10);
	}

	@Override
	public List<Double> extract(Mat m) {
		if (m.cols() != getDimension()) 
			throw new IllegalArgumentException("Expecting picture width "+getDimension()+" and got "+m.cols()); 
		List<Double> projections = new ArrayList<Double>();
		for (int col = 0; col < m.cols(); col++ ) 
			projections.add( (double) Core.countNonZero(m.col(col)) / m.rows());
		return projections;
	}
	
	@Override
	public String toString() {
		return "XProjection(".concat(String.valueOf(getDimension())).concat(")");

	}

}
