package ru.recog.segment;

import java.util.ArrayList;
import java.util.List;

public class CutData {
	
	private List<Integer> cutPoints;
	
	public CutData(SegmentationData data, int... indices) {
		cutPoints = new ArrayList<Integer>();
		for (int i = 0; i < indices.length; i++)
			cutPoints.add(data.getMinimums().get(indices[i]));
	}
	
	public CutData(List<Integer> cutPoints) {
		this.cutPoints = cutPoints;
	}
	
	
	public int[] getCutPointsArray() {
		int[] points = new int[cutPoints.size()];
		for (int i = 0; i < points.length; i++)
			points[i] = cutPoints.get(i);
		return points;
	}
	
	public List<Integer> getCutPoints() {
		return cutPoints;
	}
	
	public double calcEnergy(SegmentationData data) {
		
		double total = 0;
		for (int i = 0; i < cutPoints.size()-1; i ++) {
			int x1 = cutPoints.get(i);
			int x2 = cutPoints.get(i+1);
			
			double avg = 0.5*(double)(data.getProjection()[x1]+data.getProjection()[x2]);
			double sum = 0;
			for (int x  = x1; x<=x2; x++)
				sum+=(data.getProjection()[x]-avg);
			total+= sum/ (x2-x1);
			
		}
		
		return total;
	}
	
	public boolean isEqual(List<Integer> cutList) {
		return cutPoints.containsAll(cutList);
	}
	
	public boolean leftEqual(CutData cut) {
		List<Integer> left = cut.getCutPoints();
		for (int i = 1; i < left.size(); i++)
			if (!left.get(i).equals(cutPoints.get(i-1))) return false;
		return true;
	}
	
	public boolean rightEqual(CutData cut) {
		List<Integer> right = cut.getCutPoints();
		for (int i = 1; i < cutPoints.size(); i++)
			if (!right.get(i-1).equals(cutPoints.get(i))) return false;
		return true;
	}
	
	public double calcEnergyRatio(SegmentationData data) {
		
		double sum1 = calcEnergySymbol(data, 0) + calcEnergySymbol(data, 4) + calcEnergySymbol(data, 5);
		double sum2 = calcEnergySymbol(data, 1) + calcEnergySymbol(data, 2) + calcEnergySymbol(data, 3);
	
		return sum2/sum1;
	}
	
	public double calcEnergySymbol(SegmentationData data, int i) {
		
		if (i>=cutPoints.size()-1) i = cutPoints.size()-2; 

		    int x1 = cutPoints.get(i);
			int x2 = cutPoints.get(i+1);
			
			double avg = 0.5*(double)(data.getProjection()[x1]+data.getProjection()[x2]);
			double sum = 0;
			for (int x  = x1; x<=x2; x++)
				sum+=(data.getProjection()[x]-avg);
			
			sum = sum / (x2-x1);
			
		
		return sum;
	}
	
	public double[] buildLength() {
		//FIXME think about cases when there are more than 7 cut pointa
		if (cutPoints.size() < 7) throw new IllegalStateException("Cannot form Markov chain from CutData with size "+cutPoints.size());
		int length = cutPoints.get(6) - cutPoints.get(0);
		double avLength = (double)length/6;
		double[] ls = new double[6];
		for (int i = 0; i < 6; i++)
			ls[i] = (double)(cutPoints.get(i+1)-cutPoints.get(i))/avLength;
		return ls;
		
	}

	@Override
	public String toString() {
		return cutPoints.toString();
	}
}
