package ru.recog.imgproc;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Resizer implements ImageProcessor {
	
	private Size size;
	
	public Resizer(Size size) {
		this.size = size;
	}

	@Override
	public Mat processImage(Mat m) {
		Mat pm = new Mat(size, m.type());
		Imgproc.resize(m, pm, size, 0, 0, Imgproc.INTER_AREA);
		return pm;
	}
	
	@Override
	public String toString() {
		return "Resizer[".concat(size.toString()).concat("]");
	}

}
