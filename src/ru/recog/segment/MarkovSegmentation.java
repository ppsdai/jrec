package ru.recog.segment;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.*;
import ru.recog.imgproc.*;

public class MarkovSegmentation implements Segmentation {
	
	private MarkovLD mld;
	
	private static MarkovSegmentation defaultMS = null;
	
	public MarkovSegmentation() {
		mld = MarkovLD.getDefaultMLD();
	}
	
	private static void buildDefaultMS() {
		if (defaultMS == null) defaultMS = new MarkovSegmentation();
	}
	
//	public static class CutIndices {
//		private int[] indices;
//		
//		public CutIndices(int... cutIndex) {
//			this.indices = cutIndex;
//		}
//		
//		public int[] getIndices() {
//			return indices;
//		}
//		
//		public int[] getPoints(List<Integer> minimums) {
//			int[] points = new int[indices.length];
//			for (int i = 0; i < points.length; i++)
//				points[i] = minimums.get(indices[i]);
//			return points;
//		}
//		
//		public List<Integer> getPointsList(List<Integer> minimums) {
//			List<Integer> points = new ArrayList<Integer>();
//			for (int i = 0; i < indices.length; i++)
//				points.add(minimums.get(indices[i]));
//			return points;
//		}
//	}
	
	public static double[] countProbs(SegmentationLog.SegmentationLogEntry sle) {
		Rect r1 = sle.getRectangles().get(0);
		Rect r2 = sle.getRectangles().get(5);
		double length = r2.br().x-r1.x;
		
		double avLength = length/6;
		double[] ls = new double[sle.getRectangles().size()];
		for (int i = 0; i < sle.getRectangles().size(); i++) {
			ls[i] = (double) sle.getRectangles().get(i).width/avLength;
		}
		return ls;
	}
	
	public static void main(String[] args) throws Exception {
		
		String picFolder = args[0];
		String seglogFilename = args[1];
		LabelFrame lf = new LabelFrame(picFolder);
		
		
		MarkovLD mld = MarkovLD.getDefaultMLD();
//		Arrays.fill(SD, new Distrib());
		

		
		double[] l1 = new double[] {1,1,1,1,1,1};
		System.out.println(mld.probability(l1));
		
			
		for (File f : Utils.getOrderedList("/Users/pps/dev/newnumbers")) {
			Mat pm = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//			SegmentationResult res = MarkovSegmentation.segment(pm, SD);
			List<? extends SegmentationResult> asrList = MarkovSegmentation.multisegment(pm);
			lf.addImage(ImageUtils.drawSegLines(pm, asrList.get(0)), f.getName(),3);
			
		}
		
//		Mat pm = Imgcodecs.imread("/Users/pps/dev/SFAULT_0/frame1173201.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
////		List<AdvancedSegmentationResult> segments = multisegment(pm, SD);
//		for (AdvancedSegmentationResult result : multisegment(pm, SD)) {
//			
//			lf.addImage(ImageUtils.drawSegLines(pm, result), 
//					Double.toString(result.energy)+" p="+Double.toString(result.probability)
//					+" e*p="+result.probability*result.energy, 3);
//		}
		//		SegmentationResult res = MarkovSegmentation.segment(pm, SD);
//		lf.addImage(ImageUtils.drawSegLines(pm, res), "yo",3);
		
		lf.pack();
		lf.setVisible(true);
	}

