package ru.recog.imgproc;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class Plate {

	private List<Long> timeOfRecord = new ArrayList<Long>();
	private List<Rect>    positionRect = new ArrayList<Rect>();
	private List<Mat> plateImage = new ArrayList<Mat>();

	public static final Rect EMPTY_RECT = new Rect(0,0,0,0);
	
	public Plate(long t, Rect r, Mat m) {
		add(t, r, m);
	}
	
	public int getLength()  {
		return timeOfRecord.size();
	}
	
	public long getTimeOfRecord(int n) {
		return timeOfRecord.get(n);
	}
	
	public Rect getPositionRect(int n) {
		return positionRect.get(n);
	}
	
	public Mat getPlateImage(int n) {
		return plateImage.get(n);
	}
	
	public List<Mat> getPlateImages() {
		return plateImage;
	}
	
	public void add(long t, Rect r, Mat m) {
		timeOfRecord.add(t);
		positionRect.add(r);
		plateImage.add(m);
	}
	
	public void add(long t, Mat m) {
		add(t, EMPTY_RECT, m);
	}
	
	public Rect getLastAddedRect()  {
	    return getPositionRect( positionRect.size() - 1 );
	}
	
	public long getLastAddedTime()  {
	    return getTimeOfRecord( timeOfRecord.size() - 1 );
	}
	
	
}
