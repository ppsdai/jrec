package ru.recog.feature;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import ru.recog.ImageUtils;

public class SymmetryFeatureExtractor extends FeatureExtractor {
	
	public SymmetryFeatureExtractor() {
		setDimension(2);
	}

	@Override
	public List<Double> extract(Mat m) {
		Mat x = new Mat(m.rows(), m.cols(), m.type());
		Mat y = new Mat(m.rows(), m.cols(), m.type());

		Mat unionx = new Mat(m.rows(), m.cols(), m.type());
		Mat uniony = new Mat(m.rows(), m.cols(), m.type());
		
		Core.flip(m, x, 0);
		Core.flip(m, y, 1);
		Core.bitwise_or(m, x, unionx);
		Core.bitwise_or(m, y, uniony);

		
		List<Double> pair = new ArrayList<Double>();
		pair.add(ImageUtils.binaryAreaNorm(m)/ImageUtils.binaryAreaNorm(unionx));
		pair.add(ImageUtils.binaryAreaNorm(m)/ImageUtils.binaryAreaNorm(uniony));

		return pair;
	}

}
