package ru.recog.feature;

import java.util.Collections;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import ru.recog.ImageUtils;

public class AreaFeatureExtractor extends FeatureExtractor {

	
	public AreaFeatureExtractor() {
		setDimension(1);	
	}
	
	@Override
	public List<Double> extract(Mat m) {
		return Collections.singletonList( ImageUtils.binaryAreaNorm(m));
	}

}
