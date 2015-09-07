package ru.recog.imgproc;

import java.io.File;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;
import java.util.Arrays;

public class NewSegmenter {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		File dir = new File("c:\\dev\\PlatesSegmentation"); //Good");
		//File dir = new File("c:\\dev\\Summ");
		LabelFrame lf = new LabelFrame("GOOD", true);
//
		//for (String filestr : dir.list()) 
		{
		    String filestr = "25.bmp";
			String filename = new File(dir, filestr).getAbsolutePath();
			Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			Mat b = ImageUtils.localbin(m.clone(), 0.6);  //0.6
			
			// outputs a binary image instead of a grayscale
			b = ImageUtils.localbin(m.clone(), 0.8);
		    Imgproc.cvtColor(b, m1, Imgproc.COLOR_GRAY2RGB);// CV_GRAY2RGB);
			
			SegmentationResult result = new SegmentationResult(); 
			result = NewSegmenter.segment(m, result);
			// second time with adjusted length
			//result = NewSegmenter.segment(m, result);
			
			System.out.println("File: " + filestr);
			//System.out.println(localMaximums.size());
			
			//List<Mat> pieces = NewSegmenter.getSegments(result);
			
			
			for (int p : result.getCutPoints())
			if ( (p >= result.getleftPoint()) && ( p <= result.getrightPoint()) )  // checks boundary of number plate
				Imgproc.line(m1, new Point(p, 0), new Point(p, m1.rows()-1), new Scalar(0,255,0));
			
			lf.addImage(m1, filestr, 5);			
			lf.addImage( Histogram(m, result ) ,"hist", 1);
			//lf.addImage( Histogram(b, result ) ,"hist", 1);
			
		}
	
		lf.pack();
		lf.setVisible(true);

	}
	
