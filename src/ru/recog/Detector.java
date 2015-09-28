package ru.recog;

import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;

public class Detector {
	
	
	private CascadeClassifier classifier;
	
	private double step; // used in scaling scanning windows in classifier
	private Size minSize, maxSize;

	
	public Detector() {
		this(Utils.CASCADE_LPR_PATH, new Size(30,10), new Size(120,40), 0.05);
	}
	
	public Detector(String clPath, Size minSize, Size maxSize, double step) {
		System.out.println("Detector. Cascade path: "+clPath);
		classifier = new CascadeClassifier(clPath);
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.step = step;
	}
	
	public MatOfRect detect(Mat image) {
		MatOfRect detections = new MatOfRect();
		classifier.detectMultiScale(image, detections, 1+step, 3, 0, minSize, maxSize);
		return detections;
		
	}
	
	public static void main(String[] args) {
		
	}

}
