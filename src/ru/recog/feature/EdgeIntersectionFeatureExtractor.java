package ru.recog.feature;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

public class EdgeIntersectionFeatureExtractor extends FeatureExtractor<Mat> {
	
	private int horizontalPts, verticalPts;
	
	public EdgeIntersectionFeatureExtractor(int horizontalPts, int verticalPts) {
		this.horizontalPts = horizontalPts;
		this.verticalPts = verticalPts;
		setDimension(2*horizontalPts+2*verticalPts);
	}

	@Override
	public List<Double> extract(Mat m) {
		List<Double> features = new ArrayList<Double>(getDimension());
		
		float drow = m.rows()/(horizontalPts+1);
		float dcol = m.cols()/(verticalPts+1);

		for (int i = 1; i <= horizontalPts; i++) {
			int row = Math.round(i*drow);
			double frontval = 0;
			//FIXME this must be done more elegantly
			for (int col=0; col < m. cols(); col++) {
				frontval = m.get(row, col)[0];
				if (frontval > 0) {
					features.add((double)col/m.cols());
					break;
				}
			}
			if (frontval == 0) features.add(1.0);
		}
		
		for (int i = 1; i <= horizontalPts; i++) {
			int row = Math.round(i*drow);
			double backval = 0;
			//FIXME this must be done more elegantly
			for (int col=m.cols()-1; col >=0; col--) {
				backval = m.get(row, col)[0];
				if (backval > 0) {
					features.add((double)col/m.cols());
					break;
				}
			}
			if (backval == 0) features.add(0.0);
		}
		
		for (int i = 1; i <= verticalPts; i++) {
			int col = Math.round(i*dcol);
			double frontval = 0;
			//FIXME this must be done more elegantly
			for (int row = 0; row < m. rows(); row++) {
				frontval = m.get(row, col)[0];
				if (frontval > 0) {
					features.add((double)row/m.rows());
					break;
				}
			}
			if (frontval == 0) features.add(1.0);
		}
		
		for (int i = 1; i <= verticalPts; i++) {
			int col = Math.round(i*dcol);
			double backval = 0;
			//FIXME this must be done more elegantly
			for (int row=m.rows()-1; row >=0; row--) {
				backval = m.get(row, col)[0];
				if (backval > 0) {
					features.add((double)row/m.rows());
					break;
				}
			}
			if (backval == 0) features.add(0.0);
		}

		
		return features;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "EdgeIntersection(".concat(String.valueOf(getDimension())).concat(")[")
				.concat(String.valueOf(horizontalPts)).concat("x")
				.concat(String.valueOf(verticalPts)).concat("]") ;
	}

}
