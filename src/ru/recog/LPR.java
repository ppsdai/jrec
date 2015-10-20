package ru.recog;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.imgproc.*;
import ru.recog.nn.NNWrapper;

public class LPR {
	
	// detector
	// segmentation
	// neural network
	// plate aggregation
	// feature extraction
	
	
//	private Segmenter segmenter;
	private Sequencer sequencer;
	private NNWrapper nn;
	
	
	public LPR( Sequencer seq, NNWrapper nn) {
//		segmenter = seg;
		sequencer = seq;
		this.nn = nn;
	}
	
	
	public static void main(String[] args) {
//		List<Plate> plates = AggregatePlates.readFormattedFolder("/Users/pps/dev/aggr");
//		LPR lpr = new LPR(new Sequencer(), new NNWrapper("/Users/pps/AllSegmented/NN/BSS724021.nnet",
//				new MultipleFeatureExtractor(new OverlapGradientGridFeatureExtractor())));
//		System.out.println(lpr.processPlates(plates));
		Detector d = new Detector();
		NNWrapper nn = new NNWrapper("/Users/pps/AllSegmented/NN/BSS724021.nnet",
				new MultipleFeatureExtractor(new OverlapGradientGridFeatureExtractor()));
		Aggregator ag = new Aggregator(d);
		PlateProcessor pp = new PlateProcessor(nn);
		ag.setPlateProcessor(pp);
		
		pp.setPreferredSize(new Dimension(800,600));
		pp.setSize(800, 600);
		pp.setVisible(true);
		
		BlockingQueue<Mat> queue = new LinkedBlockingQueue<Mat>();
		FrameProducer producer = new FrameProducer("/Users/pps/dev/vid/video-046.avi", queue);
//		FrameProducer producer = new FrameProducer("/Users/pps/dev/vid/video_ador7_92_20150807_09-30.avi", queue);

		Thread t1 = new Thread(producer);
		t1.start();
		
		ag.setQueue(queue);
		Thread t2 = new Thread(ag);
		t2.start();
		
		
	}
	

}
