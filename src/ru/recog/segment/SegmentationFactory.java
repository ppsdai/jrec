package ru.recog.segment;

public class SegmentationFactory {
	
	
	private static Segmentation legacySegmentation = null;
	
	private static Segmentation markovSegmentation = null;
	
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

}
