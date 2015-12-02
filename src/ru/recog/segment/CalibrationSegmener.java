package ru.recog.segment;


import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;

public class CalibrationSegmener {

	
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
	  props.load(new FileInputStream("C:\\dev\\PlatesSegmentation\\props3.properties"));
	  props.list(System.out);
	  
	  
		File dir = new File("C:\\dev\\PlatesSegmentation");
		
		for ( String key: props.stringPropertyNames() ){
			String leString =  props.getProperty(key);
			int heInteger = Integer.parseInt( leString); // Height Estimate Integer
			
			String filename = new File(dir, key).getAbsolutePath();

			System.out.println( filename + " " + heInteger );
			
			Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			Mat b = ImageUtils.localbin(m.clone(), 0.6);  //0.6
			
			
			int median = m.rows() / 2;
			SegData trySegment = new SegData(m, median - 7, median + 7 );
			
			// first method looking for starting point
			int sPoint1 = trySegment.findStartPoint(0, 0.4);
			int xCoord1 = trySegment.getLocalMinimums().get(sPoint1);
			int xCoord2 = trySegment.getLocalMinimums().get(sPoint1 + 1);
			Imgproc.line(m1, new Point(xCoord1, 0), new Point(xCoord1, m1.rows()-1), new Scalar(0,255,0));
			Imgproc.line(m1, new Point(xCoord2, 0), new Point(xCoord2, m1.rows()-1), new Scalar(125,0,0));
			//second looking for starting point
			sPoint1 = trySegment.findStartPoint(1, 0.5);
			xCoord1 = trySegment.getLocalMinimums().get(sPoint1);	
			Imgproc.line(m1, new Point(xCoord1, 0), new Point(xCoord1, m1.rows()-1), new Scalar(0,0,125));
			
			// make sobel projectrion
			trySegment.calculateSobelXProjectonY();
			trySegment.calculateUpperAndLowerBoundary( heInteger);
			// add line bounds
			Imgproc.line(m1, new Point(0, trySegment.getUpperBound()), 
					         new Point(m1.cols()-1, trySegment.getUpperBound()), new Scalar(0,255,0));
			Imgproc.line(m1, new Point(0, trySegment.getLowerBound()), 
			                 new Point(m1.cols()-1, trySegment.getLowerBound()), new Scalar(0,255,0));	
			
			lf.addImage(m1, key, 8);
			
			//rotated image
			{
				Mat rot = rotation( m, -5);
				SegData trySegment2 = new SegData(rot, median - 7, median + 7 );
				trySegment2.calculateSobelXProjectonY();
				trySegment2.calculateUpperAndLowerBoundary( heInteger);	
				// add line bounds
				Imgproc.line(rot, new Point(0, trySegment2.getUpperBound()), 
						         new Point(rot.cols()-1, trySegment2.getUpperBound()), new Scalar(0,255,0));
				Imgproc.line(rot, new Point(0, trySegment2.getLowerBound()), 
				                 new Point(rot.cols()-1, trySegment2.getLowerBound()), new Scalar(0,255,0));			
				
				lf.addImage(rot, "rotated by 2 degrees", 8);
			}
			
			lf.addImage( makeSobelHistogram(m, trySegment, 8 ) ,"hist", 1);
			
			
			
		}
		
		lf.pack();
		lf.setVisible(true);
	 }		
	
	 

	    /**  
	    method for which this class was created *
	    returns a final list for recognition    *
	    plImg - plate Image should be grayScale */
		public static Mat makeSobelHistogram(Mat m, SegData segs, int magnification){
			

			double[] projY = segs.getSobelXProjection();
			
			Mat hist = Mat.zeros(512, magnification*m.cols(), CvType.CV_8UC3); //512
					
			double minV, maxV;
			maxV = projY[0]; minV = projY[0];
			for (int col=0; col < projY.length ; col ++)
			{
				if (projY[col] > maxV) maxV = projY[col];
				if (projY[col] < minV) minV = projY[col];
			}
			
			// find Dx, Dy to be used for plotting
			int dX;
			double dY;
			dX = (int) Math.floor(hist.cols() / m.cols());
			dY = ((double) hist.rows()) / ( maxV - minV );
			
			// plot histogram
			for (int i = 1; i < m.rows(); i++)
			Imgproc.line( hist, new Point(dX*(i-1), hist.rows() - Math.round(dY*(projY[i-1] - minV))),
			 new Point(dX*i, hist.rows() - Math.round(dY*(projY[i] - minV))), new Scalar(0, 255, 0) ); 
			
			// add lines for boundaries
			Imgproc.line( hist, new Point(dX*( segs.getUpperBound() ), 0 ),
					 new Point(dX*( segs.getUpperBound() ), hist.rows()), new Scalar(255, 0, 0) );
			Imgproc.line( hist, new Point(dX*( segs.getLowerBound() ), 0 ),
					 new Point(dX*( segs.getLowerBound() ), hist.rows()), new Scalar(255, 0, 0) );

			
			return hist;
		}
		
	    /**  
	    a stub on rotation */
		public static Mat rotation(Mat m, double alfa){
			Mat dst = Mat.zeros(m.size(), m.type());
			Point center = new Point(m.cols()/2, m.rows()/2);   
			Mat rotImage = Imgproc.getRotationMatrix2D(center, alfa, 1.0);
			Imgproc.warpAffine(m, dst, rotImage, m.size());
			return dst;
			
		}
}
