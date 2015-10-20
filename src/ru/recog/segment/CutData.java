package ru.recog.segment;

import java.util.ArrayList;
import java.util.List;

public class CutData {
	
	private int[] indices;
	
	public CutData(int... cutIndex) {
		this.indices = cutIndex;
	}
	
	public int[] getIndices() {
		return indices;
	}
	
	public int[] getPoints(List<Integer> minimums) {
		int[] points = new int[indices.length];
		for (int i = 0; i < points.length; i++)
			points[i] = minimums.get(indices[i]);
		return points;
	}
	
	public List<Integer> getPointsList(List<Integer> minimums) {
		List<Integer> points = new ArrayList<Integer>();
		for (int i = 0; i < indices.length; i++)
			points.add(minimums.get(indices[i]));
		return points;
	}

}
