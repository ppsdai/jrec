package ru.recog.feature;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

public class TileGradientFeatureExtractor extends GradientGridFeatureExtractor {
	
	public TileGradientFeatureExtractor() {
		width = 8;
		height = 14;
		setDimension(4*7);
	}

	@Override
	public List<Double> extract(Mat m) {
		grid = fillGrid(m);
		
		List<Double> features = new ArrayList<Double>(getDimension());
		
		for (int i = 0; i < width; i=i+2)
			for (int j = 0; j < height; j=j+2) 
				for (double d : gradientAtPoint(i,j))
					features.add(d);
		
		return features;
	}
	
	@Override
	public String toString() {
		return "Tile".concat(super.toString());
	}
	

}
