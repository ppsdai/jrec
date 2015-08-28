package ru.recog.feature;

import java.awt.Point;
import java.util.*;

import org.opencv.core.Mat;

public class GravityCenterFeatureExtractor extends FeatureExtractor {

	
	public GravityCenterFeatureExtractor() {
		setDimension(4);	
	}
	
	
	@Override
	public List<Double> extract(Mat m) {
		//for every point
		// if it's not zero 
		// add to x and y sum
		// increase point counter
		int counter = 0;
		List<Point> nonZeroPoints = new ArrayList<Point>();
		double sumX = 0; double sumY = 0;
		for (int row = 0; row < m.rows(); row++)
			for (int col = 0; col< m.cols(); col++) 
				if ( m.get(row, col)[0] > 0) {
					counter++;
					sumX += col;
					sumY += row;
					nonZeroPoints.add(new Point(col,row));
				}
		double cx = -1;
		double cy = -1;
		if (counter > 0) {
			cx = sumX/counter/m.cols();
			cy = sumY/counter/m.rows();
		}
		int gravCenterX = (int) Math.floor(sumX/counter);
		int gravCenterY = (int) Math.floor(sumY/counter);
		
		long a = 0; long b = 0; long c = 0;
		for (int i = 0; i < nonZeroPoints.size(); i ++) {
			int xi = nonZeroPoints.get(i).x-gravCenterX;
			int yi = nonZeroPoints.get(i).y-gravCenterY;
			a += xi * xi;
			b += 2 * xi * yi;
			c += yi * yi;
		}
		
		double alpha = 0.5 * Math.atan(b/(a-c));
		double I = 0.5*(a+c) - 0.5*(a-c)*Math.cos(2*alpha) - 0.5*b*Math.sin(2*alpha);
		I = I / nonZeroPoints.size();
		double d2Idalpha2 = 2*(a-c)*Math.cos(2*alpha) + 2*b*Math.sin(2*alpha);
		double theta = d2Idalpha2 > 0? alpha : alpha + Math.PI/2;

		return Arrays.asList(new Double[] {cx, cy, I, theta/(Math.PI/2)});
	}
	
	
	@Override
	public String toString() {
		return "GravityCenter(2)";
	}

}
