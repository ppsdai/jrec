package ru.recog.segment;

import java.util.*;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ru.recog.Contours;
import ru.recog.ImageUtils;
import ru.recog.imgproc.BinShape;

public class SegmentationResult {
	
	private Mat originalMat;
	private int upperBound, lowerBound;
	private int leftPoint, rightPoint;      // estimates of the number boundaries
	private List<Integer> cutPoints;
	
	private int lengthEstimate = 0;
	private int centerLine = 0;
	private MatOfInt intensity;
	
	private SegmentationData data;
	
	private List<CutData> possibleCuts;
	
	@Deprecated
	public SegmentationResult() {}
	
	public SegmentationResult(SegmentationData data, List<CutData> possibleCuts) {
		this.data = data;
		this.possibleCuts = possibleCuts;
	}
	
	public SegmentationResult(SegmentationData data, CutData cut) {
		this(data, Collections.singletonList(cut));
	}
	
	public List<CutData> getPossibleCuts() {
		return possibleCuts;
	}


	public void setPossibleCuts(List<CutData> possibleCuts) {
		this.possibleCuts = possibleCuts;
	}


	public SegmentationData getData() {
		return data;
	}


	public void setData(SegmentationData data) {
		this.data = data;
	}
	public List<BinShape> shapes;
	
	public MatOfInt getIntensity() {
		return intensity;
	}


	public void setIntensity(MatOfInt intensity) {
		this.intensity = intensity;
	}
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
		//FIXME binarization parameters should be parameterized and uniform across whole segmentation process
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
		return possibleCuts.get(0).getCutPoints();
	}
	@Deprecated
	public void setCutPoints(List<Integer> cutPoints) {
		setPossibleCuts(Collections.singletonList(new CutData(cutPoints)));
//		this.cutPoints = cutPoints;
	}
	public int getLengthEstimate() {
		return lengthEstimate;
	}
	public void setLengthEstimate(int lengthEstimate) {
		this.lengthEstimate = lengthEstimate;
	}

	public int getCenterLine() {
		return centerLine;
	}
	public void setCenterLine(int CenterLine) {
		this.centerLine = CenterLine;
	}
}
