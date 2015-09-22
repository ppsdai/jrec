package ru.recog;

import java.util.List;

import org.opencv.core.Mat;

public class RecognitionResult {
	
	private String number;
	private List<Mat> plateImages;
	private long timestamp;
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public List<Mat> getPlateImages() {
		return plateImages;
	}
	public void setPlateImages(List<Mat> plateImages) {
		this.plateImages = plateImages;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
