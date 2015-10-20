package ru.recog.segment;

import ru.recog.imgproc.ImageProcessor;
import ru.recog.imgproc.LocalBinarization;

public class SegmentationFactory {
	
	private static Segmentation legacySegmentation = null;
	
	private static Segmentation markovSegmentation = null;
	
	private static Segmentation shapeSegmentation = null;
	
	private static ImageProcessor defaultBinarization = null;
	
	public static Segmentation getLegacySegmentation() {
		if (legacySegmentation == null) 
			legacySegmentation = new LegacySegmentation();
		return legacySegmentation;
	}
	
	public static Segmentation getMarkovSegmentation() {
		if (markovSegmentation == null) 
			markovSegmentation = new MarkovSegmentation();
		return markovSegmentation;
	}
	
	public static Segmentation getShapeSegmentation() {
		if (shapeSegmentation == null) 
			shapeSegmentation = new SBSegmenter();
		return shapeSegmentation;
	}
	
	public static ImageProcessor getDefaultBinarization() {
		if (defaultBinarization == null)
			defaultBinarization = new LocalBinarization(0.6);
		return defaultBinarization;
	}
	
	


}
