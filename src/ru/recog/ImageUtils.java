package ru.recog;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.imgproc.CompoundImageProcessor;
import ru.recog.imgproc.ImageProcessor;

public class ImageUtils {

	public static Mat crop(Mat m) {
	//		Mat cropped = new Mat(m.size(), CvType.CV_8UC1);
			int top =-1, left =-1, right = -1, bottom = -1; 
			for (int col = 0; col<m.cols() && left < 0; col++) {
				for (int row = 0; row < m.rows()&& left < 0; row++) {
					if (m.get(row, col)[0] > 0 )
						left = col;
				}
			}
			for (int col = m.cols()-1; col>=0 && right < 0; col--) {
				for (int row = 0; row < m.rows() && right < 0; row++) {
					if (m.get(row, col)[0] > 0 )
						right = col;
				}
			}
			for (int row = 0; row < m.rows() && top < 0; row ++)
				for (int col = 0; col < m.cols() && top < 0; col++)
					if (m.get(row, col)[0] > 0)
						top = row;
			
			for (int row = m.rows()-1; row >= 0 && bottom < 0; row --)
				for (int col = 0; col < m.cols() && bottom < 0; col++)
					if (m.get(row, col)[0] > 0)
						bottom = row;
			
			System.out.println("top "+top+" bottom "+bottom+" left "+left+" right "+right);
			Mat cropped;
			if (top < 0 || bottom < 0 || left < 0 || right < 0) {
				System.err.println("top "+top+" bottom "+bottom+" left "+left+" right "+right);
				System.err.println("Could not crop, binarization ate it all, returning original one");
				cropped = m.clone();
			} else 
				cropped = m.submat(top, bottom, left, right);
			return cropped;
	}
	
	
	public static LabelFrame showProcessStages(String filename) {
		return showProcessStages(Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE));
	}
	
	public static LabelFrame showProcessStages(Mat m) {
		LabelFrame lf = new LabelFrame("process");
		
		Mat ib = new Mat(m.size(),CvType.CV_8UC1);
		Imgproc.threshold(m, ib, 80, 255, Imgproc.THRESH_BINARY_INV);
		Mat cropped = ImageUtils.crop(ib);
		Mat linear = new Mat(new Size(10,20), CvType.CV_8UC1);
		Imgproc.resize(cropped, linear, new Size(10,20), 0, 0, Imgproc.INTER_AREA);
		Mat lb = new Mat();
		Imgproc.threshold(linear, lb, 1, 255, Imgproc.THRESH_BINARY);
		
		lf.addImage(m, "orig", 3);
		lf.addImage(ib, "bin80", 3);
		lf.addImage(cropped, "crop", 3);
		lf.addImage(linear, "resized", 3);
		lf.addImage(lb,"2nd bin",3);
		
		return lf;

	}
	
	
	public static LabelFrame showProcessStages(CompoundImageProcessor processor, int scale) {
		LabelFrame lf = new LabelFrame("processing");
		
		lf.addImage(processor.getOriginalImage(), "original", scale);
		for (ImageProcessor ip : processor.getStages())
			lf.addImage(processor.getStageResult(ip), ip.toString(), scale);
		
		return lf;
	}

	public static Mat scale(Mat m, int w, int h, double threshhold) {
			
			Mat matrix = new Mat(m.rows()*h, m.cols()*w, CvType.CV_8UC1);
	//		int[][] matrix = //new byte[5][10];
	//			new int[m.cols()*w][m.rows()*h];
			
			for (int col =0; col < m.cols(); col++)
				for (int row =0; row < m.rows(); row++) {
					int val = m.get(row, col)[0]>0? 255 : 0;
					System.out.println("val "+val);
					for (int i = 0; i < w; i++)
						for (int j =0; j< h; j++) {
							matrix.put(h*row+j, w*col+i, val);
							System.out.println("putting into row "+row+"+j "+j+" col "+col+"+i"+i+" val "+val);
	//						matrix[col+i][row+j]=val;
						}
				}
			
			System.out.println("hw "+h+" "+w);
			System.out.println("matrix "+matrix.size());
			LabelFrame lf = new LabelFrame("GG");
			lf.addImage(matrix, "gg");
			lf.pack();
			lf.setVisible(true);
	
			Mat scaled = new Mat(h, w, CvType.CV_8UC1);
			for (int col = 0; col < w; col++) 
				for (int row = 0; row < h; row++) {
					long total = 0;
					for (int i = 0; i < m.cols(); i++)
						for (int j = 0; j < m.rows(); j++)
	//						total = total + matrix[col+i][row+j];
							total = total +(int)matrix.get(m.rows()*row+j, m.cols()*col+i)[0];
					System.out.println("total "+total / (m.cols() *m.rows()*255));
					int value = total / (m.cols() *m.rows()*255) > threshhold?
							255 : 0;
					scaled.put(row, col, value);
				}
			
			return scaled;
			
	}
	
	public static Mat process10x20(Mat m) {
		Mat ib = new Mat(m.size(),CvType.CV_8UC1);
		Imgproc.threshold(m, ib, 80, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
		Mat cropped = ImageUtils.crop(ib);
		Mat linear = new Mat(new Size(5,10), CvType.CV_8UC1);
		Imgproc.resize(cropped, linear, new Size(10,20), 0, 0, Imgproc.INTER_AREA);
		Mat lb = new Mat();
		Imgproc.threshold(linear, lb, 1, 255, Imgproc.THRESH_BINARY);
		return lb;
	}
 	
	public static double binaryAreaNorm(Mat m) {
		return (double) Core.countNonZero(m)/ (m.cols()*m.rows());
	}
	
	public static Mat scaleUp(Mat m, int scaleFactor) {
		System.out.println("scaling m "+m.cols()+"x"+m.rows());
		Mat sm = new Mat(m.rows()*scaleFactor, m.cols()*scaleFactor, m.type());
		for (int row = 0; row < m.rows(); row++)
			for (int col = 0; col < m.cols(); col++) {
				double val = m.get(row, col)[0];
				for (int srow = 0; srow < scaleFactor; srow++)
					for (int scol = 0; scol < scaleFactor; scol++) {
//						System.out.println("row "+row+" col "+col+" srow "+srow+" scol"
//								+scol+" tr "+row*scaleFactor+srow+ " tc "+col*scaleFactor+scol);
						sm.put(row*scaleFactor+srow, col*scaleFactor+scol, val);
					}
			}
		
		return sm;
				
	}
	
	public static List<Point> mat2PointList(Mat m) {
		//TODO assuming 1 channel mat binary image
		List<Point> points = new ArrayList<Point>();
		for (int row = 0; row < m.rows(); row++)
			for (int col = 0; col < m.cols(); col++)
				if (m.get(row, col)[0]>0)
					points.add(new Point((float) col, (float)row ));
		return points;
	}
	
	//TODO for testing scale
	
//	Mat m44 = new Mat(4,4,CvType.CV_8UC1);
//	m44.put(0, 0, 1);
//	m44.put(0, 1, 1);
//	m44.put(0, 2, 1);
//	m44.put(0, 3, 1);
//	
//	m44.put(1, 0, 1);
//	m44.put(1, 1, 1);
//	m44.put(1, 2, 1);
//	m44.put(1, 3, 1);
//	
//	m44.put(2, 0, 0);
//	m44.put(2, 1, 0);
//	m44.put(2, 2, 0);
//	m44.put(2, 3, 0);
//	
//	m44.put(3, 0, 0);
//	m44.put(3, 1, 0);
//	m44.put(3, 2, 0);
//	m44.put(3, 3, 0);
//	
//	
//	Mat m22 = scale(m44, 2,2, 0.3);
//	
//	System.out.println(m22.get(0, 0)[0]);
//	System.out.println(m22.get(0, 1)[0]);
//	System.out.println(m22.get(1, 0)[0]);
//	System.out.println(m22.get(1, 1)[0]);

}
