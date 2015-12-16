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

public class ExperimentsCalibrationSegmenter {

	
	private CalibrationLine calLine1; 
	private CalibrationLine calLine2;
	
	// temporary variables
	private double[] sobelXprojY;
	private Mat originalM;
	private int upperBound, lowerBound; //upper is smaller, as axis Y points downward
	
	 /**  
    constructor takes two lines with calibrated *
    positions */
	public ExperimentsCalibrationSegmenter(String XML_FileName1, String XML_FileName2){
		
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
			
//		 pointOutput.setHeight(height);
//		 pointOutput.setAlfa(alfa);
//		 pointOutput.setLength(length);
		 CalibrationPoint result = new 
				 CalibrationPoint(testP.getX(), testP.getY(), height, length, alfa) ;
		 
		 
		 return result;
	}
	
	 public static void main(String[] args) throws Exception {
		 System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
		 
		  testProperties();
		 // differentRotations();
		 //testRecognition();
		 //refactor();
		 
	 }
	 
	
	 /**  
     tries a segmentation through recognition */
 public static void refactor() throws Exception {
		  		
				 
	 LabelFrame lf = new LabelFrame("Images", true);
	 
	 //File folder = new File("C:\\dev\\frames\\VNew\\detected79230"); //detected1411"); //Try")
	 File folder = new File("C:\\dev\\frames\\VNew\\detected1411"); //Try")
	 //File folder = new File("C:\\dev\\frames\\VNew\\detected76635"); //detected1411"); //Try");
	 File[] allFilesInFolder = folder.listFiles();
	 
	 String fnm1 = "C:\\dev\\frames\\VNew\\cal1411\\CalLine1.xml";
	 String fnm2 = "C:\\dev\\frames\\VNew\\cal1411\\CalLine2.xml";
	 
	 //String fnm1 = "C:\\dev\\frames\\VNew\\cal79230\\cal79230\\CalLine1.xml";
	 //String fnm2 = "C:\\dev\\frames\\VNew\\cal79230\\cal79230\\CalLine2.xml";
	 
	 //String fnm1 = "C:\\dev\\frames\\VNew\\cal76635\\cal76635\\CalLine1.xml";
	 //String fnm2 = "C:\\dev\\frames\\VNew\\cal76635\\cal76635\\CalLine2.xml";
 
	 CalibrationSegmenter calSeg = new CalibrationSegmenter(fnm1, fnm2);

	 for(File f : allFilesInFolder) {
		 
		 
		 // exclude all files that are not png
		 String str = f.getName().substring(f.getName().lastIndexOf('.') + 1);
		 //System.out.println(str);
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
		 
		 // try to segment by recognition
		 SegmentationResult result = SegmentationFactory.getMarkovSegmentation().segment(mRot, 
				                      MarkovSegmentation.USE_WIDTH, sd.getWidth() , 
				                      sd.getUpperBound(), sd.getLowerBound());
		 
		 NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\NNet6x12_2883521.nnet",
					new MultipleFeatureExtractor<Mat>(
				new OverlapGradientGridFeatureExtractor(7,13)));
		 
		 CutData cd = Piece.findBestCut(result, nn);
		 
		 // add points in the end and in the beginning
		 if ( cd == null) continue;
			 
//		 List<Integer> newCP= cd.getCutPoints();
//		 int first = newCP.get(0);
//		 int last = 0;
//		 if (newCP.size()>=1)
//		   last = newCP.get(newCP.size()-1);
//		 Mat mRec = mRot.clone();
//		 int symbLength =  (int) sd.getWidth();
//		 
//		 if ( ( first - symbLength) > 0 ) {
//			 first = first - symbLength;
//			 newCP.add(0,first); 
//		 }
//			 
//		 if ( ( first - symbLength) > 0 ) {
//			 first = first - symbLength;
//			 newCP.add(0,first); 
//		 }
//		 
//		 if ( ( last + symbLength) < mRec.cols() ) {
//			 last = last + symbLength;
//			 newCP.add(last); 
//		 }
//			 
//		 if ( ( last + symbLength) < mRec.cols() ) {
//			 last = last + symbLength;
//			 newCP.add(last); 
//		 }
//			
//		 
//		 Mat mNN = Mat.zeros(mRec.size(), mRec.type()) ;// = \
//				 mRec.copyTo(mNN);
		// CutData cd2 = new CutData(newCP);
		 
		 CutData cd2 = CalibrationSegmenter.addLinesToNN(cd, sd);
		 if ( cd2 != null)
             lf.addImage(ImageUtils.drawSegLines(mRot, cd2), "NN+", 3);

		 
		 System.out.println(f.getName());
		 
	 }

