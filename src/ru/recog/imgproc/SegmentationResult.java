package ru.recog.imgproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ru.recog.Contours;
import ru.recog.ImageUtils;

public class SegmentationResult {
	
	private Mat originalMat;
	private int upperBound, lowerBound;
	private List<Integer> cutPoints;
	private List<Rect> rectangles = null;
	
	
	
	public List<Mat> getSegments() {
		List<Mat> pieces = new ArrayList<Mat>();
		
		Mat  res = getOriginalMat().rowRange(getRowRange());

		List<Integer> cutPoints = getCutPoints();
		int x0 = 0; int x1 = 0;
		
		for (int i = 0; i <= cutPoints.size(); i++) {
			if (i == cutPoints.size()) x1 = res.cols()-1;
			else x1 = cutPoints.get(i);
			pieces.add(res.colRange(x0, x1+1));
			
			x0 = x1;
		}
		return pieces;
	}
	
	
	public List<Mat> getRevisedSegments() {
		List<Mat> segments = new ArrayList<Mat>();
		for (Rect r : getRevisedRectangles())
			segments.add(getOriginalMat().submat(r.y, r.y+r.height+1, r.x, r.x+r.width+1));
		
		return segments;
	}
	
	public List<Rect> getRevisedRectangles() {
		if (rectangles != null) return rectangles;
		
		rectangles = new ArrayList<Rect>();
		
		Mat  bin = ImageUtils.localbin(getOriginalMat(), 0.6); 
				
		List<Integer> cutPoints = getCutPoints();
		int x0 = 0; int x1 = 0;
		
		
		for (int i = 0; i <= cutPoints.size(); i++) {
			if (i == cutPoints.size()) x1 = bin.cols()-1;
			else x1 = cutPoints.get(i);

			Mat piece = bin.colRange(x0, x1+1);
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(piece.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			
			int topBorder = getUpperBound();
			int bottomBorder = getLowerBound();
			for (MatOfPoint mop : contours) {
				Rect r = Contours.getContourRect(mop);
				if (r.width < 25 && r.height < 30) {
					if (r.y < getUpperBound() && (r.y+r.height) > getUpperBound() && r.y < topBorder)
						topBorder = r.y;
					if (r.y < getLowerBound() && (r.y+r.height) > getLowerBound() && r.y+r.height > bottomBorder)
						bottomBorder = r.y+r.height;
				}
			}
			
			rectangles.add(new Rect(new Point(x0,topBorder), new Point(x1,bottomBorder)));
			
			x0 = x1;
		}
		
		return rectangles;
	}
	
	
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
