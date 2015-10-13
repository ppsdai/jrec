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
	
	
	public List<String> processPlates(List<Plate> plates) {
		List<String> numbers = new ArrayList<String>();
//		NNOutput nno = new NNOutput(nn);
		for (Plate p : plates) {
			try {
			List<String> candidates = new ArrayList<String>();
			for (Mat m : p.getPlateImages()) 
				candidates.add(nn.getLPString(Segmenter.segment(m).getSegments()));
			
			String s;
				s = sequencer.doSequence(candidates);
				numbers.add(s);
				System.out.println("Plate: "+p.getLength()+" :"+s);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//				System.out.println("TRY: "+candidates);
			}
			
			
		}
		return numbers;
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
		FrameProducer producer = new FrameProducer("/Users/pps/dev/vid/video_ador7_92_20150807_09-30.avi", queue);
		Thread t1 = new Thread(producer);
		t1.start();
		
		ag.setQueue(queue);
		Thread t2 = new Thread(ag);
		t2.start();
		
		
	}
	

}
