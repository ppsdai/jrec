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
	
	
	private static final int MAX_HOPS = 5; //cause great shaman told us so
	private static final int MAX_CHAR_WIDTH = 16;
	private static final int MIN_CHAR_WIDTH = 6;

	public static void main(String[] args) throws IllegalArgumentException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\BSS724021.nnet", 
				new MultipleFeatureExtractor(new OverlapGradientGridFeatureExtractor()));
		
		CompoundImageProcessor cip = new CompoundImageProcessor();
		cip.addImageProcessor(new Binarization(40, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU));
//		cip.addImageProcessor(new LocalBinarization(0.6));
//		cip.addImageProcessor(new ErodingDilator(Imgproc.MORPH_CROSS, new Size(3,3), 2));
		cip.addImageProcessor(new Cropper());
		
		
		//File dir = new File("C:\\dev\\frames\\VNew\\detected76635"); //Good");
		File dir = new File("C:\\dev\\frames\\VNew\\detected1411"); //Good");
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
			
			List<Mat> pieces = result.getSegments();
			
			
//			Mat  res = m.rowRange(result.getRowRange());

			
			
//			for (int p : points)
//			for (int p : result.getCutPoints())
//				Imgproc.line(m1, new Point(p, 0), new Point(p, m1.rows()-1), new Scalar(0,255,0));
			for (Rect r : result.getRevisedRectangles())
				Imgproc.rectangle(m1, r.tl(), r.br(), new Scalar(0,255,0));
			lf.addImage(m1, nn.getLPString(result.getRevisedSegments()) , 5);  //"orig"
/*			
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
	*/
		}
		
	
		lf.pack();
		lf.setVisible(true);
	}

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
			
			segResult.setIntensity(new MatOfInt(projX));
	
			// Calculate local minimum
	
	//		int minCount = 0; int maxCount = 0;
			List<Integer> localMinimums = new ArrayList<Integer>();
			List<Integer> localMaximums = new ArrayList<Integer>();
	
			
			for (int x = 1; x < m.cols()-1; x++) {
				if (projX[x+1] < projX[x] && projX[x]>=projX[x-1]) localMaximums.add(x);
				if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
			}
			
	
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
			for (x = 0; x < MAX_HOPS && x_Start-x>=0; x++) {
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
					if (((mins[x + 1] - mins[x]) < MAX_CHAR_WIDTH)
							&& ((mins[x + 1] - mins[x]) >= MIN_CHAR_WIDTH)) // (diff1 < 3) &&
					{
						divPoints.add(0, mins[x]);
						x = x + 0; // FIXME LOL?
					}
				} else {
					// add the second point on condition that it is inside the
					// interval
					if (((mins[x + 1] - mins[x - 1]) < MAX_CHAR_WIDTH)
							&& ((mins[x + 1] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff2 < 3) &&
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
					if (((mins[x] - mins[x - 1]) < MAX_CHAR_WIDTH)
							&& ((mins[x] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff1 < 3) &&
					{
						divPoints.add(mins[x]);
						x = x + 0; // FIXME ROFLCOPTER
					}
				} else {
					// add the second point on condition that it is inside the
					// interval
					if (((mins[x + 1] - mins[x - 1]) < MAX_CHAR_WIDTH)
							&& ((mins[x + 1] - mins[x - 1]) >= MIN_CHAR_WIDTH)) // (diff2 < 3) &&
					{
						divPoints.add(mins[x + 1]);
						x = x + 1;
					}
	
				}
			}
			
			segResult.setCutPoints(divPoints);
			
			return segResult;
			
		}
	
	public static SegmentationResult shapesegment(Mat m) throws ArrayIndexOutOfBoundsException {
		
		SegmentationResult segResult = new SegmentationResult();
		segResult.setOriginalMat(m.clone());
		verticalCut(segResult);
		
		Mat b = ImageUtils.localbin(m.clone(), 0.6);

		int[] projX = new int[m.cols()];
		Arrays.fill(projX, 0);
		for (int col=0; col < m.cols(); col ++)
			for (int row = segResult.getUpperBound(); row <= segResult.getLowerBound(); row++)
				projX[col] += 255 - (int) m.get(row, col)[0];
		
		segResult.setIntensity(new MatOfInt(projX));

		// Calculate local minimum

//		int minCount = 0; int maxCount = 0;
		List<Integer> localMinimums = new ArrayList<Integer>();
		List<Integer> localMaximums = new ArrayList<Integer>();
		for (int x = 1; x < m.cols()-1; x++) {
			if (projX[x+1] < projX[x] && projX[x]>=projX[x-1]) localMaximums.add(x);
			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
		}
		
		List<BinShape> shapes = ShapeBasedSegmenter.getFinalShapes(b);
		List<BinShape> moreshapes = new ArrayList<BinShape>();
		b = ImageUtils.localbin(m.clone(), 0.4);
		for (BinShape shape : ShapeBasedSegmenter.getFinalShapes(b)) 
			if (!covered(shape, shapes)) moreshapes.add(shape);
		
		shapes.addAll(moreshapes);
		
		List<Integer> divPoints = new ArrayList<Integer>();

		for (int min : localMinimums)
			if (between(shapes, min)) divPoints.add(min);
		
		segResult.setCutPoints(uniteClosePoints(divPoints));
		
		return segResult;
		
	}
	
	private static boolean covered(BinShape shape, List<BinShape> existingShapes) {
		for (BinShape presentShape : existingShapes) {
			Rect r = shape.getBoundingRect();
			Rect pr = presentShape.getBoundingRect();
			if (r.x >= pr.x && r.x<=pr.x+pr.width) return true;
			if (r.x+r.width >= pr.x && r.x+r.width <=pr.x+pr.width) return true;
		}
		return false;
	}
	
	
	private static void verticalCut(SegmentationResult result) {
		Mat m = result.getOriginalMat();
		int[] blackLength = new int[m.rows()];
//		int MaxBlackLength, CountStart;
		
//		SegmentationResult segResult = new SegmentationResult();
//		segResult.setOriginalMat(m.clone());
		
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
		
		result.setUpperBound(UpperPoint);
		result.setLowerBound(LowerPoint);
		result.setCenterLine((int) Math.floor(rowAvg));
	}
	
	private static List<Integer> uniteClosePoints(List<Integer> divPoints) {
		List<Integer> finalPoints = new ArrayList<Integer>();
		
		for (int i = 0; i < divPoints.size() - 1; i++) {
			int x1 = divPoints.get(i);
			int x2 = divPoints.get(i+1);
			if (x2 - x1 <= 3) {
				finalPoints.add((x2+x1)/2);
				i++;
			}
			else finalPoints.add(x1);
			
		}
		
		return finalPoints;
	}
	
	private static boolean between(List<BinShape> shapes, int x) {
		for (BinShape shape : shapes)
			if (x <= shape.getLRPoint().x-1 && x >= shape.getULPoint().x+1)
				return false;
		
		return true;
	}


}
