package ru.recog.segment;

import java.util.List;

import org.opencv.core.Mat;

import ru.recog.imgproc.SegmentationResult;

public interface Segmentation {
	
	public List<? extends SegmentationResult> segment(Mat m);
	
	public List<? extends SegmentationResult> segment(Mat m, double...parameters);

}
