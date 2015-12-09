package ru.recog.segment;

import java.util.*;
import java.util.Map.Entry;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.nn.NNWrapper;

public class MarkovSegmentation implements Segmentation {
	
	private MarkovLD mld;
	
	private static MarkovSegmentation defaultMS = null;
	
	private static final double NOREGIONRATIO = 0.8;
	
	private static final double ALL_POSSIBLE = 0.1;
	
	public static final double USE_WIDTH = 1.11;
	
	public MarkovSegmentation() {
		mld = MarkovLD.getDefaultMLD();
	}
	
	private static void buildDefaultMS() {
		if (defaultMS == null) defaultMS = new MarkovSegmentation();
	}
	


	public SegmentationResult segment(Mat m) {
		
		// calculating vertical cuts, minimums, etc
		SegmentationData data = new SegmentationData(m);
		
		if (!data.getMinimums().contains(0)) data.getMinimums().add(0, 0);

		if (!data.getMinimums().contains(m.cols()-1)) data.getMinimums().add(m.cols()-1);
		
		Map<CutData, Double> cutMap = buildCuts(data, mld);
		
		final Map<Integer, Double> lineMap = buildLineEvals(cutMap, data);
		
		List<CutData> all = new ArrayList<CutData>(cutMap.keySet());
		List<CutData> used = new ArrayList<CutData>();
		final SegmentationData fdata = data;
		Comparator<CutData> c = new Comparator<CutData>() {
			@Override
			public int compare(CutData o1, CutData o2) {
				return Double.compare(calcWithLineMap(o2, lineMap), calcWithLineMap(o1, lineMap));
//				return Double.compare(o2.calcEnergy(fdata), o1.calcEnergy(fdata));
			}
		};
		Collections.sort(all, c);
		
//		System.out.println("Cuts: "+cutMap.keySet());
		
//		System.out.println("total: "+all.size());
		
		while(!all.isEmpty()) {
			CutData top = all.remove(0);
//			System.out.println("top: "+top);
			if (!isUsed(used, top.getCutPoints())) {
				CutData left = null, right = null;
				for (int i = 1; i < all.size() && (left==null || right==null); i++) {
					CutData candidate = all.get(i);
//					System.out.println("can: "+candidate);

					if (top.leftEqual(candidate)) left = candidate;
					else if (top.rightEqual(candidate)) right = candidate;
				}
//				System.out.println("left: "+left+" right "+right);

				List<Integer> newCut = top.getCutPoints();
				if (left !=null) {
					newCut.add(0, left.getCutPoints().get(0));
//					all.remove(all.indexOf(left));
				}
				if (right !=null) {
					newCut.add(right.getCutPoints().get(right.getCutPoints().size()-1));
//					all.remove(all.indexOf(right));
				}
				used.add( new CutData(newCut));
				
			}
			
		}
		
//		
//
//
//		// find top 7 lines and make sure they are in order
//		List<Integer> cutPoints = findBestLines(cutMap, data);
////		Collections.sort(cutPoints);
//		
//		List<CutData> possibleCuts = new ArrayList<CutData>(cutMap.keySet());
//		possibleCuts.add(0, new CutData(cutPoints)); //TODO it's possible that this one is already in the table somewhere
//		return new SegmentationResult(data, possibleCuts);
		return new SegmentationResult(data, used);

	}
	
	private static double calcWithLineMap(CutData cut, Map<Integer, Double> lineMap) {
		double sum = 0;
		for (int i : cut.getCutPoints())
			sum+=lineMap.getOrDefault(i, 0.0);
		return sum;
	}
	
	
	private static boolean isUsed(List<CutData> used, List<Integer> cut) {
		for (CutData cd : used) if (exactEquals(cd.getCutPoints(), cut)) return true;
		return false;
	}
	
	private static boolean exactEquals(List<Integer> data, List<Integer> cut) {
		int index = data.indexOf(cut.get(0));
		if (index == -1 || index+cut.size()>data.size()) return false;
		for (int i = 1; i<cut.size(); i++)
			if (!data.get(index+i).equals(cut.get(i))) return false;
		return true;
	}
	
