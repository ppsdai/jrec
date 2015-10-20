package ru.recog.segment;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.opencv.core.Mat;
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
		
		// calculating vertical cuts, minimums, etc
		SegmentationData data = new SegmentationData(m);
		
		if (!data.getMinimums().contains(m.cols()-1)) data.getMinimums().add(m.cols()-1);
		
		Map<CutData, Double> cutMap = new HashMap<CutData,Double>();
		
		//building table of all cuts with probability above zero
		for (int startingPoint = 0; startingPoint < 6; startingPoint++) {
			for (int i1 = 1; i1<=3; i1++)
			 for (int i2 = 1; i2<=3; i2++)
	 		  for (int i3 = 1; i3<=3; i3++)
				for (int i4 = 1; i4<=3; i4++)
				 for (int i5 = 1; i5<=3; i5++)
					for (int i6 = 1; i6<=3; i6++) {
						if (startingPoint+i1+i2+i3+i4+i5+i6>=data.getMinimums().size()) continue;
						else {
							CutData cut = new CutData(data,
									startingPoint,
									startingPoint+i1,
									startingPoint+i1+i2,
									startingPoint+i1+i2+i3,
									startingPoint+i1+i2+i3+i4,
									startingPoint+i1+i2+i3+i4+i5,
									startingPoint+i1+i2+i3+i4+i5+i6);
							
							
							if (pointsAcceptable(cut.getCutPointsArray(), m)) {
								double p = mld.probability(cut.buildLength());
								if (p!=0)
									cutMap.put(cut, p);
							}
						}
					}
			
		}
		
		// now using table of cuts we find which lines are more likely to be actual segmentation lines
		SortedMap<Integer, Double> lineMap = new TreeMap<Integer,Double>();
		for (CutData cut : cutMap.keySet()) {
			double energy = cut.calcEnergy(data);
			double probability = cutMap.get(cut);
			for (Integer i : cut.getCutPoints()) {
				double val = energy * probability;
				double cval = lineMap.getOrDefault(i, 0.0);
				lineMap.put(i, cval+val);
			}
				
		}

		// find top 7 lines and make sure they are in order
		List<Integer> cutPoints = findBestLines(lineMap);
		Collections.sort(cutPoints);
		
		List<CutData> possibleCuts = new ArrayList<CutData>(cutMap.keySet());
		possibleCuts.add(0, new CutData(cutPoints)); //TODO it's possible that this one is already in the table somewhere
		
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
