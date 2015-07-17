package ru.recog.imgproc;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ErodingDilator implements ImageProcessor {

	
	private int morphologyShape;
	private Size size;
	private int iterations;
	
	public ErodingDilator(int morphologyShape, Size size, int iterations) {
		this.morphologyShape = morphologyShape;
		this.size = size;
		this.iterations = iterations;
	}
	
	public Mat processImage(Mat m) {
		//TODO what's up with Point parameter?
		Mat morphMat = Imgproc.getStructuringElement(morphologyShape, size);

		Mat eroded = new Mat(), dilated = new Mat();
		Imgproc.erode(m, eroded, morphMat, new Point(-1,-1), iterations);
		
		Imgproc.dilate(eroded, dilated, morphMat, new Point(-1,-1), iterations);

		return dilated;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Erodingdilator[");
		if (Imgproc.MORPH_CROSS == morphologyShape) 
			sb.append("cross,");
		else if (Imgproc.MORPH_ELLIPSE == morphologyShape)
			sb.append("ellipse,");
		else
			sb.append(morphologyShape).append(",");
		sb.append(size.toString()).append(",iterations=").append(iterations).append("]");
		return sb.toString();
	}

}
