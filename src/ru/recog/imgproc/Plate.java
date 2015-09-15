package ru.recog.imgproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class Plate {

	private List<Integer> timeOfRecord = new ArrayList<Integer>();
	private List<Rect>    positionRect = new ArrayList<Rect>();
	private List<Mat> plateImage = new ArrayList<Mat>();
	//Mat newM = image.submat(rect);
	
	public Plate(int t, Rect r, Mat m) {
		add(t, r, m);
	}
	
	public int getLength()  {
		return timeOfRecord.size();
	}
	
	public Integer getTimeOfRecord(int n) {
		return timeOfRecord.get(n);
	}
	
	public Rect getPositionRect(int n) {
		return positionRect.get(n);
	}
	
	public Mat getPlateImage(int n) {
		return plateImage.get(n);
	}
	
	public void add(int t, Rect r, Mat m) {
		timeOfRecord.add(t);
		positionRect.add(r);
		plateImage.add(m);
	}
	
	public Rect getLastAddedRect()  {
	    return getPositionRect( positionRect.size() - 1 );
	}
	
	public int getLastAddedTime()  {
	    return getTimeOfRecord( timeOfRecord.size() - 1 );
	}
	
	
}
