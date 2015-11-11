package ru.recog.feature;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;

public class GravityGridFeatureExtractor extends FeatureExtractor<Mat> {

	private double x0 = -1, y0 = -1;
	private int lastX = -1, lastY = -1;
	
	private int gridW,gridH;
	
	private Mat img;
	
	private double dx, dy;
	
	//FIXME handle empty or completely black data properly
	
	
	
	public GravityGridFeatureExtractor(int width, int height) {
		//FIXME these must be even for now
		gridW = width;
		gridH = height;
		setDimension(gridW+1+gridH+1+2);
	}
	
	@Override
	public List<Double> extract(Mat m) {
		img = m.clone();
		
		findGravityCenter();
		
		dx = (x0>=lastX/2)? 2*x0/gridW : 2*(lastX-x0)/gridW;
//		System.out.println("dx= "+dx);
		dy = (y0>=lastY/2)? 2*y0/gridH : 2*(lastY-y0)/gridH;
//		System.out.println("dx= "+dx);
		
		List<Double> features = new ArrayList<Double>();
		
		features.add(x0/lastX);
		features.add(y0/lastY);
		
		features.add(intensityFCX(x0));
		for (int col = 1; col <=gridW/2; col++) {
			double x1 = x0 - dx * col;
			double x2 = x0 + dx * col;
			features.add(intensityFCX(x1));
			features.add(intensityFCX(x2));
		}
		
		features.add(intensityFCY(y0));
		for (int row = 1; row <=gridH/2; row++) {
			double y1 = y0 - dy * row;
			double y2 = y0 + dy * row;
			features.add(intensityFCY(y1));
			features.add(intensityFCY(y2));
		}
		
		return features;
		
	}
	
	
	private void findGravityCenter() {
		//for every point
		// if it's not zero 
		// add to x and y sum
		// increase point counter
		int counter = 0;
		double sumX = 0; double sumY = 0;
		for (int row = 0; row < img.rows(); row++)
			for (int col = 0; col< img.cols(); col++) 
				if ( img.get(row, col)[0] > 0) {
					counter++;
					sumX += col;
					sumY += row;
				}
		lastX = img.cols()-1;
		lastY = img.rows()-1;
		
		
		if (counter > 0) {
			x0 = sumX/counter;
			y0 = sumY/counter;
		}
//		System.out.println("Center of gravity: "+x0+", "+y0);
	}
	
	private double intensityFCX(double x) {
		int x1 = (int) Math.floor(x);
		int x2 = (int) Math.ceil(x);
		if (x0 >= 0.5 * lastX && (Math.min(x1, x2)<0))
			return Ix(0);
		else if (x0 <= 0.5 * lastX && Math.max(x1, x2)>lastX)
			return Ix(lastX);
		else
			return 0.5 * (Ix(x1) + Ix(x2));
	}
	
	private double intensityFCY(double y) {
		int y1 = (int) Math.floor(y);
		int y2 = (int) Math.ceil(y);
		if (y0 >= 0.5 * lastY && (Math.min(y1, y2)<0))
			return Iy(0);
		else if (y0 <= 0.5 * lastY && Math.max(y1, y2)>lastY)
			return Iy(lastY);
		else
			return 0.5 * (Iy(y1) + Iy(y2));
	}
	
	private double Ix(int col) {
		if (col < 0 || col > lastX) 
			return 0;
		else
			return (double) Core.countNonZero(img.col(col)) / img.rows();
	}
	
	private double Iy(int row) {
		if (row < 0 || row > lastY) 
			return 0;
		else
			return (double) Core.countNonZero(img.row(row)) / img.cols();
	}
	
	@Override
	public String toString() {
		return "GravityGrid[".concat(String.valueOf(gridW)).concat("x")
				.concat(String.valueOf(gridH)).concat("]");
	}
	
	
	public static void main(String[] args) {
		System.out.println(Math.floor(-0.1));
		System.out.println(Math.ceil(-0.1));
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		GravityGridFeatureExtractor ggfe = new GravityGridFeatureExtractor(10,20);
//		Mat m = new Mat(20,10, CvType.CV_8UC1);
//		m.
		Mat ones = Mat.ones(21, 11, CvType.CV_8UC1);
		System.out.println(ggfe.extract(ones));
		
		Mat m4x4 = Mat.zeros(4, 4, CvType.CV_8UC1);
		m4x4.put(1, 1, 1);
		m4x4.put(1, 2, 1);
		m4x4.put(2, 1, 1);
		m4x4.put(2, 2, 1);
		System.out.println("Center:"+ggfe.extract(m4x4));
		
		Mat m4x4left = Mat.zeros(4, 4, CvType.CV_8UC1);
		m4x4left.put(1, 0, 1);
		m4x4left.put(1, 1, 1);
		m4x4left.put(2, 0, 1);
		m4x4left.put(2, 1, 1);
		System.out.println("Left: "+ggfe.extract(m4x4left));
		
		Mat m4x4right = Mat.zeros(4, 4, CvType.CV_8UC1);
		m4x4right.put(1, 2, 1);
		m4x4right.put(1, 3, 1);
		m4x4right.put(2, 2, 1);
		m4x4right.put(2, 3, 1);
		System.out.println("Right: "+ggfe.extract(m4x4right));


		Mat m4x4emptyC= Mat.ones(4, 4, CvType.CV_8UC1);
		m4x4emptyC.put(1, 1, 0);
		m4x4emptyC.put(1, 2, 0);
		m4x4emptyC.put(2, 1, 0);
		m4x4emptyC.put(2, 2, 0);
		System.out.println("EmptyC: "+ggfe.extract(m4x4emptyC));

		
	}

}
