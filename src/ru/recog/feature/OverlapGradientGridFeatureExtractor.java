package ru.recog.feature;

import java.io.PrintStream;
import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

import org.opencv.core.*;

import ru.recog.LabelFrame;


@XmlRootElement (name = "OverlapGradient")
public class OverlapGradientGridFeatureExtractor extends GradientGridFeatureExtractor {
	
	public OverlapGradientGridFeatureExtractor() {
		width = 4;
		height = 7;
		setDimension((width-1)*(height-1)*4);
	}

	@Override
	public List<Double> extract(Mat m) {
		grid = fillGrid(m);
		
		List<Double> features = new ArrayList<Double>(getDimension());
		
		for (int i = 0; i < width-1; i++)
			for (int j = 0; j < height-1; j++) 
				for (double d : gradientAtPoint(i,j))
					features.add(d);
				
		return features;
	}
	
	@Override
	public String toString() {
		return "Overlap".concat(super.toString());
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

		OverlapGradientGridFeatureExtractor ggfe = new OverlapGradientGridFeatureExtractor();
//		Mat m = new Mat(20,10, CvType.CV_8UC1);
//		m.
		Mat ones = Mat.ones(21, 11, CvType.CV_8UC1);
		
		double[][] g = ggfe.fillGrid(ones);
		printDoubleArr(g, System.out);
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
		g = ggfe.fillGrid(m1);
		lf1.addImage(doubleArr2Mat(g), "", 20);
		
		
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
