package ru.recog.ui;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.ImageUtils;
import ru.recog.LabelFrame;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.nn.NNAnalysis;
import ru.recog.nn.NNWrapper;
import ru.recog.segment.*;

public class SegView {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		NNWrapper nn = new NNWrapper("/Users/pps/AllSegmented/NN/BSS724021.nnet",
				new MultipleFeatureExtractor(new OverlapGradientGridFeatureExtractor()));
		String name = "/Users/pps/dev/newnumbers/frame66401.png";
		Mat m = Imgcodecs.imread(name, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		
		LabelFrame lf = new LabelFrame("Chi", true);
		
		SegmentationResult sr1 = SegmentationFactory.getLegacySegmentation().segment(m);
//		SegmentationResult sr2 = Segmenter.shapesegment(m);
		SegmentationResult sr2 = new SBSegmenter().segment(m, SBSegmenter.BIN_OTSU);

		
		lf.addImage(m, name, 3);
		
		lf.addImage(ImageUtils.drawSegRectangles(m, sr1), nn.getLPString(sr1.getRevisedSegments()), 3);
		for (Mat piece : sr1.getRevisedSegments()) 
			lf.addImage(piece, NNAnalysis.convertNNOutputToString(nn.getNNOutputArray(piece)), 5);
		
		lf.addImage(ImageUtils.drawSegRectangles(m, sr2), nn.getLPString(sr2.getRevisedSegments()), 3);
		for (Mat piece : sr2.getRevisedSegments()) 
			lf.addImage(piece, NNAnalysis.convertNNOutputToString(nn.getNNOutputArray(piece)), 5);
		
		lf.pack();
		lf.setVisible(true);

		
		

	}

}
