package ru.recog.segment;


import java.io.File;
import java.util.Arrays;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ru.recog.XML;

public class CalibrationSegmenter {

	
	private CalibrationLine calLine1; 
	private CalibrationLine calLine2;
	
	// temporary variables
	private double[] sobelXprojY;
	private Mat originalM;
	private int upperBound, lowerBound; //upper is smaller, as axis Y points downward
	
	 /**  
    constructor takes two lines with calibrated *
    positions */
	public CalibrationSegmenter(String XML_FileName1, String XML_FileName2){
		
		calLine1 = (CalibrationLine) XML.fromXML(new File(XML_FileName1));
		calLine2 = (CalibrationLine) XML.fromXML(new File(XML_FileName2));
		
	}

	/**  
    takes filename like V1411N4t16960X637Y165.png and matrix*
    returns segmentationData with rotated matrix *
    and new upper and lower boundaries */
	public SegmentationData calculateSegmentationData( File f, Mat m){
		 //System.out.println(f.getName());
	  int indexX = f.getName().lastIndexOf('X');
	  int indexY = f.getName().lastIndexOf('Y');
	  int indexP = f.getName().lastIndexOf('.');
		 
	  double X = Double.parseDouble(f.getName().substring(indexX+1, indexY));
	  double Y = Double.parseDouble(f.getName().substring(indexY+1, indexP));
		
	  return this.calculateSegmentationData(X, Y, m);	
	}
	
	/**  
    takes X, Y and matrix*
    returns segmentationData with rotated matrix *
    and new upper and lower boundaries */
	public SegmentationData calculateSegmentationData( double X, double Y, Mat m){
		
		 // find closest points on calibration lines
		 CalibrationPoint testPoint = new CalibrationPoint(X, Y, 0, 0, 0);
		 CalibrationPoint foundPoint1 = this.calLine1.findMinDistancePoint( testPoint);
		 CalibrationPoint foundPoint2 = this.calLine2.findMinDistancePoint( testPoint);
		 
		 // check which point is closer and assign to output
		 double dist1 = this.calLine1.findDistance(testPoint, foundPoint1);
		 double dist2 = this.calLine2.findDistance(testPoint, foundPoint2); 
		 CalibrationPoint pointOutput = foundPoint1;
		 if (dist2<dist1) pointOutput = foundPoint2;
		 
		 // rotate the matrix
		 originalM = rotation(m , pointOutput.getAlfa());
		
		 //calculate boundaries
		 this.calculateSobelXProjectonY();
		 this.calculateUpperAndLowerBoundary( (int) Math.round(pointOutput.getHeight()) );	
		 
		 SegmentationData sd = new SegmentationData(originalM, upperBound, lowerBound );
		 sd.setWidth( pointOutput.getLength());
		 return sd;
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
		
		/**  
	    method calculates a projection *
	    of SobleX onto axisY *
	    of gray scale array */
		public void calculateSobelXProjectonY() {

			Mat sobelx = new Mat(originalM.size(), originalM.type());
			Imgproc.Sobel(originalM.clone(), sobelx, CvType.CV_32F, 1, 0);
			
			sobelXprojY = new double[sobelx.rows()];
			Arrays.fill(sobelXprojY, 0);
			for (int row = 0; row < sobelx.rows(); row++)
				for (int col = 0; col < sobelx.cols(); col++)
					sobelXprojY[row] = sobelXprojY[row] + Math.abs(sobelx.get(row, col)[0]);
		}
		
		  /**  
	    method calculates a position of *
	    left and right boundary of max Area in SobelX projection on axis Y*
	    given the height of symbols */
		public void calculateUpperAndLowerBoundary( int symbolHeight) {
			
			// check if height of the input array is bigger than symbol height
			int arrLength = sobelXprojY.length;
			if (arrLength < symbolHeight) return;
			
			// calculate the summ starting from x=0
			double sumTemp = 0;
			for ( int i=0; i < symbolHeight; i++ )
				sumTemp += sobelXprojY[i];
			
			// calculate summ for other positions and take the max
			double maxSum = sumTemp;
			int xEnd = symbolHeight - 1;
			for ( int i=symbolHeight; i < arrLength; i++ ){
				sumTemp = sumTemp  + sobelXprojY[i] - sobelXprojY[i - symbolHeight];
				if (sumTemp > maxSum){
					maxSum = sumTemp;
					xEnd = i;
				}
			}
			
			// set new Y-borders
			upperBound = (xEnd - symbolHeight + 1);
			lowerBound = (xEnd);
		}
		
}
