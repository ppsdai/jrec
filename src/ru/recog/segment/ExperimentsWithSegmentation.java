package ru.recog.segment;

import java.util.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.nn.NNWrapper;

public class ExperimentsWithSegmentation {

	
	public static void main(String[] args) throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\NNet6x12_2883521.nnet",
				new MultipleFeatureExtractor<Mat>(
			new OverlapGradientGridFeatureExtractor(7,13)));
		// C:\dev\frames\VNew\detected1411\V1411N33t50680.png
//		Mat m = Imgcodecs.imread("c:\\dev\\frames\\VNew\\detectedTry\\V1411N0t2320X495Y558.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Mat m = Imgcodecs.imread("c:\\dev\\frames\\VNew\\detected76635\\V76635N33t294480.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		//Mat m = Imgcodecs.imread("c:\\dev\\frames\\VNew\\detected79230\\V79230N86t397640.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		
//		SegmentationResult result = SegmentationFactory.getMarkovSegmentation().segment(m, USE_WIDTH, 13.3, 16, 38); 
		//SegmentationResult result = SegmentationFactory.getMarkovSegmentation().segment(m, USE_WIDTH, 10);
		//SegmentationResult result = SegmentationFactory.getMarkovSegmentation().segment(m, USE_WIDTH, 11.6);
		
//		LabelFrame lf = ImageUtils.showAllSegmentations(result, 3);
//		LabelFrame lf = ImageUtils.showAllProbSegmentations(result, 3);
//		CutData cd = Piece.findBestCut(result, nn);
//		lf.addImage(ImageUtils.drawSegLines(m, cd), "NN", 3);
//		lf.setVisible(true);
		
	}

	/*
	public SegmentationResult segment(Mat m, double...parameters) {
		
		if (USE_WIDTH == parameters[0]) {
			double width = parameters[1];
			int ub = (int) Math.round(parameters[2]);
			int lb = (int) Math.round(parameters[3]);
			
			SegmentationData data = new SegmentationData(m, ub, lb);

	}
	*/
	
	
}