	public static SegmentationResult segment(Mat m, MarkovLD mld) {
		SegmentationResult segResult = new SegmentationResult();
		segResult.setOriginalMat(m.clone());
		SBSegmenter.verticalCut(segResult);
		
//		Mat b = processor.processImage(m);
	
		int[] projX = new int[m.cols()];
		Arrays.fill(projX, 0);
		for (int col=0; col < m.cols(); col ++)
			for (int row = segResult.getUpperBound(); row <= segResult.getLowerBound(); row++)
				projX[col] += 255 - (int) m.get(row, col)[0];
		
		segResult.setIntensity(new MatOfInt(projX));
		
//		System.out.println("PROJECTION: "+Arrays.toString(projX));
	
		// Calculate local minimum
	
	//	int minCount = 0; int maxCount = 0;
		List<Integer> localMinimums = new ArrayList<Integer>();
//		List<Integer> localMaximums = //new ArrayList<Integer>();
//				NewSegmenter.getlocalMinimums(projX, projX.length);
		for (int x = 1; x < m.cols()-1; x++) {
//			if (projX[x+1] < projX[x] && projX[x]>=projX[x-1]) localMaximums.add(x);
			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
		}
		
		if (!localMinimums.contains(0)) localMinimums.add(0, 0);
		if (!localMinimums.contains(m.cols()-1)) localMinimums.add(m.cols()-1);
		
		System.out.println(localMinimums);
		
		List<Double> probList = new ArrayList<Double>();
		
		for (int startingPoint = 0; startingPoint < 3; startingPoint++) {
			for (int i1 = 1; i1<=3; i1++)
			 for (int i2 = 1; i2<=3; i2++)
	 		  for (int i3 = 1; i3<=3; i3++)
				for (int i4 = 1; i4<=3; i4++)
				 for (int i5 = 1; i5<=3; i5++)
					for (int i6 = 1; i6<=3; i6++) {
						if (startingPoint+i1+i2+i3+i4+i5+i6>=localMinimums.size()) probList.add(0.0);
						else {
							CutData indices = new CutData(startingPoint,
									startingPoint+i1,
									startingPoint+i1+i2,
									startingPoint+i1+i2+i3,
									startingPoint+i1+i2+i3+i4,
									startingPoint+i1+i2+i3+i4+i5,
									startingPoint+i1+i2+i3+i4+i5+i6);
							
							int[] points = indices.getPoints(localMinimums);
							
							if (!pointsAcceptable(points, m)) 
								probList.add(0.0);
							else {
								double[] ls = buildLength(points);
								double p = mld.probability(ls);
								probList.add(p);
							}
							
						}
					}
			
		}
		
		double d = Collections.max(probList);
		int N = probList.indexOf(d);
		int i6 = (N % 3) +1;
		N = N / 3;
		int i5 = (N % 3) +1;
		N = N / 3;
		int i4 = (N % 3) +1;
		N = N / 3;		
		int i3 = (N % 3) +1;
		N = N / 3;
		int i2 = (N % 3) +1;
		N = N / 3;
		int i1 = (N % 3) +1;
		N = N / 3;
		int startingPoint = N ;
		
		List<Integer> cutPoints = new ArrayList<Integer>();
		cutPoints.add(localMinimums.get(startingPoint));
		cutPoints.add(localMinimums.get(startingPoint+i1));
		cutPoints.add(localMinimums.get(startingPoint+i1+i2));
		cutPoints.add(localMinimums.get(startingPoint+i1+i2+i3));
		cutPoints.add(localMinimums.get(startingPoint+i1+i2+i3+i4));
		cutPoints.add(localMinimums.get(startingPoint+i1+i2+i3+i4+i5));
		cutPoints.add(localMinimums.get(startingPoint+i1+i2+i3+i4+i5+i6));
		
		segResult.setCutPoints(cutPoints);
		
		return segResult;
	}
	
	public static List<? extends SegmentationResult> multisegment(Mat m) {
		buildDefaultMS();
		return defaultMS.segment(m);
//		return multisegment(m, MarkovLD.getDefaultMLD());
	} 
	