	lf.pack();
	lf.setVisible(true);
 }	 
	 
 
 
 
		 /**  
	     tries a segmentation through recognition */
	 public static void testRecognition() throws Exception {
			  		
					 
		 LabelFrame lf = new LabelFrame("Images", true);
		 
		 File folder = new File("C:\\dev\\frames\\VNew\\detected79230"); //detected1411"); //Try");
		 //File folder = new File("C:\\dev\\frames\\VNew\\detected76635"); //detected1411"); //Try");
		 
		 File[] allFilesInFolder = folder.listFiles();
		 
		 //String fnm1 = "C:\\dev\\frames\\VNew\\cal76635\\cal76635\\CalLine1.xml";
		 //String fnm2 = "C:\\dev\\frames\\VNew\\cal76635\\cal76635\\CalLine2.xml";
		 
		 String fnm1 = "C:\\dev\\frames\\VNew\\cal79230\\cal79230\\CalLine1.xml";
		 String fnm2 = "C:\\dev\\frames\\VNew\\cal79230\\cal79230\\CalLine2.xml";
		 
		 //String fnm1 = "C:\\dev\\frames\\VNew\\det79230\\Cal79230\\CalLine1.xml";
		 //String fnm2 = "C:\\dev\\frames\\VNew\\det79230\\Cal79230\\CalLine2.xml";
	 
		 ExperimentsCalibrationSegmenter CalSeg = new ExperimentsCalibrationSegmenter(fnm1, fnm2);

		 int counter = 0;
		 for(File f : allFilesInFolder) {
			 
			 //counter++;
			 //if (counter>200) break;

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
			 
			 //Mat m = Mat.zeros(b.size(), b.type());
			 //Imgproc.GaussianBlur(b, m, new Size(3,3), 0, 0);
    		 //lf.addImage(m, f.getName() , 3);
			 
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
			 
			 NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\NNet6x12_2883521.nnet",
						new MultipleFeatureExtractor<Mat>(
					new OverlapGradientGridFeatureExtractor(7,13)));
			 
			 System.out.println(f.getName());
//			 System.out.println( pointOutput.getHeight() );
//			 System.out.println(trySegment.getUpperBound() + " " + trySegment.getLowerBound());
//			 System.out.println( mRec.height() );
			 
			 CutData cd = Piece.findBestCut(result, nn);
			 
			 // add points in the end and in the beginning
			 List<Integer> newCP= cd.getCutPoints();
			 int first = newCP.get(0);
			 int last = 0;
			 if (newCP.size()>=1)
			   last = newCP.get(newCP.size()-1);
			 
			 if ( ( first - pointOutput.getLength()) > 0 ) {
				 first = first - ((int) pointOutput.getLength());
				 newCP.add(0,first); 
			 }
				 
			 if ( ( first - pointOutput.getLength()) > 0 ) {
				 first = first - ((int) pointOutput.getLength());
				 newCP.add(0,first); 
			 }
			 
			 if ( ( last + pointOutput.getLength()) < mRec.cols() ) {
				 last = last + ((int) pointOutput.getLength());
				 newCP.add(last); 
			 }
				 
			 if ( ( last + pointOutput.getLength()) < mRec.cols() ) {
				 last = last + ((int) pointOutput.getLength());
				 newCP.add(last); 
			 }
				
			 
			 Mat mNN = Mat.zeros(mRec.size(), mRec.type()) ;// = \
					 mRec.copyTo(mNN);
			 CutData cd2 = new CutData(newCP); 
			 if ( cd2 != null)
	             lf.addImage(ImageUtils.drawSegLines(mNN, cd2), "NN+", 3);
			 //
			 
			 //if ( cd != null)
             //lf.addImage(ImageUtils.drawSegLines(mRec, cd), "NN", 3);	
			 
			 
			
			 
			 
//			 lf.addImage(mRot, Double.toString(pointOutput.getAlfa()) , 4);
			 
//			 lf.addImage(mRot1, Double.toString(foundPoint1.getAlfa()) , 4);
//			 lf.addImage(mRot2, Double.toString(foundPoint2.getAlfa()) , 4);
			 
		 }
	
		lf.pack();
		lf.setVisible(true);
	 }	 
	 
	 /**  
     different rotations 
     can be used to estimate rotations angles
     for calibration
     */
	 public static void differentRotations() throws Exception {
		
		 LabelFrame lf = new LabelFrame("Different Rotations", true);
		 File folder = new File("C:\\dev\\frames\\VNew\\cal76635\\cal76635"); //detected1411"); //Try");
		 File[] allFilesInFolder = folder.listFiles();
		 
		 
		 for(File f : allFilesInFolder) {
		 
			 // exclude all files that are not png
		   String str = f.getName().substring(f.getName().lastIndexOf('.') + 1);
		   if (!"png".equals(str) )
				 continue;
		   
		   System.out.println(f.getName());
		   Mat m = Imgcodecs.imread( f.getAbsolutePath() , Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		   lf.addImage(m, f.getName(), 4);
		   for (int i=-7; i<=7; i++ ) {
			 Mat rot = rotation( m, i);
			 lf.addImage(rot, Integer.toString(i) , 4);
		 }
		 
		 }
		lf.pack();
		lf.setVisible(true);
		
		// declare the variables
		File folder2 = new File("C:\\dev\\frames\\VNew\\cal76635\\cal76635\\rot");
		String str;
		Mat m;
		Mat img;
		
		boolean save = false; //true; //false;
		if (save) {

			// open rotate and save one file
			str = "V76635N0t1120X93Y244.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, -3.5);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);

			// open rotate and save one file
			str = "V76635N0t1320X262Y422.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, -4);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);

			// open rotate and save one file
			str = "V76635N0t1520X499Y666.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, -5);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);

			// open rotate and save one file
			str = "V76635N1t42520X1155Y391.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, 0);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);

			// open rotate and save one file
			str = "V76635N1t42760X944Y261.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, 1);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);

			// open rotate and save one file
			str = "V76635N1t43000X775Y158.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, 2);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);

			// open rotate and save one file
			str = "V76635N1t43240X641Y75.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, 2.5);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);
			
			// open rotate and save one file
			str = "V76635N1t43480X524Y2.png";
			m = Imgcodecs.imread(new File(folder, str).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			img = rotation(m, 3);
			Imgcodecs.imwrite(new File(folder2, "Rot".concat(str)).getAbsolutePath(), img);
		}
	 }
	 
	 
	 
     /**  
     load files with some *
     properties and do some tests */
	 public static void testProperties() throws Exception {
		  		
			
	 
	  LabelFrame lf = new LabelFrame("For Inspection", true);
		 
	  // load files list with their properties	 
	  Properties props = new Properties();
	  props.load(new FileInputStream("C:\\dev\\frames\\VNew\\cal1411\\props2.properties"));
	  props.list(System.out);
	  
	  
		File dir = new File("C:\\dev\\frames\\VNew\\cal1411");
		
		 String fnm1 = "c:\\dev\\CalLine1.xml";
		 String fnm2 = "c:\\dev\\CalLine2.xml";
	 
		 CalibrationSegmenter calSeg = new CalibrationSegmenter(fnm1, fnm2);
		
		for ( String key: props.stringPropertyNames() ){
			String leString =  props.getProperty(key);
			double startInt = Double.parseDouble(leString); // starting point Integer
			//int heInteger = Integer.parseInt( leString); // starting point Integer
			
			
		
			File f = new File(dir, key);
			System.out.println( f.getAbsolutePath() + " " + startInt );
			
			 Mat b = Imgcodecs.imread( f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			 lf.addImage(b, f.getName() , 3);		 
			 
			 Mat m = Mat.zeros(b.size(), b.type());
			 Imgproc.GaussianBlur(b, m, new Size(3,3), 0, 0);
			 lf.addImage(m, f.getName() , 3);
			 
			 SegmentationData sd = calSeg.calculateSegmentationData(f, m);
			 
			 
	         Mat mRot = sd.getOriginalMat();
			 // add line bounds
			 Imgproc.line(mRot, new Point(0, sd.getUpperBound()), 
						         new Point(mRot.cols()-1, sd.getUpperBound()), new Scalar(0,255,0));
			 Imgproc.line(mRot, new Point(0, sd.getLowerBound()), 
				                 new Point(mRot.cols()-1, sd.getLowerBound()), new Scalar(0,255,0));			
						

			 //lf.addImage(mRot, "Processed" , 3);
			 
			 //CutData cd = Piece.findBestCut(result, nn);
			 //lf.addImage(ImageUtils.drawSegLines(mRec, cd), "NN", 3);
			 
			 double wd = sd.getWidth();
			 List<Integer> listOfCuts = Arrays.asList(new Integer[]
			 {(int) Math.round(startInt) , 
				 (int) Math.round(startInt+wd), 
				 (int) Math.round(startInt+wd*2), 
				 (int) Math.round(startInt+wd*3), 
				 (int) Math.round(startInt+wd*4), 
				 (int) Math.round(startInt+wd*5), 
				 (int) Math.round(startInt+wd*6)});
			 CutData cd = new CutData(listOfCuts);
			 
		
			 
			 lf.addImage(ImageUtils.drawSegLines(mRot, cd), Double.toString(wd)  , 3);
			
			 SegmentationResult sR = calSeg.segment(sd, startInt);
			 lf.addImage(ImageUtils.drawSegLines(mRot, sR.getPossibleCuts().get(0) ), "modified legacy"  , 3);
			
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
		
		  /**  
	    method calculates segmentation *
	    on the basis of legacySegmentation with known*
	    starting point */
		public SegmentationResult segmentWithStartPoint(Mat m, SegmentationData sd, int startP) {
			
		SegmentationData data = new SegmentationData(m, sd.getUpperBound(), sd.getLowerBound());

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

		LengthEstimate = (int) Math.floor(sd.getWidth());


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
