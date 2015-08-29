package ru.recog.imgproc;

import java.io.File;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;
import ru.recog.feature.*;
import ru.recog.nn.NNAnalysis;
import ru.recog.nn.NNWrapper;

public class Segmenter {

	public static void main(String[] args) throws IllegalArgumentException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
/*		File dir = new File("/Users/pps/dev/detected");
		File destdir = new File("/Users/pps/dev/detectedcut");
		
		
		
		for (String fs : dir.list()) {
			String filename = new File(dir,fs).getAbsolutePath();
			System.out.println(filename);
			Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			
			SegmentationResult result = null;
			try { result = Segmenter.segment(m); 
			} catch (ArrayIndexOutOfBoundsException ae) {
				ae.printStackTrace();
			}
			
//			Mat  res = m1.rowRange(result.getRowRange());
			
//			for (int p : points)
			if (result!=null) {
				for (int p : result.getCutPoints())
					Imgproc.line(m1, new Point(p, 0), new Point(p, m1.rows()-1), new Scalar(0,255,0));
				
				String dfilename = new File(destdir, fs).getAbsolutePath();
				
				Imgcodecs.imwrite(dfilename, m1);
			}
		}
		*/
		
		
		
		NNWrapper nn = new NNWrapper("/Users/pps/dev/NNTrain/goodshit/Net496021.nnet", 
				new MultipleFeatureExtractor(new AreaFeatureExtractor(),
						new GravityGridFeatureExtractor(10, 20),
						new SymmetryFeatureExtractor(),
						new EdgeIntersectionFeatureExtractor(3, 3)));
		
		CompoundImageProcessor cip = new CompoundImageProcessor();
		cip.addImageProcessor(new Binarization(40, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU));
//		cip.addImageProcessor(new LocalBinarization(0.6));
//		cip.addImageProcessor(new ErodingDilator(Imgproc.MORPH_CROSS, new Size(3,3), 2));
		cip.addImageProcessor(new Cropper());
		
		
		File dir = new File("/Users/pps/dev/Examples");
		LabelFrame lf = new LabelFrame("GOOD", true);
//
//		
//		for (int fileN = 1; fileN <= 22; fileN++) {
		for (String filestr : dir.list()) {
//			String filename = new File(dir, String.valueOf(fileN).concat(".bmp")).getAbsolutePath();
		
			String filename = new File(dir, filestr).getAbsolutePath();
			Mat m = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			Mat m1 = Imgcodecs.imread(filename, Imgcodecs.CV_LOAD_IMAGE_COLOR);
			
//			List<Integer> points = Segmenter.segment(m);
			SegmentationResult result = Segmenter.segment(m);
			
			List<Mat> pieces = getSegments(m);
			
			
//			Mat  res = m.rowRange(result.getRowRange());

			
			
//			for (int p : points)
			for (int p : result.getCutPoints())
				Imgproc.line(m1, new Point(p, 0), new Point(p, m1.rows()-1), new Scalar(0,255,0));
			lf.addImage(m1, "orig", 5);
			
			for (Mat piece : pieces) {
				Mat proc = cip.processImage(piece);
				lf.addImage(piece, "orig",3);
				for (Mat stage : cip.getStageImages())
					lf.addImage(stage, "stages",3);
				lf.addImage(proc, NNAnalysis.convertNNOutputToString(nn.getNNOutputArray(proc)),3);
				
//				Mat scaled = ImageUtils.scaleUp(proc, 3);
//				lf.addImage(scaled, NNAnalysis.convertNNOutputToString(nn.getNNOutputArray(scaled)),1);


			}

//			lf.addImage(ImageUtils.localbin(m.clone(), 0.6), "bin",5);
		}
		
	
		lf.pack();
		lf.setVisible(true);
	}
	
	
	
	public static List<Mat> getSegments(Mat m) {
		
		return segment(m).getSegments();
	}
	