	public List<? extends SegmentationResult> segment(Mat m) {
		
		SegmentationData data = new SegmentationData(m);//, ub, lb);
		
//		AdvancedSegmentationResult segResult = new AdvancedSegmentationResult();
//		segResult.setOriginalMat(m.clone());
//		SBSegmenter.verticalCut(segResult);
//		
//		int[] projX = new int[m.cols()];
//		Arrays.fill(projX, 0);
//		for (int col=0; col < m.cols(); col ++)
//			for (int row = segResult.getUpperBound(); row <= segResult.getLowerBound(); row++)
//				projX[col] += 255 - (int) m.get(row, col)[0];
//		
//		segResult.setIntensity(new MatOfInt(projX));
//		
//		List<Integer> localMinimums = new ArrayList<Integer>();
//		for (int x = 1; x < m.cols()-1; x++) {
//			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
//		}
//		data.get
//		if (!localMinimums.contains(0)) localMinimums.add(0, 0);
		if (!data.getLocalMinimums().contains(m.cols()-1)) data.getLocalMinimums().add(m.cols()-1);
		
		Map<CutData, Double> cutMap = new HashMap<CutData,Double>();
		
		for (int startingPoint = 0; startingPoint < 6; startingPoint++) {
			for (int i1 = 1; i1<=3; i1++)
			 for (int i2 = 1; i2<=3; i2++)
	 		  for (int i3 = 1; i3<=3; i3++)
				for (int i4 = 1; i4<=3; i4++)
				 for (int i5 = 1; i5<=3; i5++)
					for (int i6 = 1; i6<=3; i6++) {
						if (startingPoint+i1+i2+i3+i4+i5+i6>=data.getLocalMinimums().size()) continue;
						else {
							CutData indices = new CutData(startingPoint,
									startingPoint+i1,
									startingPoint+i1+i2,
									startingPoint+i1+i2+i3,
									startingPoint+i1+i2+i3+i4,
									startingPoint+i1+i2+i3+i4+i5,
									startingPoint+i1+i2+i3+i4+i5+i6);
							
							
							int[] points = indices.getPoints(data.getLocalMinimums());
							if (pointsAcceptable(points, m)) {
								double[] ls = buildLength(points);
								double p = mld.probability(ls);
								if (p!=0)
								cutMap.put(indices, p);
							}
							
						}
					}
			
		}
		
		AdvancedSegmentationResult segResult = new AdvancedSegmentationResult();
		segResult.setCenterLine(data.getCenterLine());
		segResult.setLowerBound(data.getLowerBound());
		segResult.setUpperBound(data.getUpperBound());
		
		SortedMap<Integer, Double> lineMap = new TreeMap<Integer,Double>();
		
		List<AdvancedSegmentationResult> results = new ArrayList<AdvancedSegmentationResult>();
		for (CutData indices : cutMap.keySet()) {
			AdvancedSegmentationResult newResult = cloneResult(segResult);
			List<Integer> cutPoints = indices.getPointsList(data.getLocalMinimums());
			newResult.alpha = 0;//calcAlpha(indices.getPoints(localMinimums), localMinimums, projX);
			newResult.setCutPoints(cutPoints);
			newResult.energy = calcEnergy(cutPoints, data.getProjection());
			newResult.probability = cutMap.get(indices);
			for (Integer i : cutPoints) {
				double val = newResult.energy * newResult.probability;
				double cval = lineMap.getOrDefault(i, 0.0);
				lineMap.put(i, cval+val);
			}
				
			results.add(newResult);
		}
		
		AdvancedSegmentationResult newResult = cloneResult(segResult);
		newResult.energy = -1;
		
		newResult.probability = -1;
		newResult.setCutPoints(findBestLines(lineMap));
		Collections.sort(newResult.getCutPoints());
		
		results.add(0,newResult);
		
		return results;

	}
	
	public List<? extends SegmentationResult> segment(Mat m, double...paramaters) {
		return segment(m);
	}
	
	
	public static List<AdvancedSegmentationResult> multisegment(Mat m, MarkovLD mld) {
		AdvancedSegmentationResult segResult = new AdvancedSegmentationResult();
		segResult.setOriginalMat(m.clone());
		SBSegmenter.verticalCut(segResult);
		
		int[] projX = new int[m.cols()];
		Arrays.fill(projX, 0);
		for (int col=0; col < m.cols(); col ++)
			for (int row = segResult.getUpperBound(); row <= segResult.getLowerBound(); row++)
				projX[col] += 255 - (int) m.get(row, col)[0];
		
		segResult.setIntensity(new MatOfInt(projX));
		
		List<Integer> localMinimums = new ArrayList<Integer>();
		for (int x = 1; x < m.cols()-1; x++) {
			if (projX[x+1] > projX[x] && projX[x]<=projX[x-1]) localMinimums.add(x);
		}
		
		if (!localMinimums.contains(0)) localMinimums.add(0, 0);
		if (!localMinimums.contains(m.cols()-1)) localMinimums.add(m.cols()-1);
		
		Map<CutData, Double> cutMap = new HashMap<CutData,Double>();
		
		for (int startingPoint = 0; startingPoint < 6; startingPoint++) {
			for (int i1 = 1; i1<=3; i1++)
			 for (int i2 = 1; i2<=3; i2++)
	 		  for (int i3 = 1; i3<=3; i3++)
				for (int i4 = 1; i4<=3; i4++)
				 for (int i5 = 1; i5<=3; i5++)
					for (int i6 = 1; i6<=3; i6++) {
						if (startingPoint+i1+i2+i3+i4+i5+i6>=localMinimums.size()) continue;
						else {
							CutData indices = new CutData(startingPoint,
									startingPoint+i1,
									startingPoint+i1+i2,
									startingPoint+i1+i2+i3,
									startingPoint+i1+i2+i3+i4,
									startingPoint+i1+i2+i3+i4+i5,
									startingPoint+i1+i2+i3+i4+i5+i6);
							
							
							int[] points = indices.getPoints(localMinimums);
							if (pointsAcceptable(points, m)) {
								double[] ls = buildLength(points);
								double p = mld.probability(ls);
								if (p!=0)
								cutMap.put(indices, p);
							}
							
						}
					}
			
		}
		
		SortedMap<Integer, Double> lineMap = new TreeMap<Integer,Double>();
		
		List<AdvancedSegmentationResult> results = new ArrayList<AdvancedSegmentationResult>();
		for (CutData indices : cutMap.keySet()) {
			AdvancedSegmentationResult newResult = cloneResult(segResult);
			List<Integer> cutPoints = indices.getPointsList(localMinimums);
			newResult.alpha = 0;//calcAlpha(indices.getPoints(localMinimums), localMinimums, projX);
			newResult.setCutPoints(cutPoints);
			newResult.energy = calcEnergy(cutPoints, projX);
			newResult.probability = cutMap.get(indices);
			for (Integer i : cutPoints) {
				double val = newResult.energy * newResult.probability;
				double cval = lineMap.getOrDefault(i, 0.0);
				lineMap.put(i, cval+val);
			}
				
			results.add(newResult);
		}
		AdvancedSegmentationResult newResult = cloneResult(segResult);
		newResult.energy = -1;
		newResult.probability = -1;
		newResult.setCutPoints(findBestLines(lineMap));
		Collections.sort(newResult.getCutPoints());
		
		results.add(0,newResult);

		return results;
	}
	