/*	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		File dir = new File("c:\\dev\\PlatesSegmentation"); //Good");
		LabelFrame lf = new LabelFrame("GOOD", true);
//
		for (String filestr : dir.list()) {
		
			String filename = new File(dir, filestr).getAbsolutePath();
			Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			
			SegmentationResult result = NewSegmenter.segment(m);
			
			
			System.out.println("File: " + filestr);
			//System.out.println(localMaximums.size());
			
			//List<Mat> pieces = NewSegmenter.getSegments(result);
			
			
			for (int p : result.getCutPoints())
				Imgproc.line(m1, new Point(p, 0), new Point(p, m1.rows()-1), new Scalar(0,255,0));
			lf.addImage(m1, filestr, 5);
			lf.addImage( Histogram(m, 0, m.rows()) ,"hist", 1);

		}
	
		lf.pack();
		lf.setVisible(true);

	}
*/
	
	public static Mat Histogram(Mat m, SegmentationResult result){
		
		// make a projection array

		int UpperPoint = result.getUpperBound();
		int LowerPoint = result.getLowerBound() ;
		int[] projX = new int[m.cols()];
		Mat hist = Mat.zeros(512, 5*m.cols(), CvType.CV_8UC3); //512
				
		Arrays.fill(projX, 0);
		int minV, maxV;
		maxV = 0; minV = 64000;
		for (int col=0; col < m.cols(); col ++)
		{
			for (int row = UpperPoint; row < LowerPoint; row++)
				projX[col] += 255 - (int) m.get(row, col)[0];
			
			if (projX[col] > maxV) maxV = projX[col];
			if (projX[col] < minV) minV = projX[col];
		}
		// find Dx, Dy to be used for plotting
		int dX;
		float dY;
		dX = (int) Math.floor(hist.cols() / m.cols());
		dY = ((float) hist.rows()) / ( maxV - minV );
		
		for (int i = 1; i < m.cols(); i++)
		Imgproc.line( hist, new Point(dX*(i-1), hist.rows() - Math.round(dY*(projX[i-1] - minV))),
		 new Point(dX*i, hist.rows() - Math.round(dY*(projX[i] - minV))), new Scalar(0, 255, 0) ); 

		for (int p : result.getCutPoints())
			Imgproc.line(hist, new Point(5*p, 0), new Point(5*p, hist.rows()-1), new Scalar(0,0,255));
		
		return hist;
	}
	
	
	public static List<Mat> getSegments(SegmentationResult result) {
		List<Mat> pieces = new ArrayList<Mat>();
		
		Mat  res = result.getOriginalMat().rowRange(result.getRowRange());

		List<Integer> cutPoints = result.getCutPoints();
		int x0 = 0; int x1 = 0;
		
		for (int i = 0; i <= cutPoints.size(); i++) {
			if (i == cutPoints.size()) x1 = res.cols()-1;
			else x1 = cutPoints.get(i);
			pieces.add(res.colRange(x0, x1+1));
			
			x0 = x1;
		}
		return pieces;
	}
	
	public static SegmentationResult segment(Mat m, SegmentationResult segResult) throws ArrayIndexOutOfBoundsException {
		int[] blackLength = new int[m.rows()];
//		int MaxBlackLength, CountStart;
		
		//SegmentationResult segResult = new SegmentationResult();
		segResult.setOriginalMat(m.clone());
		
		Mat b = ImageUtils.localbin(m.clone(), 0.6);

		
		/*  Making an array of max length lines  */
		for (int row = 0; row < b.rows(); row++) {    // loop on y
			int col = 0;
			int maxBlackLength = 0; int countStart = 0;
			while (col < (b.cols() - 1)) {    // looking inside a line
				if (b.get(row, col)[0] == 0) { // if it is black then start to look for a line
					countStart = col;
					while ((!((b.get(row, col)[0] == 255) && (b.get(row, col+1)[0] == 255))) && (col < (b.cols() - 1)))       // checks whether it is a line of two white in a row
						col++;
					blackLength[row] = col - countStart;
					if (blackLength[row] > maxBlackLength)  maxBlackLength = blackLength[row];
				}
				else col++;                    // else go to the next pixel
			}
			blackLength[row] = maxBlackLength;
		}
		
//		System.out.println("blacklength");
//		for (int i : blackLength)
//			System.out.println(i);
			
		/* Looking for a central line  */
		Mat sobelx = new Mat(m.size(), m. type());
		Imgproc.Sobel(m.clone(), sobelx, CvType.CV_32F, 1, 0);

		double rowAvg = 0; double sumI = 0; double sqrI = 0;
		for (int row = 0; row < sobelx.rows(); row++) {
			double sobelI = 0;
			for (int col = 0; col < sobelx.cols(); col++) 
				sobelI = sobelI + Math.abs(sobelx.get(row, col)[0]);
			rowAvg = rowAvg + row * sobelI;
			sqrI = sqrI + row * row * sobelI;
			sumI = sumI + sobelI;
		}
		rowAvg = rowAvg / sumI;
		sqrI = Math.sqrt(sqrI/sumI - rowAvg*rowAvg);
		
		if (segResult.getCenterLine() == 0)
			segResult.setCenterLine( (int) Math.round(rowAvg) );


		int UpperPoint, LowerPoint;

		UpperPoint = (int) Math.floor(rowAvg);
		while ((UpperPoint > 0) && (blackLength[UpperPoint] < 5 * Math.round(sqrI)))  //4
			UpperPoint--;

		LowerPoint = (int) Math.ceil(rowAvg);
		while ((LowerPoint < m.rows()) && (blackLength[LowerPoint] < 6 * Math.round(sqrI))) //4
			LowerPoint++;
		
		segResult.setUpperBound(UpperPoint);
		segResult.setLowerBound(LowerPoint);

		
//		System.out.println("Yavg= "+rowAvg+" from "+UpperPoint+" to "+LowerPoint);


		//System.out.println(Arrays.toString(projBinX));
		//System.out.println(projBinX.length);
		
        // projection of a gray scale array
		int[] projX = new int[m.cols()];
		Arrays.fill(projX, 0);
		for (int col=0; col < m.cols(); col ++)
			for (int row = UpperPoint; row <= LowerPoint; row++)
				projX[col] += 255 - (int) m.get(row, col)[0];

		// Calculate local minimum

//		int minCount = 0; int maxCount = 0;
		List<Integer> localMinimums = new ArrayList<Integer>();
		List<Integer> localMaximums = new ArrayList<Integer>();

		
		localMinimums = getlocalMinimums( projX, m.cols());
		localMaximums = getlocalMaximums( projX, m.cols());
	

		// Clean Up minimums and Maximums Arrays, at the end 
		if ( localMinimums.size() == localMaximums.size() )
		  localMinimums.remove((localMinimums.size()-1)); 

		if ( localMinimums.size() != (localMaximums.size() - 1) )
			  localMaximums.remove((localMaximums.size() - 1)); 

		// check if there is enough of local minimums
		if (localMinimums.size() < 8)
		{
			System.out.println(" Error: Not enough of local Minimums ");
			return null;
		}
		// check if number of local maximums match + 1is equal to number of local minimums
		if ( localMinimums.size() != (localMaximums.size() - 1) )
		{
			System.out.println(" Error: Number of Minimums is not Equal to (Number of Maximums - 1) ");
			return null;
		}		

		//OUTPUTS
		//System.out.println("max: "+localMaximums);
		//System.out.println(localMaximums.size());
		
		//System.out.println("mins: "+localMinimums);
		//System.out.println(localMinimums.size());

		int[] mins = new int[localMinimums.size()];
		int[] maxs = new int[localMaximums.size()];
		for (int i=0; i < localMinimums.size(); i++)
			mins[i] = localMinimums.get(i);
		for (int i=0; i < localMaximums.size(); i++)
			maxs[i] = localMaximums.get(i);

		// Calculation of the minimums Depth = summ of difference up to the nearest local maximums		
		int [] minD = new int[mins.length];
		
		for (int x = 0; x < ( localMinimums.size() - 1); x++)
		{
			minD[x] = projX[maxs[x]] + projX[maxs[x + 1]]
				- 2 * projX[mins[x]];
			}
			
		// Calculation of the Number estimated left and right points
		
         
		// find left and right number position estimate
		// to do this find first and second maximums
		int L_Max = 0, SndL_Max = 0, x_L_Max = 0;
		int R_Max = 0, SndR_Max = 0, x_R_Max = (mins.length - 1);
		int CurrentValue;
		for (int x = 0; x < 8; x++)
		{
			CurrentValue = projX[maxs[x]] - projX[mins[x]];
			if ( CurrentValue > L_Max )
			{
				SndL_Max = L_Max;
				L_Max = CurrentValue;
				x_L_Max = x;
			}
		
			CurrentValue = projX[maxs[maxs.length - 1 - x]] - projX[mins[mins.length - 1 - x]];
			if ( CurrentValue > R_Max )
			{
				SndR_Max = R_Max;
				R_Max = CurrentValue;
				x_R_Max = mins.length - 1 - x;
			}
		}
		
		// if there is marked beginning and end add a point 
		if ( L_Max > (2*SndL_Max))
		   segResult.setleftPoint(mins[x_L_Max]);
		else
		   segResult.setleftPoint(mins[0]);
		if ( R_Max > (2*SndR_Max))
			segResult.setrightPoint(mins[x_R_Max]);
		else
			segResult.setrightPoint(mins[mins.length - 1]);
		
		
		b = ImageUtils.localbin(m.clone(), 0.4);  //0.6
		// projection of a binary array
		int[] projBinX = new int[b.cols()];
		Arrays.fill(projBinX, 0);
		for (int col=0; col < b.cols(); col ++)
			for (int row = UpperPoint; row <= LowerPoint; row++)
				projBinX[col] +=  (int) b.get(row, col)[0];
		
		// check list
		List<Integer> check_depth = new ArrayList<Integer>();
		for (int i=0; i < localMinimums.size(); i++)
			check_depth.add(projBinX[localMinimums.get(i)]);
		
		//OUTPUTS
		//System.out.println("mins depth: " + check_depth);
		//System.out.println(check_depth.size());		
		
		//System.out.println(Arrays.toString(projBinX));
		//System.out.println(projBinX.length);
		
	
		//try to remove (or add) points according to the binary picture
		//int[] mins_removed = new int[localMinimums.size()];
		List<Integer> mins_removed = new ArrayList<Integer>(); 
		List<Integer> minD_removed = new ArrayList<Integer>(); 
		for (int i=0; i < localMinimums.size(); i++)
			if (projBinX[localMinimums.get(i)] <= 510) //765) //510 may be got to make 510, but need to change binarisation to 0.4 
			{
				mins_removed.add(localMinimums.get(i));
				minD_removed.add(minD[i]);
			}
		
		//OUTPUTS
		//System.out.println("mins removed: "+mins_removed);
		//System.out.println(mins_removed.size());

		int[] mins_rem = new int[mins_removed.size()];
		int[] minD_rem = new int[mins_removed.size()];
		for (int i=0; i < mins_removed.size(); i++)
		{
			mins_rem[i] = mins_removed.get(i);
			minD_rem[i] = minD_removed.get(i);
		}
		// original
		//List<Integer> divPoints =  calculateDivPoints(segResult, mins, minD, m.cols());

		// removed points
		List<Integer> divPoints =  calculateDivPoints(segResult, mins_rem, minD_rem, m.cols());
		
		//OUTPUTS
		//System.out.println("DivPoints: "+ divPoints);
		//System.out.println(divPoints.size());
		
		// Show difference
		List<Integer> difference_divPoints = new ArrayList<Integer>();
		for (int i = 1; i < divPoints.size() ; i++ )
		if  (divPoints.get(i - 1) >= segResult.getleftPoint())   // checks boundary of number plate
		if  ( difference_divPoints.size() < 6 )	
			difference_divPoints.add(divPoints.get(i) - divPoints.get(i - 1));
		
		Collections.sort( difference_divPoints );
		System.out.println("difference_DivPoints: "+ difference_divPoints);
		System.out.println(difference_divPoints.size());	
		if ( difference_divPoints.size() == 6 )
		{
		  segResult.setLengthEstimate( (int) (difference_divPoints.get(2) + difference_divPoints.get(3))/2 );
		  System.out.println("LengthEstimate recalculated: " + segResult.getLengthEstimate());
		}
		
		segResult.setCutPoints(divPoints);
		return segResult;
		
	}

	// returns the list of CutPoints
	private static List<Integer> calculateDivPoints(SegmentationResult result, int[] mins, int[] minD, int M_Length) {
		
		List<Integer> divPoints = new ArrayList<Integer>();
		
		int pointStart = (int) Math.round(0.55 * M_Length);
//		System.out.println("PS: "+pointStart);

		int x = 1;
		while (mins[x] < pointStart) x++;

		int x_Max = x;
		float ValueMax = minD[x];
		int x_Start = x;
		for (x = 0; x < 5; x++) {
			if (ValueMax < minD[x_Start - x]) {
				ValueMax = minD[x_Start - x];
				x_Max = x_Start - x;
			}
		}

		// going to beginning

		int LengthEstimate = 0;
		int diff1, diff2;
         
		//TODO
		System.out.println("LengthEstimate before: " + result.getLengthEstimate());
		
		if (result.getLengthEstimate() == 0)
		{  
		  LengthEstimate = Math.round(2 * ( result.getLowerBound() - result.getUpperBound() ) / 3);
		  if (LengthEstimate < 9) LengthEstimate = 9; // if (LengthEstimate < 8) LengthEstimate = 8; //if (LengthEstimate < 9) LengthEstimate = 9;
		  if (LengthEstimate > 14) LengthEstimate = 14;// if (LengthEstimate > 14) LengthEstimate = 14;
		  result.setLengthEstimate(LengthEstimate);
		}
		LengthEstimate = result.getLengthEstimate();
		System.out.println("LengthEstimate after: " + result.getLengthEstimate());
		
		x = x_Max;
		divPoints.add(mins[x_Max]);
		

		// Going back
		while (x > 1) {
			x--;
			diff1 = Math.abs((mins[x + 1] - mins[x]) - LengthEstimate);
			diff2 = Math.abs((mins[x + 1] - mins[x - 1]) - LengthEstimate);
			if (diff1 == diff2) // check the depth and choose the deepest
			{
				if (minD[x] > minD[x - 1])

					diff2++;
			}
			if (diff1 < diff2) {
				// add the first point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x]) <= 16)
						&& ((mins[x + 1] - mins[x]) >= 6)) // (diff1 < 3) &&
				{
					divPoints.add(0, mins[x]);
					x = x + 0; // FIXME LOL?
				}
			} else {
				// add the second point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x - 1]) <= 16)
						&& ((mins[x + 1] - mins[x - 1]) >= 6)) // (diff2 < 3) &&
				{
					divPoints.add(0, mins[x - 1]);
					x = x - 1;
				}

			}
		}
        // Fixing the first point
		if (x == 1)
		{
			if (((mins[x+1] - mins[x]) <= 16)
					&& ((mins[x+1] - mins[x]) >= 6))
			{
				divPoints.add(0, mins[x]);
				
			}
			x--;
		}
		
		if (x == 0)
		{
			if (((mins[x+1] - mins[x]) <= 16)
					&& ((mins[x+1] - mins[x]) >= 6))
			{
				divPoints.add(0, mins[x]);
				
			}	
		}
		

		
		// Going Forward
		x = x_Max;
		while (x < mins.length - 2) {
			x++;
			diff1 = Math.abs(Math.abs(mins[x - 1] - mins[x]) - LengthEstimate);
			diff2 = Math.abs(Math.abs(mins[x - 1] - mins[x + 1])
					- LengthEstimate);
			if (diff1 == diff2) // check the depth and choose the deepest
			{
				if (minD[x] > minD[x + 1])

					diff2++;
				else
					diff1++;
			}
			if (diff1 < diff2) {
				// add the first point on condition that it is inside the
				// interval
				if (((mins[x] - mins[x - 1]) <= 16)
						&& ((mins[x] - mins[x - 1]) >= 6)) // (diff1 < 3) &&
				{
					divPoints.add(mins[x]);
					x = x + 0; // FIXME ROFLCOPTER
				}
			} else {
				// add the second point on condition that it is inside the
				// interval
				if (((mins[x + 1] - mins[x - 1]) <= 16)
						&& ((mins[x + 1] - mins[x - 1]) >= 6)) // (diff2 < 3) &&
				{
					divPoints.add(mins[x + 1]);
					x = x + 1;
				}

			}
		}
		
		// CLEAN UP
		// removing double points on first and second position
		if (divPoints.get(1) == divPoints.get(2))
			divPoints.remove(2);
		if (divPoints.get(0) == divPoints.get(1))
			divPoints.remove(1);


		return divPoints;
	}
	// returns the list of localMinimums, adds minimums on first and last location
	private static List<Integer> getlocalMinimums(int[] SearchArr, int projX_Length) {
	
		List<Integer> localMinimums = new ArrayList<Integer>();
		
//		for (int x = 1; x < projX_Length-1; x++) 
//			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
        
		
	// check first point
	if (SearchArr[1] < SearchArr[2])
		localMinimums.add(1);

	// Look inside omitting edge points
	int IsPlato = 0; 
	//int XStart;
	for (int x = 2; x < (projX_Length - 2); x++)
	{
		if ((IsPlato == 0) && (SearchArr[x + 1] > SearchArr[x]) && (SearchArr[x - 1] > SearchArr[x]))
		{
			localMinimums.add(x);
		
		}
		if ( (IsPlato == 0) && (SearchArr[x + 1] == SearchArr[x]) && (SearchArr[x - 1] > SearchArr[x]))
		{
			IsPlato = 1;
			//XStart = x;
		}
		if ((IsPlato == 1) && (SearchArr[x + 1] > SearchArr[x]))
		{
			IsPlato = 0;
			localMinimums.add(x); // (int)((x + XStart) / 2);
			
		}

	}
	// check last point
	if (SearchArr[projX_Length - 2] > SearchArr[projX_Length - 1])
	{
		localMinimums.add(projX_Length-2); 
		
	}
		
		return localMinimums;
	}
	
	// returns the list of localMaximums, adds maximums on first and last location
	private static List<Integer> getlocalMaximums(int[] SearchArr, int projX_Length) {
		
		List<Integer> localMaximums = new ArrayList<Integer>();
		
		//for (int x = 1; x < projX_Length-1; x++) 
		//	if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMaximums.add(x);
        
		// first point is always zero
		{
			localMaximums.add(0);
			
		}
		// Look inside omitting edge points
		int IsPlato = 0;
		//int XStart;
		for (int x = 1; x < (projX_Length - 2); x++)
		{
			if ((IsPlato == 0) && (SearchArr[x + 1] < SearchArr[x]) && (SearchArr[x - 1] <SearchArr[x]) && (x!=1))
			{
				localMaximums.add(x);
				
			}
			if ((IsPlato == 0) && (SearchArr[x + 1] == SearchArr[x]) && (SearchArr[x - 1] < SearchArr[x]))
			{
				IsPlato = 1;
			//	XStart = x;
			}
			if ((IsPlato == 1) && (SearchArr[x + 1] < SearchArr[x]))
			{
				IsPlato = 0;
				localMaximums.add(x); //  (int)((x + XStart) / 2);
				//CountMax++;
			}
		}
		// last point is always (Length - 1)
		{
			localMaximums.add(projX_Length-1);
			
		}
		
		
		return localMaximums;
	}
	
	
}
