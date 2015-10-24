package ru.recog.segment;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.*;

public class APerfectMarkovSegmentation implements Segmentation {
	
	private MarkovLD mld;
	
	private static APerfectMarkovSegmentation defaultMS = null;
	
	public static final double MINIMUM_PROBABILITY = 1e-8;
	
	public APerfectMarkovSegmentation() {
		mld = MarkovLD.getDefaultMLD();
	}
	
	private static void buildDefaultMS() {
		if (defaultMS == null) defaultMS = new APerfectMarkovSegmentation();
	}
	
	public static void main(String[] args) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	
		//String str = "C:\\dev\\frames\\VNew\\detected1411\\V1411N33t50680.png"; 
		String str = "C:\\dev\\PlatesSegmentation\\10.bmp";
		Mat m = Imgcodecs.imread(str, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		
		// take only sensible probabilities, above a minimum threshold
		SegmentationResult result =  multisegment(m);
		// sort according to probability
	
		SortedMap<Double, Integer> sortedProbabilityCorrespondence = 
				              sortByProbability(result, MINIMUM_PROBABILITY);
		
		// take the ones that different max 10 times from max probability
		double maxKey = sortedProbabilityCorrespondence.lastKey();
		SortedMap<Double, Integer> sortedPC_Top = sortedProbabilityCorrespondence.subMap(maxKey/10, maxKey);
		
//		for (double key : sortedPC_Top.keySet()) {
//			// probability, energy, etc
//			//double p = cut.calcEnergyRatio(result.getData());
//			//double p = MarkovLD.getDefaultMLD().probability(cut.buildLength());
//			double p = cut.calcEnergy(result.getData());
//			if (!(sortedCorrespondence.containsKey(p)))  
//				        sortedCorrespondence.put(p, number);
//			else {
//				double minValue = MINIMUM_PROBABILITY;			
//				double closestKey = returnClosestKey(p, sortedCorrespondence, minValue);
//				sortedCorrespondence.put(closestKey, number);				
//			}	  
//
//		}
		
		
		//LabelFrame lf = showSortedSegmentations(result, sortedProbabilityCorrespondence, 3);
		LabelFrame lf = showSortedSegmentations(result, sortedPC_Top, 3);
		
		//LabelFrame lf = showAllSegmentations(result, 3);
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
								if (p > MINIMUM_PROBABILITY)
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

	public static LabelFrame showAllSegmentations(SegmentationResult result, int scale) {
		LabelFrame lf = new LabelFrame("ALL");
		
		Mat cvt = ImageUtils.bin2color(result.getOriginalMat());
		for (CutData cut : result.getPossibleCuts()) {
			Mat m = cvt.clone();
			ImageUtils.drawLines(m, cut);
			double p = MarkovLD.getDefaultMLD().probability(cut.buildLength());
			lf.addImage(m, "Energy = " + Double.toString(cut.calcEnergy(result.getData())) + 
					       " Prob = "  + Double.toString(MarkovLD.getDefaultMLD().probability(cut.buildLength())), 
					scale);
		}
		
		//System.out.println(" Total Number UnSorted= " + result.getPossibleCuts().size());
		
		lf.pack();
		return lf;

	}
	
	
	/**
	 * shows sorted segmentations according to a sorted map
	 */
	public static LabelFrame  showSortedSegmentations(SegmentationResult result, 
			                  SortedMap<Double, Integer> sortedCorrespondence, int scale){
		
		LabelFrame lf = new LabelFrame("SORTED");
		
		
		Mat cvt = ImageUtils.bin2color(result.getOriginalMat());
		for( double key: sortedCorrespondence.keySet() ){
			Mat m = cvt.clone();
			int relevantNumber = sortedCorrespondence.get(key);
			CutData cut = result.getPossibleCuts().get(relevantNumber);
			ImageUtils.drawLines(m, cut);
			double probability = MarkovLD.getDefaultMLD().probability(cut.buildLength());
			double energy = cut.calcEnergy(result.getData());
			double ratio = cut.calcEnergyRatio(result.getData());
			lf.addImage(m, "Energy = " + Double.toString(energy) + 
					       " Prob = "  + Double.toString(probability)+
					       " EnergyRatio = "  + Double.toString(ratio), 
					scale);
		}
		
		//System.out.println(" Total Number Sorted= " + sortedCorrespondence.size());
		
		lf.pack();
		return lf;
	}
	
	/**
	 * returns a key that is permitted in a sorted in increasing order map
	 * it is either a value minus min value, or a point on a half distance
	 * to the closest existing
	 */
	public static double returnClosestKey( double key, SortedMap<Double, Integer> sortedCorrespondence, 
			                                 double minValue){
		double closestKeyValue = key;
		for( double key1: sortedCorrespondence.keySet())
			if (key1 < closestKeyValue ){
				closestKeyValue = key1;
				break;
			}
		double closestKey = key - minValue;
		if ((closestKey < closestKeyValue) && (closestKeyValue != key))
			closestKey = (closestKeyValue + key) / 2;	
		return closestKey;
	}

	/**
	 * returns a sorted map of probabilities
	 */
	public static SortedMap<Double, Integer> sortByProbability(SegmentationResult result, double minValue) {

		SortedMap<Double, Integer> sortedCorrespondence = new TreeMap<Double, Integer>();

		int number = 0;
		for (CutData cut : result.getPossibleCuts()) {

			double p = MarkovLD.getDefaultMLD().probability(cut.buildLength());

			if (!(sortedCorrespondence.containsKey(p)))
				sortedCorrespondence.put(p, number);
			else {

				double closestKey = returnClosestKey(p, sortedCorrespondence, minValue);
				sortedCorrespondence.put(closestKey, number);
			}

			number++;
		}
		return sortedCorrespondence;
	}

}
