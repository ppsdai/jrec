package ru.recog;

import java.io.File;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
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
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Detector d = new Detector(Utils.CASCADE_LPR_PATH, new Size(30,10), new Size(360,120), 0.05);
		File dest = new File(args[1]);
		for (File f : Utils.getOrderedList(args[0])) {
			Mat m = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
			MatOfRect mr = d.detect(m);
			System.out.println(mr);
			if (!mr.empty()) {
				for (Rect r : mr.toArray())
					Imgproc.rectangle(m, r.tl(), r.br(), new Scalar(0,255,0));
				Imgcodecs.imwrite(Utils.fullPath(dest, f.getName()),m);
			}
		}
			
	}

}
