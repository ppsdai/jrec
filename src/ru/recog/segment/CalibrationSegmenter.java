package ru.recog.segment;


import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.*;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.nn.NNWrapper;

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
		 
		 
		 return new SegmentationData(originalM, upperBound, lowerBound );
	}
	
	
	 public static void main(String[] args) throws Exception {
		 System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
		 
		 // testProperties();
		 // differentRotations();
		 //testRecognition();
		 refactor();
		 
	 }
	 
	
	 /**  
     tries a segmentation through recognition */
 public static void refactor() throws Exception {
		  		
				 
	 LabelFrame lf = new LabelFrame("Images", true);
	 
	 File folder = new File("C:\\dev\\frames\\VNew\\FP"); //detected1411"); //Try");
	 File[] allFilesInFolder = folder.listFiles();
	 
	 String fnm1 = "c:\\dev\\CalLine1.xml";
	 String fnm2 = "c:\\dev\\CalLine2.xml";
 
	 CalibrationSegmenter calSeg = new CalibrationSegmenter(fnm1, fnm2);

	 for(File f : allFilesInFolder) {
		 
		 
		 // exclude all files that are not png
		 String str = f.getName().substring(f.getName().lastIndexOf('.') + 1);
		 System.out.println(str);
		 if (!"png".equals(str) )
			 continue;
	
			 
		 
		 Mat m = Imgcodecs.imread( f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		 lf.addImage(m, f.getName() , 3);		 
		 
		 
		 SegmentationData sd = calSeg.calculateSegmentationData(f, m);
		 
		 
         Mat mRot = sd.getOriginalMat();
		 // add line bounds
		 Imgproc.line(mRot, new Point(0, sd.getUpperBound()), 
					         new Point(mRot.cols()-1, sd.getUpperBound()), new Scalar(0,255,0));
		 Imgproc.line(mRot, new Point(0, sd.getLowerBound()), 
			                 new Point(mRot.cols()-1, sd.getLowerBound()), new Scalar(0,255,0));			
					

		 lf.addImage(mRot, "Processed" , 3);
		 System.out.println(f.getName());
		 
	 }

	lf.pack();
	lf.setVisible(true);
 }	 
	 
 
		 /**  
	     tries a segmentation through recognition */
	 public static void testRecognition() throws Exception {
			  		
					 
		 LabelFrame lf = new LabelFrame("Images", true);
		 
		 File folder = new File("C:\\dev\\frames\\VNew\\FP"); //detected1411"); //Try");
		 File[] allFilesInFolder = folder.listFiles();
		 
		 String fnm1 = "c:\\dev\\CalLine1.xml";
		 String fnm2 = "c:\\dev\\CalLine2.xml";
	 
		 CalibrationSegmenter CalSeg = new CalibrationSegmenter(fnm1, fnm2);

		 for(File f : allFilesInFolder) {
			 
			 

			 //System.out.println(f.getName());
			 int indexX = f.getName().lastIndexOf('X');
			 int indexY = f.getName().lastIndexOf('Y');
			 int indexP = f.getName().lastIndexOf('.');
			 

			 // exclude all files that are not png
			 String str = f.getName().substring(f.getName().lastIndexOf('.') + 1);
			 System.out.println(str);
			 if (!"png".equals(str) )
				 continue;
		
			 
			 
			 double X = Double.parseDouble(f.getName().substring(indexX+1, indexY));
			 double Y = Double.parseDouble(f.getName().substring(indexY+1, indexP));
				 
			 //System.out.println(X+" "+Y);
			 CalibrationPoint testPoint = new CalibrationPoint(X, Y, 0, 0, 0);
			 CalibrationPoint foundPoint1 = CalSeg.calLine1.findMinDistancePoint( testPoint);
			 CalibrationPoint foundPoint2 = CalSeg.calLine2.findMinDistancePoint( testPoint);
			 
			 // check which point is closer and assign to output
			 double dist1 = CalSeg.calLine1.findDistance(testPoint, foundPoint1);
			 double dist2 = CalSeg.calLine2.findDistance(testPoint, foundPoint2); 
			 CalibrationPoint pointOutput = foundPoint1;
			 if (dist2<dist1) pointOutput = foundPoint2;
			 
			 
			 Mat m = Imgcodecs.imread( f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			 lf.addImage(m, f.getName() , 3);
			 
			 Mat mRot = rotation(m , pointOutput.getAlfa());
			 //Mat mRot1 = rotation(m , foundPoint1.getAlfa());
			 //Mat mRot2 = rotation(m , foundPoint2.getAlfa());
			 
			 // add lines on rotated image
			 
				int median = mRot.rows() / 2;
				SegData trySegment = new SegData(mRot, median - 7, median + 7 );
				trySegment.calculateSobelXProjectonY();
				trySegment.calculateUpperAndLowerBoundary( (int) Math.round(pointOutput.getHeight()) );	
				
				Mat mRec = mRot.clone();
				// add line bounds
				Imgproc.line(mRot, new Point(0, trySegment.getUpperBound()), 
						         new Point(mRot.cols()-1, trySegment.getUpperBound()), new Scalar(0,255,0));
				Imgproc.line(mRot, new Point(0, trySegment.getLowerBound()), 
				                 new Point(mRot.cols()-1, trySegment.getLowerBound()), new Scalar(0,255,0));			
				
				lf.addImage(mRot, "H= " + Double.toString(pointOutput.getHeight())
				                  +" "  + Double.toString(pointOutput.getAlfa()), 3);
			
			
			 // try to segment by recognition
			 SegmentationResult result = SegmentationFactory.getMarkovSegmentation().segment(mRec, 
					                      MarkovSegmentation.USE_WIDTH, pointOutput.getLength() , 
					                      trySegment.getUpperBound(), trySegment.getLowerBound());
			 
//			 NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\NNet6x12_2883521.nnet",
//						new MultipleFeatureExtractor<Mat>(
//					new OverlapGradientGridFeatureExtractor(7,13)));
			 
			 System.out.println(f.getName());
//			 System.out.println( pointOutput.getHeight() );
//			 System.out.println(trySegment.getUpperBound() + " " + trySegment.getLowerBound());
//			 System.out.println( mRec.height() );
			 
//			 CutData cd = Piece.findBestCut(result, nn);
//			 if ( cd != null)
//             lf.addImage(ImageUtils.drawSegLines(mRec, cd), "NN", 3);	
			 
//			 lf.addImage(mRot, Double.toString(pointOutput.getAlfa()) , 4);
			 
//			 lf.addImage(mRot1, Double.toString(foundPoint1.getAlfa()) , 4);
//			 lf.addImage(mRot2, Double.toString(foundPoint2.getAlfa()) , 4);
			 
		 }
	
		lf.pack();
		lf.setVisible(true);
	 }	 
	 
	 /**  
     different rotations */
	 public static void differentRotations() throws Exception {
		
		 LabelFrame lf = new LabelFrame("Different Rotations", true);
		 Mat m = Imgcodecs.imread( "C:\\dev\\PlatesSegmentation\\Calibrate\\V1411N1t19880X443Y664.png" 
				                  , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		 lf.addImage(m, "Original", 4);
		 for (int i=-7; i<=7; i++ ) {
			 Mat rot = rotation( m, i);
			 lf.addImage(rot, Integer.toString(i) , 4);
		 }
		 
		lf.pack();
		lf.setVisible(true);
		
		Mat img = rotation( m, -1);
		Imgcodecs.imwrite("C:\\dev\\PlatesSegmentation\\Calibrate\\RotM1_V1411N1t19880X443Y664.png", img);

	 }
	 
	 
	 
     /**  
     load files with some *
     properties and do some tests */
	 public static void testProperties() throws Exception {
		  		
			
	/*  Properties pros = new Properties();
	  pros.setProperty("1.bmp", "5");
	  pros.setProperty("2.bmp", "6");
	  pros.setProperty("3.bmp", "7");
	  
	  pros.store(new FileOutputStream("C:\\dev\\PlatesSegmentation\\props.properties"), "");*/
	  
		 
	  LabelFrame lf = new LabelFrame("For Inspection", true);
		 
	  // load files list with their properties	 
	  Properties props = new Properties();
	  props.load(new FileInputStream("C:\\dev\\frames\\VNew\\FP\\props2.properties"));
	  props.list(System.out);
	  
	  
		File dir = new File("C:\\dev\\frames\\VNew\\FP");
		
		for ( String key: props.stringPropertyNames() ){
			String leString =  props.getProperty(key);
			//int startInt = Integer.parseInt( leString); // starting point Integer
			int heInteger = Integer.parseInt( leString); // starting point Integer
			
			
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
