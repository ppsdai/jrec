package ru.recog.feature;

import java.util.Collections;
import java.util.List;

import org.opencv.core.Mat;

public class TruthFeatureExtractor extends FeatureExtractor<Mat> {

	@Override
	public List<Double> extract(Mat m) {
		return Collections.emptyList();
	}

}
