package ru.recog.segment;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.*;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.nn.NNWrapper;

public class Piece {
	
	private int left;
	private int right;
	
	
	
	
	public static void main(String[] args) {
		List<File> files = Utils.getOrderedList("C:\\dev\\frames\\VNew\\detected1411");
		LabelFrame lf = new LabelFrame("pieces");
		lf.setSize(800,600);
		lf.setVisible(true);
		
		NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\NNet6x12_2883521.nnet",
				new MultipleFeatureExtractor<Mat>(
			new OverlapGradientGridFeatureExtractor()));
		
//		int count = 0;
//		for (File f : files) {
//			count++;
//			if (count > 10) break;
//			Mat m = Imgcodecs.imread("/Users/pps/dev/aggr/V046N4t3880.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//			SegmentationResult sr = SegmentationFactory.getMarkovSegmentation().segment(m, 0.1);
//			System.out.println(sr.getPossibleCuts());
//			for (CutData cut : sr.getPossibleCuts()) {
//				List<Mat> pieces = sr.getRevisedSegments(cut);
//				List<Double> probs = nn.probList(pieces);
//				double prob = 1;
//				for (double d : probs) prob=prob*d;
//				lf.addImage(ImageUtils.drawSegRectangles(m, sr, cut),nn.getLPString(pieces)+" "+prob+" "+probs, 3);
//			}
////		}
		
		int count = 0;
		for (File f : files) {
			count++;
//			if (count > 300) break;
			Mat m = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			SegmentationResult sr = SegmentationFactory.getMarkovSegmentation().segment(m, 0.1);
			CutData cut = findBestCut(sr, nn);
			List<Mat> pieces = sr.getRevisedSegments(cut);
			List<Double> probs = nn.probList(pieces);
			double prob = 1;
			for (double d : probs) prob=prob*d;
			lf.addImage(ImageUtils.drawSegRectangles(m, sr, cut),nn.getLPString(pieces)+" "+prob+" "+probs, 3);
			
		}
	}
	
	public static CutData findBestCut(SegmentationResult sr, NNWrapper nn) {
		double topProb = 0;
		CutData topCut = null;
		for (CutData cut : sr.getPossibleCuts()) {
			List<Mat> pieces = sr.getRevisedSegments(cut);
			List<Double> probs = nn.probList(pieces);
			double prob = 1;
			for (double d : probs) prob=prob*d;
			if (prob>topProb) {
				topProb = prob;
				topCut = cut;
			}
		}
		return topCut;
		
	}

}