	public SegmentationResult segment(Mat m, double...parameters) {
		
		if (USE_WIDTH == parameters[0]) {
			double width = parameters[1];
			int ub = (int) Math.round(parameters[2]);
			int lb = (int) Math.round(parameters[3]);
			
			SegmentationData data = new SegmentationData(m, ub, lb);
			data.setWidth(width);
			if (!data.getMinimums().contains(0)) data.getMinimums().add(0, 0);
			if (!data.getMinimums().contains(m.cols()-1)) data.getMinimums().add(m.cols()-1);
			final Map<CutData, Double> cutMap = buildCuts(data, mld);
			TreeMap<CutData, Double> sorted = new TreeMap<>(new Comparator<CutData>() {
				public int compare(CutData o1, CutData o2) {
					return Double.compare(cutMap.get(o2), cutMap.get(o1));
				};
			});
			for (CutData c : cutMap.keySet()) sorted.put(c, cutMap.get(c));
			return new SegmentationResult(data, new ArrayList<CutData>(sorted.keySet()));
		} else {
			SegmentationData data = new SegmentationData(m);
			
			if (!data.getMinimums().contains(0)) data.getMinimums().add(0, 0);
	
			if (!data.getMinimums().contains(m.cols()-1)) data.getMinimums().add(m.cols()-1);
			
			Map<CutData, Double> cutMap = buildCuts(data, mld);
			
			return new SegmentationResult(data, new ArrayList<CutData>(cutMap.keySet()));
		}
		
	}
	
	private static List<Integer> findBestLines(Map<CutData,Double> cutMap, SegmentationData data) {
		// now using table of cuts we find which lines are more likely to be actual segmentation lines

		SortedMap<Integer,Double> lineMap = buildLineEvals(cutMap,  data);
//		SortedMap<Integer, Double> lineMap = new TreeMap<Integer,Double>();
//		for (CutData cut : cutMap.keySet()) {
//			double energy = cut.calcEnergy(data);
//			double probability = cutMap.get(cut);
//			for (Integer i : cut.getCutPoints()) {
//				double val = energy*probability;
//				double cval = lineMap.getOrDefault(i, 0.0);
//				lineMap.put(i, cval+val);
//			}
//				
//		}
		
		
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
		
		Collections.sort(bestList);
		
		return bestList;
	}
	
	private static SortedMap<Integer,Double> buildLineEvals(Map<CutData,Double> cutMap, SegmentationData data) {
		SortedMap<Integer, Double> lineMap = new TreeMap<Integer,Double>();
		for (CutData cut : cutMap.keySet()) {
			double energy = cut.calcEnergy(data);
			double probability = cutMap.get(cut);
			for (Integer i : cut.getCutPoints()) {
				double val = energy*probability;
				double cval = lineMap.getOrDefault(i, 0.0);
				lineMap.put(i, cval+val);
			}
		}
		return lineMap;
	}
	
	public static boolean pointsAcceptable(int[] points, Mat m) {
//		return true;
		int length = points[6] - points[0];
		if (points[6] > (double) m.cols()*NOREGIONRATIO 
				|| length >= (double) m.cols()*NOREGIONRATIO 
				|| length <= m.cols()/2) 
			return false;
		
		return true;
	}
	
	public static boolean pointsAcceptedWithWidth(int[] points, Mat m, double width) {
		int length = points[6] - points[0];

		double diff = Math.abs(length - width*6);
		return diff/length <= 0.1;
	}
	
	public static Map<CutData, Double> buildCuts(SegmentationData data, MarkovLD mld) {
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
							
							
							boolean pointIsAcceptable = data.getWidth() > 0? 
									pointsAcceptedWithWidth(cut.getCutPointsArray(), data.getOriginalMat(), data.getWidth()) :
									pointsAcceptable(cut.getCutPointsArray(), data.getOriginalMat());
							
							if (pointIsAcceptable) {
								double p = mld.probability(data.getWidth() <0? cut.buildLength() 
										: cut.buildLength(data.getWidth()));
								if (p!=0)
									cutMap.put(cut, p);
							}
						}
					}
		}
		return cutMap;
		
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
