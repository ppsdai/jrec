package ru.recog.feature;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;

import ru.recog.LabelFrame;

public class GradientGridFeatureExtractor extends FeatureExtractor {
	
	private int width;
	private int height;
	
	public GradientGridFeatureExtractor() {
		width = 4;
		height = 7;
		setDimension((width-1)*(height-1)*4);
	}

	@Override
	public List<Double> extract(Mat m) {
		double[][] grid = fillGrid(m);
		
		List<Double> features = new ArrayList<Double>(getDimension());
		
		for (int i = 0; i < width-1; i++)
			for (int j = 0; j < height-1; j++) {
				double dx1 = grid[i][j] - grid[i+1][j];
				double dx2 = grid[i][j+1] - grid[i+1][j+1];
				double dy1 = grid[i][j] - grid[i][j+1];
				double dy2 = grid[i+1][j] - grid[i+1][j+1];
				
				double dx = dx1+dx2; double adx = Math.abs(dx1)+Math.abs(dx2);
				double dy = dy1+dy2; double ady = Math.abs(dy1)+Math.abs(dy2);
				
				double r = Math.sqrt(dx*dx + dy*dy + adx*adx + ady*ady);
				
				features.add(r == 0? 0 : dx/r);
				features.add(r == 0? 0 : dy/r);
				features.add(r == 0? 0 : adx/r);
				features.add(r == 0? 0 : ady/r);
			}
		
		return features;
	}
	
	private double[][] fillGrid(Mat m) {
		double[][] grid = new double[width][height];
		
		int mw = m.cols();
		int mh = m.rows();
		double[][] fusion = new double[width*mw][height*mh];
		for (int row = 0; row < mh; row++)
			for (int col = 0; col < mw; col++) {
				//fill out square of width x height with particular value
				double val = m.get(row, col)[0];
				for (int i = 0; i < width; i++ )
					for (int j = 0; j < height; j++) {
						fusion[col*width+i][row*height+j] = val;
					}
			}
		
		for (int i = 0; i < width; i++) 
			for (int j = 0; j < height; j++) {
				double sum = 0;
				for (int ii = 0; ii < mw; ii++)
					for (int jj = 0; jj < mh; jj++) 
						sum = sum + fusion[i*mw+ii][j*mh+jj];
				grid[i][j] = sum/(mw*mh);
			}
		
		return grid;
	}
	
	public static void printDoubleArr(double[][] arr, PrintStream ps) {
		for (int j = 0; j < arr[0].length; j++) {
			for (int i = 0; i < arr.length; i++) {
				ps.print(arr[i][j]+" ");
			}
			ps.println();
		}
		
	}
	
	private static Mat doubleArr2Mat(double[][] arr) {
		Mat m = new Mat(7, 4, CvType.CV_8UC1);
		for (int row = 0; row < m.rows(); row++) 
			for (int col = 0; col < m.cols(); col++)
				m.put(row, col, arr[col][row]);
		return m;
	}
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		GradientGridFeatureExtractor ggfe = new GradientGridFeatureExtractor();
//		Mat m = new Mat(20,10, CvType.CV_8UC1);
//		m.
		Mat ones = Mat.ones(21, 11, CvType.CV_8UC1);
		
		double[][] grid = ggfe.fillGrid(ones);
		printDoubleArr(grid, System.out);
		List<Double> features = ggfe.extract(ones);
		System.out.println(features.size());
		System.out.println(features);
		
//		Mat m1 = Mat.zeros(28, 16, CvType.CV_8UC1);
//		
//		int k = 14;
//		for (int i = 0; i < 0.5*m1.rows()/k; i++) {
//			for (int j = 0; j<k; j++) {
//				int row = i*2*k+j;
//				for (int col = 0; col < m1.cols(); col++)
//					m1.put(row, col, 255);
//			}
//		}
//		
//		LabelFrame lf = new LabelFrame();
//		lf.addImage(m1, String.valueOf(k), 5);
//		
//		LabelFrame lf1 = new LabelFrame();
//		grid = ggfe.fillGrid(m1);
//		lf1.addImage(doubleArr2Mat(grid), "", 20);
		
		
		Mat m1 = Mat.zeros(28, 16, CvType.CV_8UC1);
		
		int k = 8;
		for (int i = 0; i < 0.5*m1.cols()/k; i++) {
			for (int j = 0; j<k; j++) {
				int col = i*2*k+j;
				for (int row = 0; row < m1.rows(); row++)
					m1.put(row, col, 255);
			}
		}
		
		LabelFrame lf = new LabelFrame();
		lf.addImage(m1, String.valueOf(k), 5);
		
		LabelFrame lf1 = new LabelFrame();
		grid = ggfe.fillGrid(m1);
		lf1.addImage(doubleArr2Mat(grid), "", 20);
		
		
//		lf1.addImage()
		
		lf.pack();
		lf.setVisible(true);
		
		lf1.pack();
		lf1.setVisible(true);
		
		
	/*	System.out.println(ggfe.fillGrid(ones));
		
		
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

		*/
	}

}
