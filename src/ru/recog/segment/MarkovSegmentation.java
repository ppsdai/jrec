package ru.recog.segment;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.LabelFrame;
import ru.recog.Utils;

public class MarkovSegmentation implements Segmentation {
	
	private MarkovLD mld;
	
	private static MarkovSegmentation defaultMS = null;
	
	public MarkovSegmentation() {
		mld = MarkovLD.getDefaultMLD();
	}
	
	private static void buildDefaultMS() {
		if (defaultMS == null) defaultMS = new MarkovSegmentation();
	}
	
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
//			List<? extends SegmentationResult> asrList = MarkovSegmentation.multisegment(pm);
//			lf.addImage(ImageUtils.drawSegLines(pm, asrList.get(0)), f.getName(),3);
			
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

	
	public static SegmentationResult multisegment(Mat m) {
		buildDefaultMS();
		return defaultMS.segment(m);
	} 
	
	public SegmentationResult segment(Mat m) {
		
		SegmentationData data = new SegmentationData(m);//, ub, lb);
		
		if (!data.getMinimums().contains(m.cols()-1)) data.getMinimums().add(m.cols()-1);
		
		Map<CutData, Double> cutMap = new HashMap<CutData,Double>();
		
		for (int startingPoint = 0; startingPoint < 6; startingPoint++) {
			for (int i1 = 1; i1<=3; i1++)
			 for (int i2 = 1; i2<=3; i2++)
	 		  for (int i3 = 1; i3<=3; i3++)
				for (int i4 = 1; i4<=3; i4++)
				 for (int i5 = 1; i5<=3; i5++)
					for (int i6 = 1; i6<=3; i6++) {
						if (startingPoint+i1+i2+i3+i4+i5+i6>=data.getMinimums().size()) continue;
						else {
							CutData indices = new CutData(data,
									startingPoint,
									startingPoint+i1,
									startingPoint+i1+i2,
									startingPoint+i1+i2+i3,
									startingPoint+i1+i2+i3+i4,
									startingPoint+i1+i2+i3+i4+i5,
									startingPoint+i1+i2+i3+i4+i5+i6);
							
							
							if (pointsAcceptable(indices.getCutPointsArray(), m)) {
								double p = mld.probability(indices.buildLength());
								if (p!=0)
									cutMap.put(indices, p);
							}
						}
					}
			
		}
		
		SortedMap<Integer, Double> lineMap = new TreeMap<Integer,Double>();
		
		for (CutData indices : cutMap.keySet()) {
			List<Integer> cutPoints = indices.getCutPoints();
			double energy = indices.calcEnergy(data);
			double probability = cutMap.get(indices);
			for (Integer i : cutPoints) {
				double val = energy * probability;
				double cval = lineMap.getOrDefault(i, 0.0);
				lineMap.put(i, cval+val);
			}
				
		}

		List<Integer> cutPoints = findBestLines(lineMap);
		Collections.sort(cutPoints);
		List<CutData> possibleCuts = new ArrayList<CutData>();
		possibleCuts.add(new CutData(cutPoints));
		possibleCuts.addAll(cutMap.keySet());
		
		return new SegmentationResult(data, possibleCuts);

	}
	
	public SegmentationResult segment(Mat m, double...paramaters) {
		return segment(m);
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

}
