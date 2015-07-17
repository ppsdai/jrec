package ru.recog.imgproc;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Binarization implements ImageProcessor {
	
	private double threshold;
	private double maxValue;
	private int binType;
	
	
	public Binarization(double threshold, double maxValue, int binType) {
		this.threshold = threshold;
		this.maxValue = maxValue;
		this.binType = binType;
	}


	@Override
	public Mat processImage(Mat m) {
		Mat pm = new Mat(m.size(), m.type());
		Imgproc.threshold(m, pm, threshold, maxValue, binType);
		return pm;

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Binarization[");
		if (Imgproc.THRESH_BINARY == binType) 
			sb.append("bin,");
		else if (Imgproc.THRESH_BINARY_INV == binType)
			sb.append("inv,");
		else if (Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU == binType)
			sb.append("invOtsu,");
		else if (Imgproc.THRESH_OTSU == binType)
			sb.append("Otsu,");
		else
			sb.append(binType).append(",");
		sb.append("threshold=").append(threshold).append(",maxValue=").append(maxValue).append("]");
		return sb.toString();
	}
	
}