package ru.recog.imgproc;

import org.opencv.core.Mat;

import ru.recog.ImageUtils;

public class LocalBinarization implements ImageProcessor {
	
	
	private double threshold;
	
	
	public LocalBinarization() {
		this(0.6);
	}
	
	public LocalBinarization(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public Mat processImage(Mat m) {
		return ImageUtils.localbin(m, threshold);
	}

}