//	public static List<Mat> getSegments(SegmentationResult result) {
//		List<Mat> pieces = new ArrayList<Mat>();
//		
//		Mat  res = result.getOriginalMat().rowRange(result.getRowRange());
//
//		List<Integer> cutPoints = result.getCutPoints();
//		int x0 = 0; int x1 = 0;
//		
//		for (int i = 0; i <= cutPoints.size(); i++) {
//			if (i == cutPoints.size()) x1 = res.cols()-1;
//			else x1 = cutPoints.get(i);
//			pieces.add(res.colRange(x0, x1+1));
//			
//			x0 = x1;
//		}
//		return pieces;
//	}

	public static SegmentationResult segment(Mat m) throws ArrayIndexOutOfBoundsException {
			int[] blackLength = new int[m.rows()];
	//		int MaxBlackLength, CountStart;
			
			SegmentationResult segResult = new SegmentationResult();
			segResult.setOriginalMat(m.clone());
			
			Mat b = ImageUtils.localbin(m.clone(), 0.6);
	
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
//			System.out.println("blacklength");
	//		for (int i : blackLength)
	//			System.out.println(i);
				
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
	
	
			int[] projX = new int[m.cols()];
			Arrays.fill(projX, 0);
			for (int col=0; col < m.cols(); col ++)
				for (int row = UpperPoint; row <= LowerPoint; row++)
					projX[col] += 255 - (int) m.get(row, col)[0];
	
			// Calculate local minimum
	
	//		int minCount = 0; int maxCount = 0;
			List<Integer> localMinimums = new ArrayList<Integer>();
			List<Integer> localMaximums = new ArrayList<Integer>();
	
			
			for (int x = 1; x < m.cols()-1; x++) {
				if (projX[x+1] < projX[x] && projX[x]>=projX[x-1]) localMaximums.add(x);
				if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
			}
	
//			System.out.println("max: "+localMaximums);
//			System.out.println(localMaximums.size());
//			
//			System.out.println("mins: "+localMinimums);
//			System.out.println(localMinimums.size());
	
			int[] mins = new int[localMinimums.size()];
			int[] maxs = new int[localMaximums.size()];
			for (int i=0; i < localMinimums.size(); i++)
				mins[i] = localMinimums.get(i);
			for (int i=0; i < localMaximums.size(); i++)
				maxs[i] = localMaximums.get(i);
	
			// Calculation of the minimums Depth = summ of difference up to the nearest local maximums
	
			int [] minD = new int[mins.length];
			
			int maxi = mins[0]<maxs[0]? -1 : 0;
	
			
			for (int i = 0; i < mins.length && maxi < maxs.length; i++, maxi++) {
//				System.out.println(i+" "+maxi);
				if (maxi==-1)
					minD[i] = projX[maxs[0]]+projX[0] - 2*projX[mins[0]];
				else if (maxi>=maxs.length-1)
					minD[i] = projX[maxs[maxi]]+projX[projX.length-1] - 2*projX[mins[i]];
				else
					minD[i] = projX[maxs[maxi]]+projX[maxs[maxi+1]] - 2*projX[mins[i]];
			
			}
	
			int pointStart = (int) Math.round(0.55 * m.cols());
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
			List<Integer> divPoints = new ArrayList<Integer>();
			int LengthEstimate;
			int diff1, diff2;
	
			LengthEstimate = Math.round(2 * (LowerPoint - UpperPoint) / 3);
			if (LengthEstimate < 9) LengthEstimate = 9; // if (LengthEstimate < 8) LengthEstimate = 8; //if (LengthEstimate < 9) LengthEstimate = 9;
			if (LengthEstimate > 14) LengthEstimate = 14;// if (LengthEstimate > 14) LengthEstimate = 14;
	
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
					if (((mins[x + 1] - mins[x]) < 16)
							&& ((mins[x + 1] - mins[x]) >= 6)) // (diff1 < 3) &&
					{
						divPoints.add(0, mins[x]);
						x = x + 0; // FIXME LOL?
					}
				} else {
					// add the second point on condition that it is inside the
					// interval
					if (((mins[x + 1] - mins[x - 1]) < 16)
							&& ((mins[x + 1] - mins[x - 1]) >= 6)) // (diff2 < 3) &&
					{
						divPoints.add(0, mins[x - 1]);
						x = x - 1;
					}
	
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
					if (((mins[x] - mins[x - 1]) < 16)
							&& ((mins[x] - mins[x - 1]) >= 6)) // (diff1 < 3) &&
					{
						divPoints.add(mins[x]);
						x = x + 0; // FIXME ROFLCOPTER
					}
				} else {
					// add the second point on condition that it is inside the
					// interval
					if (((mins[x + 1] - mins[x - 1]) < 16)
							&& ((mins[x + 1] - mins[x - 1]) >= 6)) // (diff2 < 3) &&
					{
						divPoints.add(mins[x + 1]);
						x = x + 1;
					}
	
				}
			}
			
			segResult.setCutPoints(divPoints);
			
			return segResult;
			
		}

}
