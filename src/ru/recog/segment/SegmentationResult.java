package ru.recog.segment;

import java.util.*;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import ru.recog.ImageUtils;

public class SegmentationResult {
	
	private SegmentationData data;
	
	private List<CutData> possibleCuts;
	
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
	
	public List<CutData> getPossibleCuts(int numberOfCuts) {
		return possibleCuts.subList(0, Math.min(possibleCuts.size(),numberOfCuts));
	}

	void setPossibleCuts(List<CutData> possibleCuts) {
		this.possibleCuts = possibleCuts;
	}

	public SegmentationData getData() {
		return data;
	}

	public void setData(SegmentationData data) {
		this.data = data;
	}
	
	public List<Mat> getSegments(CutData cut) {
		List<Mat> pieces = new ArrayList<Mat>();
		
		Mat  res = getOriginalMat().rowRange(data.getUpperBound(), data.getLowerBound()+1);

		List<Integer> cutPoints = cut.getCutPoints();
		int x0 = 0; int x1 = 0;
		
		for (int i = 0; i <= cutPoints.size(); i++) {
			if (i == cutPoints.size()) x1 = res.cols()-1;
			else x1 = cutPoints.get(i);
			pieces.add(res.colRange(x0, x1+1));
			
			x0 = x1;
		}
		return pieces;
	}
	
	
	public List<Mat> getRevisedSegments(CutData cut) {
		List<Mat> segments = new ArrayList<Mat>();
		for (Rect r : getRevisedRectangles(cut))
			segments.add(getOriginalMat().submat(r.y, r.y+r.height+1, r.x, r.x+r.width+1));
		
		return segments;
	}
	
	public List<Mat> getRevisedSegments() {
		List<Mat> segments = new ArrayList<Mat>();
		for (Rect r : getRevisedRectangles())
			segments.add(getOriginalMat().submat(r.y, r.y+r.height+1, r.x, r.x+r.width+1));
		
		return segments;
	}
	
	public List<Rect> getRevisedRectangles(CutData cut) {
		
		List<Rect> rectangles = new ArrayList<Rect>();
		Mat bin = SegmentationFactory.getDefaultBinarization().processImage(data.getOriginalMat());
				
		List<Integer> cutPoints = cut.getCutPoints();
//		int x0 = 0; int x1 = 0;
//		
//		
//		for (int i = 0; i <= cutPoints.size(); i++) {
//			if (i == cutPoints.size()) x1 = bin.cols()-1;
//			else x1 = cutPoints.get(i);
//
//			Mat piece = bin.colRange(x0, x1+1);
//			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//			Imgproc.findContours(piece.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//			
//			int topBorder = data.getUpperBound();
//			int bottomBorder = data.getLowerBound();
//			for (MatOfPoint mop : contours) {
//				Rect r = ImageUtils.getContourRect(mop);
//				if (r.width < 25 && r.height < 30) {
//					if (r.y < data.getUpperBound() && (r.y+r.height) > data.getUpperBound() && r.y < topBorder)
//						topBorder = r.y;
//					if (r.y < data.getLowerBound() && (r.y+r.height) > data.getLowerBound() && r.y+r.height > bottomBorder)
//						bottomBorder = r.y+r.height;
//				}
//			}
//			
//			rectangles.add(new Rect(new Point(x0,topBorder), new Point(x1,bottomBorder)));
//			
//			x0 = x1;
//		}
		
		for (int i = 0; i < cutPoints.size() - 1; i++) {
			int x0 = cutPoints.get(i);
			int x1 = cutPoints.get(i+1);
			Mat piece = bin.colRange(x0, x1+1);
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(piece.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			
			int topBorder = data.getUpperBound();
			int bottomBorder = data.getLowerBound();
			for (MatOfPoint mop : contours) {
				//FIXME there should be more reasonable approach to filtering resulting shape rectangles
				Rect r = ImageUtils.getContourRect(mop);
				if (r.width < 25 && r.height < 30) {
					if (r.y < data.getUpperBound() && (r.y+r.height) > data.getUpperBound() && r.y < topBorder)
						topBorder = r.y;
					if (r.y < data.getLowerBound() && (r.y+r.height) > data.getLowerBound() && r.y+r.height > bottomBorder)
						bottomBorder = r.y+r.height;
				}
			}
			
			rectangles.add(new Rect(new Point(x0,topBorder), new Point(x1,bottomBorder)));
		}
		
		return rectangles;
	}

	
	public List<Rect> getRevisedRectangles() {
		if (possibleCuts.isEmpty()) return Collections.emptyList();
		return getRevisedRectangles(possibleCuts.get(0));
	}
	
	public Mat getOriginalMat() {
		return data.getOriginalMat();
	}
	
	public List<Integer> getCutPoints() {
		if (possibleCuts.isEmpty()) return Collections.emptyList();
		return possibleCuts.get(0).getCutPoints();
	}

}
