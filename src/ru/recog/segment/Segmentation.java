package ru.recog.segment;

import org.opencv.core.Mat;

public interface Segmentation {
	
	public SegmentationResult segment(Mat m);
	
	public SegmentationResult segment(Mat m, double...parameters);
	
	public SegmentationResult segment(SegmentationData data);
	
	public SegmentationResult segment(SegmentationData data, double...parameters);

}
