package ru.recog.segment;

import org.opencv.core.Mat;

import ru.recog.imgproc.SegmentationResult;

public interface Segmentation {
	
	public SegmentationResult segment(Mat m);
	
	public SegmentationResult segment(Mat m, double...parameters);

}
