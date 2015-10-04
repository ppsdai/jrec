package ru.recog;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.imgproc.*;
import ru.recog.nn.NNWrapper;

public class PlateProcessor extends LabelFrame {
	
	
	private NNWrapper nn;
	private Sequencer sequencer = new Sequencer();
	private JLabel statusBar;// = new JLabel()
	private long t0;
	private volatile long total, correct, partial;
	
	public PlateProcessor(NNWrapper nn)  {
		super("smee",false);
		t0 = System.nanoTime();
		total = 0;
		correct = 0;
		partial = 0;
		statusBar = new JLabel("Status:");
		statusBar.setBorder(new LineBorder(Color.black, 1));
		add(statusBar, BorderLayout.SOUTH);
		this.nn = nn;
	}
	
	public RecognitionResult processPlate(Plate plate) {
		List<String> possibleNumbers = new ArrayList<String>();
//		List<String> pn = new ArrayList<String>();
		for (Mat m : plate.getPlateImages()) {
			SegmentationResult sr = Segmenter.segment(m);
			SegmentationResult sr1 = Segmenter.shapesegment(m);
			String possible = nn.getLPString(sr.getRevisedSegments());
			possibleNumbers.add(possible);
			possibleNumbers.add(nn.getLPString(sr1.getRevisedSegments()));
//			pn.add(nn.getLPString(sr1.getRevisedSegments()));
			
			
			
		
		// feature extraction
		
		// nn OCR
		}
		// sequencing of nn output
		String number = sequencer.doSequence(possibleNumbers);
//		String n2 = sequencer.doSequence(pn);
		total++;
		if ( ( number != "")  && !(number.contains("*")) )
			correct++;
		else if ( ( number != "") )
			partial++;
		
		RecognitionResult rr = new RecognitionResult();
//		rr.setTimestamp(plate.getTimestamp());
		rr.setTimestamp(System.nanoTime());
		rr.setPlateImages(plate.getPlateImages());
		rr.setNumber(number);
		addRR(rr);
		
//		RecognitionResult rr2 = new RecognitionResult();
////		rr.setTimestamp(plate.getTimestamp());
//		rr2.setTimestamp(System.nanoTime());
//		rr2.setPlateImages(plate.getPlateImages());
//		rr2.setNumber(n2);
//		addRR(rr2);

		return rr;
	}
	
	public void addRR(final RecognitionResult rr) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String label = rr.getNumber()+" n: "+rr.getPlateImages().size()+" tmpst: "
			+(rr.getTimestamp()-t0)/1000000;
				addImage(rr.getSinglePlateImage(), label, 3);
				statusBar.setText("Total: "+total+" Correct: "+correct+" Partial: "+partial);
				validate();
			}
		});
	}

	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		if (args.length < 2) {
			System.out.println("Usage: PlateProcessor nnFile plateSourceFolder");
		}
		
		NNWrapper nn = new NNWrapper(args[0],
		new MultipleFeatureExtractor(
				new OverlapGradientGridFeatureExtractor()));
		
		PlateProcessor pl = new PlateProcessor(nn);
		
		List<Plate> plates = AggregatePlates.readFormattedFolder(args[1]);
		
		
//		File dir = new File(args[1]);
		PlateProcessor pp = new PlateProcessor(nn);
		pp.setSize(800, 600);
		pp.setPreferredSize(new Dimension(800, 600));

		pp.setVisible(true);
		
		for (Plate p : plates)
			pp.processPlate(p);
		
//		int total = plates.size();
//		int correct = 0; int nomatches = 0;
//		
//		for (Plate p : plates) {
//			RecognitionResult rr = pl.processPlate(p);
//			if (rr.getNumber().isEmpty())
//				nomatches++;
//			else if (!rr.getNumber().contains("*"))
//				correct++;
//			pp.addRR(rr);
//		}
//		
//		System.out.println("Total: "+total+" correct: "+correct+" no matches: "+nomatches);

	}

}
