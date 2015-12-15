package ru.recog.segment;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.*;
import ru.recog.nn.NNWrapper;
import ru.recog.repo.*;

public class Piece {
	
	private int left;
	private int right;
	
	
	
	
	public static void main(String[] args) {
		LabelFrame lf = new LabelFrame("pieces");
		lf.setSize(800,600);
		lf.setVisible(true);
		
//		NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\NNet6x12_2883521.nnet",
//				new MultipleFeatureExtractor<Mat>(
//			new OverlapGradientGridFeatureExtractor()));
		
		NNWrapper nn = Network.getDefaultWrapper();
		SegLog sl = Repository.getSegLog("seglog047.xml");
		File picdir = Repository.getPlateFolderFile(sl.getPlatesLocation());
		System.out.println("Picdir: "+picdir.getAbsolutePath());
		for (SegLogEntry sle : sl.getEntries()) {
			if (sle.getResult() != ResultEnum.SFAULT) continue;
			System.out.println("File: "+new File(picdir, sle.getFilename()).getAbsolutePath());

			Mat m = Imgcodecs.imread(new File(picdir, sle.getFilename()).getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			SegmentationResult sr = SegmentationFactory.getMarkovSegmentation().segment(m, MarkovSegmentation.ALL_POSSIBLE);
			System.out.println(sr.getPossibleCuts());
			CutData cut = findBestCut(sr, nn);
			if (cut == null) {
				System.out.println("Best cut was null for "+sle.getFilename());
				lf.addImage(m, "Best cut was null for "+sle.getFilename(), 3);
				continue;
			}
			List<Mat> pieces = sr.getRevisedSegments(cut);
			List<Double> probs = nn.probList(pieces);
			double prob = 1;
			for (double d : probs) prob=prob*d;
			lf.addImage(ImageUtils.drawSegRectangles(m, sr, cut),nn.getLPString(pieces)+" "+prob+" "+probs, 3);
			
		}
		
		
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
		
//		int count = 0;
//		for (File f : files) {
//			count++;
////			if (count > 300) break;
//			Mat m = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//			SegmentationResult sr = SegmentationFactory.getMarkovSegmentation().segment(m, 0.1);
//			CutData cut = findBestCut(sr, nn);
//			List<Mat> pieces = sr.getRevisedSegments(cut);
//			List<Double> probs = nn.probList(pieces);
//			double prob = 1;
//			for (double d : probs) prob=prob*d;
//			lf.addImage(ImageUtils.drawSegRectangles(m, sr, cut),nn.getLPString(pieces)+" "+prob+" "+probs, 3);
//			
//		}
	}
	
	public static CutData findBestCut(SegmentationResult sr, NNWrapper nn) {
		double topProb = 0;
		CutData topCut = null;
		for (CutData cut : sr.getPossibleCuts()) {
			List<Mat> pieces = sr.getSegments(cut);  //getRevisedSegments(cut);
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
