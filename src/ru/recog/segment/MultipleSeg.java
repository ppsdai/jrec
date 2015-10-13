package ru.recog.segment;


import java.io.*;
import java.util.Arrays;
import java.util.Properties;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;
import ru.recog.imgproc.SegmentationResult;

/**
 *  
         Shape Based Segmenter.
         Input: Mat - which should be a plate Image.
         Output: Some shapes, which are ready for recognition.
         Segments a typical plate image into digits and symbols.
 *
 * @version      
         1.00 19 September 2015  * @author          
         Alexey Denisov  */

public class MultipleSeg {

 public static void main(String[] args) throws Exception {
	 System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
/*  Properties pros = new Properties();
  pros.setProperty("1.bmp", "5");
  pros.setProperty("2.bmp", "6");
  pros.setProperty("3.bmp", "7");
  
  pros.store(new FileOutputStream("C:\\dev\\PlatesSegmentation\\props.properties"), "");*/
  
	 
  LabelFrame lf = new LabelFrame("For Inspection", true);
	 
  // load files list with their properties	 
  Properties props = new Properties();
  props.load(new FileInputStream("C:\\dev\\PlatesSegmentation\\props.properties"));
  //props.list(System.out);
  
  
	File dir = new File("C:\\dev\\PlatesSegmentation");
	
	for ( String key: props.stringPropertyNames() ){
		String leString =  props.getProperty(key);
		int leInteger = Integer.parseInt( leString);
		
		String filename = new File(dir, key).getAbsolutePath();

		System.out.println( filename + " " + leInteger );
		
		Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		Mat b = ImageUtils.localbin(m.clone(), 0.6);  //0.6
		
		int median = m.rows() / 2;
		SegData trySegment = new SegData(m);
		trySegment.setUpperBound(median - leInteger);
		trySegment.setLowerBound(median + leInteger);
		trySegment.calculateProjection();
		trySegment.calculateLocalMaximums();
		trySegment.calculateLocalMinimums();
		
		lf.addImage(m1, key, 5);
		lf.addImage( makeHistogram(m, trySegment, 5 ) ,"hist", 1);
	}
	
	lf.pack();
	lf.setVisible(true);
 }	
	

    /**  
    method for which this class was created *
    returns a final list for recognition    *
    plImg - plate Image should be grayScale */
	public static Mat makeHistogram(Mat m, SegData segs, int magnification){
		

		int[] projX = segs.getProjection();
		
		Mat hist = Mat.zeros(512, magnification*m.cols(), CvType.CV_8UC3); //512
				
		int minV, maxV;
		maxV = projX[0]; minV = projX[0];
		for (int col=0; col < projX.length ; col ++)
		{
			if (projX[col] > maxV) maxV = projX[col];
			if (projX[col] < minV) minV = projX[col];
		}
		
		// find Dx, Dy to be used for plotting
		int dX;
		float dY;
		dX = (int) Math.floor(hist.cols() / m.cols());
		dY = ((float) hist.rows()) / ( maxV - minV );
		
		// plot histogram
		for (int i = 1; i < m.cols(); i++)
		Imgproc.line( hist, new Point(dX*(i-1), hist.rows() - Math.round(dY*(projX[i-1] - minV))),
		 new Point(dX*i, hist.rows() - Math.round(dY*(projX[i] - minV))), new Scalar(0, 255, 0) ); 

		// add minimums
		for (int p : segs.getLocalMinimums())
			Imgproc.line(hist, new Point(magnification*p, 0), new Point(magnification*p, hist.rows()-1), new Scalar(0,0,255));
		
		//add maximums
		for (int p : segs.getLocalMaximums())
			Imgproc.line(hist, new Point(magnification*p, 0), new Point(magnification*p, hist.rows()-1), new Scalar(255,0,0));

		
		return hist;
	}
	
 }

