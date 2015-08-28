package ru.recog.imgproc;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Range;

public class SegmentationResult {
	
	private Mat originalMat;
	private int upperBound, lowerBound;
	private List<Integer> cutPoints;
	
	
	public Mat getOriginalMat() {
		return originalMat;
	}
	
	public Range getRowRange() {
		return new Range(upperBound, lowerBound+1);
	}
	
	public void setOriginalMat(Mat originalMat) {
		this.originalMat = originalMat;
	}
	public int getUpperBound() {
		return upperBound;
	}
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}
	public int getLowerBound() {
		return lowerBound;
	}
	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}
	public List<Integer> getCutPoints() {
		return cutPoints;
	}
	public void setCutPoints(List<Integer> cutPoints) {
		this.cutPoints = cutPoints;
	}
	

}
