package ru.recog.feature;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

public class BinaryPixelFeatureExtractor extends FeatureExtractor<Mat> {
	
	private int width;
	private int height;
	
	public BinaryPixelFeatureExtractor(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getDimension() {
		return width*height;
	}
	@Override
	public List<Double> extract(Mat m) {
		
		List<Double> pixels = new ArrayList<Double>();
		for (int row=0; row < m.rows(); row++)
			for (int col=0; col < m.cols(); col++)
				pixels.add(m.get(row, col)[0]>0? 1.0 : 0.0 );
		return pixels;
	}
	
	@Override
	public String toString() {
		return "BinaryPixel(".concat(String.valueOf(getDimension())).concat(")");
	}

}
