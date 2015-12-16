package ru.recog.segment;


import java.io.File;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ru.recog.XML;

public class CalibrationSegmenter implements Segmentation {

	
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
//		 double dist1 = this.calLine1.findDistance(testPoint, foundPoint1);
//		 double dist2 = this.calLine2.findDistance(testPoint, foundPoint2); 
//		 CalibrationPoint pointOutput = foundPoint1;
//		 if (dist2<dist1) pointOutput = foundPoint2;
		 
		 // calculate precise output
		 CalibrationPoint pointOutput = preciseOutputPoint(testPoint, foundPoint1, foundPoint2);

		 
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
		
		/**  
	    makes an approximation between two points from different lines*
	     */
		
		public CalibrationPoint preciseOutputPoint( CalibrationPoint testP,
				                                           CalibrationPoint p1, CalibrationPoint p2){
			 double dist1 = this.calLine1.findDistance(testP, p1);
			 double dist2 = this.calLine2.findDistance(testP, p2); 
			 CalibrationPoint pointOutput = p1;
			 if (dist2<dist1) pointOutput = p2;
				 
			 
			 double R0_x = p2.getX()    - p1.getX();   double R0_y = p2.getY()    - p1.getY();
			 double r_x  = testP.getX() - p1.getX();   double r_y  = testP.getY() - p1.getY();
			 double L = Math.sqrt( R0_x * R0_x + R0_y * R0_y );
			 double axisX_Proj = ( R0_x * r_x + R0_y * r_y ) / L;
			 
			 double length = p1.getLength() + ( axisX_Proj / L ) * ( p2.getLength() - p1.getLength() ) ;
			 double alfa = p1.getAlfa() + ( axisX_Proj / L ) * ( p2.getAlfa() - p1.getAlfa() ) ;
			 double height = p1.getHeight() + ( axisX_Proj / L ) * ( p2.getHeight() - p1.getHeight() ) ;
				
//			 pointOutput.setHeight(height);
//			 pointOutput.setAlfa(alfa);
//			 pointOutput.setLength(length);
			 CalibrationPoint result = new 
					 CalibrationPoint(testP.getX(), testP.getY(), height, length, alfa) ;
			 
			 
			 return result;
		}
		
		  /**  
	    method calculates segmentation *
	    on the basis of legacySegmentation with known*
	    starting point *
	    parameter[0] = startingpoint */
		@Override
		public SegmentationResult segment(SegmentationData data, double... parameters) {
			
	    int startP = (int) Math.round(parameters[0]);

		int[] mins = new int[data.getMinimums().size()];
		int[] maxs = new int[data.getMaximums().size()];
		for (int i = 0; i < data.getMinimums().size(); i++)
			mins[i] = data.getMinimums().get(i);
		for (int i = 0; i < data.getMaximums().size(); i++)
			maxs[i] = data.getMaximums().get(i);

		// Calculation of the minimums Depth = summ of difference up to the
		// nearest local maximums

		int[] minD = new int[mins.length];

		int maxi = mins[0] < maxs[0] ? -1 : 0;

		for (int i = 0; i < mins.length && maxi < maxs.length; i++, maxi++) {
			// System.out.println(i+" "+maxi);
			if (maxi == -1)
				minD[i] = data.getProjection()[maxs[0]] + data.getProjection()[0] - 2 * data.getProjection()[mins[0]];
			else if (maxi >= maxs.length - 1)
				minD[i] = data.getProjection()[maxs[maxi]] + data.getProjection()[data.getProjection().length - 1]
						- 2 * data.getProjection()[mins[i]];
			else
				minD[i] = data.getProjection()[maxs[maxi]] + data.getProjection()[maxs[maxi + 1]]
						- 2 * data.getProjection()[mins[i]];

		}

		int pointStart = startP;

		// going to beginning
		List<Integer> divPoints = new ArrayList<Integer>();
		int LengthEstimate;
		int diff1, diff2;

		LengthEstimate = (int) Math.floor(data.getWidth());


		// looking for the closest point in minimums
		int x = 0;

		while (mins[x] < (pointStart )) x++;
			
		divPoints.add(pointStart);
		//System.out.println(pointStart);
		//System.out.println(mins[x]);
		
		// Going Forward
		int lastMin = pointStart;
		x--;
		while (x < mins.length - 2) {
			x++;
			diff1 = Math.abs(mins[x] - lastMin) - LengthEstimate;
			diff2 = Math.abs(mins[x + 1] - lastMin) - LengthEstimate;
			
			if (diff1 >= 0) {
				divPoints.add(mins[x]);
			    lastMin = mins[x];
			    continue;
			}
			if ((diff1 <= 0) && (diff2 >= 0)) {

				diff1 = Math.abs(diff1);
				diff2 = Math.abs(diff2);

				if (diff1 == diff2) // check the depth and choose the deepest
				{
					if (minD[x] > minD[x + 1])

						diff2++;
					else
						diff1++;
				}
				if (diff1 < diff2) {

					if (diff1 < 5) {
					  divPoints.add(mins[x]);
					  lastMin = mins[x];
					}
					else {
					  divPoints.add(lastMin + LengthEstimate);
					  lastMin = lastMin + LengthEstimate;
					}
					
					//System.out.println(lastMin);

				} else {
					if (diff2 < 5) {
					  divPoints.add(mins[x + 1]);
					  lastMin = mins[x + 1];
					}
					else {
					  divPoints.add(lastMin + LengthEstimate);
					  lastMin = lastMin + LengthEstimate;
						}
					//System.out.println(lastMin);
				}

			}
		}
			
			//if (!divPoints.contains(0)) divPoints.add(0, 0);
			return new SegmentationResult(data, new CutData(divPoints));
			
		}
		
		@Override
		public SegmentationResult segment(Mat m) {
			
			return segment(new SegmentationData(m));
		}
		
		@Override
		public SegmentationResult segment(Mat m,
				double... parameters) {
			return segment(m);
		}
		
		@Override
		public SegmentationResult segment(SegmentationData data) {
		
			return segment(data, 10);
		}
		
		  /**  
	    method calculates segmentation *
	    on the basis of legacySegmentation with known*
	    starting point */
		public static CutData addLinesToNN(CutData cd, SegmentationData sd) {
		
			 List<Integer> newCP= cd.getCutPoints();
			 int first = newCP.get(0);
			 int last = 0;
			 if (newCP.size()>=1)
			   last = newCP.get(newCP.size()-1);

			 int symbLength =  (int) sd.getWidth();
			 
			 if ( ( first - symbLength) > 0 ) {
				 first = first - symbLength;
				 newCP.add(0,first); 
			 }
				 
			 if ( ( first - symbLength) > 0 ) {
				 first = first - symbLength;
				 newCP.add(0,first); 
			 }
			 
			 if ( ( last + symbLength) <  sd.getOriginalMat().cols() ) {
				 last = last + symbLength;
				 newCP.add(last); 
			 }
				 
			 if ( ( last + symbLength) < sd.getOriginalMat().cols() ) {
				 last = last + symbLength;
				 newCP.add(last); 
			 }
				
			 
			 CutData cd2 = new CutData(newCP);
			
			 return cd2;
			
		}
		
}
