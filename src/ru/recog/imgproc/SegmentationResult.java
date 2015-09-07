package ru.recog.imgproc;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Range;

public class SegmentationResult {
	
	private Mat originalMat;
	private int upperBound, lowerBound;
	private int leftPoint, rightPoint;      // estimates of the number boundaries
	private List<Integer> cutPoints;
	private int lengthEstimate = 0;
	private int CenterLine = 0;
	
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
	public int getleftPoint() {
		return leftPoint;
	}
	public void setleftPoint(int leftPoint) {
		this.leftPoint = leftPoint;
	}
	public int getrightPoint() {
		return rightPoint;
	}
	public void setrightPoint(int rightPoint) {
		this.rightPoint = rightPoint;
	}
	public List<Integer> getCutPoints() {
		return cutPoints;
	}
	public void setCutPoints(List<Integer> cutPoints) {
		this.cutPoints = cutPoints;
	}
	public int getlengthEstimate() {
		return lengthEstimate;
	}
	public void setlengthEstimate(int lengthEstimate) {
		this.lengthEstimate = lengthEstimate;
	}

	public int getCenterLine() {
		return CenterLine;
	}
	public void setCenterLine(int CenterLine) {
		this.CenterLine = CenterLine;
	}
}