	private static List<Integer> findBestLines(SortedMap<Integer,Double> lineMap) {
		List<Integer> bestList = new ArrayList<Integer>();
		List<Map.Entry<Integer,Double>> entries = new ArrayList<Map.Entry<Integer, Double>>(lineMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<Integer, Double>>(){
			@Override
			public int compare(Entry<Integer, Double> o1,
					Entry<Integer, Double> o2) {
				return Double.compare(o1.getValue(), o2.getValue());
			}
		});
		Collections.reverse(entries);
//		System.out.println(entries);
		if (entries.size()< 7) return bestList;
		else for (int i = 0; i < 7; i++) 
			bestList.add(entries.get(i).getKey());
		return bestList;
	}
	
	private static AdvancedSegmentationResult cloneResult(AdvancedSegmentationResult result) {
		AdvancedSegmentationResult newResult = new AdvancedSegmentationResult();
		newResult.setOriginalMat(result.getOriginalMat());
		newResult.setUpperBound(result.getUpperBound());
		newResult.setLowerBound(result.getLowerBound());
		newResult.setCenterLine(result.getCenterLine());
		return newResult;
	}
	
	
	private static boolean pointsAcceptable(int[] points, Mat m) {
//		return true;
		int length = points[6] - points[0];
		if (points[6] > (double) m.cols()*400/527 
				|| length >= (double) m.cols()*400/527 
				|| length <= m.cols()/2) 
			return false;
		
		return true;
	}
	
	
	public static double calcAlpha(int[] cutPoints, List<Integer> minimums, int[] projection) {
		double totalAverage = 0;
		int count = 0;
		for (int i = cutPoints[0]; i <= cutPoints[6]; i++, count++ ) 
			totalAverage+= projection[minimums.get(i)];
		
		totalAverage = totalAverage/count;
		
		double cutAverage = 0;
		for (int i = 0; i < 7; i++)
			cutAverage+=projection[minimums.get(cutPoints[i])];
		cutAverage = cutAverage / 7;
		
		return cutAverage/totalAverage;
	}
	
	public static double calcEnergy(List<Integer> cutPoints, int[] projection) {
		
		double total = 0;
		for (int i = 0; i < cutPoints.size()-1; i ++) {
			int x1 = cutPoints.get(i);
			int x2 = cutPoints.get(i+1);
			
			double avg = 0.5*(double)(projection[x1]+projection[x2]);
			double sum = 0;
			for (int x  = x1; x<=x2; x++)
				sum+=(projection[x]-avg);
			total+= sum/ (x2-x1);
			
		}
		
		return total;
	}
	
	public static double[] buildLength(int[] points) {
		int length = points[6]-points[0];
		double avLength = (double)length/6;
		double[] ls = new double[6];
		for (int i = 0; i < 6; i++)
			ls[i] = (double)(points[i+1]-points[i])/avLength;
		return ls;
		
	}
	
	

}
