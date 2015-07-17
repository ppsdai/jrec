package ru.recog.imgproc;

import org.opencv.core.Mat;

import ru.recog.ImageUtils;

public class Cropper implements ImageProcessor {
	
//	private Size size;
//	
//	public Cropper(Size size) {
//		this.size = size;
//	}

	@Override
	public Mat processImage(Mat m) {
		return ImageUtils.crop(m);
	}

}
