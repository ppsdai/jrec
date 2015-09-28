package ru.recog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.imgproc.*;
import ru.recog.nn.*;

public class PlateProcessor extends LabelFrame {
	
	
	private NNWrapper nn;
	private Sequencer sequencer = new Sequencer();
	
	public PlateProcessor(NNWrapper nn)  {
		this.nn = nn;
	}
	
	public RecognitionResult processPlate(Plate plate) {
//		List<SegmentationResult> segList = new ArrayList<SegmentationResult>(plate.getPlateImages().size());
		// segmentation
		List<String> possibleNumbers = new ArrayList<String>();
		for (Mat m : plate.getPlateImages()) {
			SegmentationResult sr = Segmenter.segment(m);
			String possible = nn.getLPString(sr.getRevisedSegments());
			possibleNumbers.add(possible);
			
		
		// feature extraction
		
		// nn OCR
		}
		// sequencing of nn output
		String number = sequencer.doSequence(possibleNumbers);
		RecognitionResult rr = new RecognitionResult();
		rr.setTimestamp(plate.getTimestamp());
		rr.setPlateImages(plate.getPlateImages());
		rr.setNumber(number);
		addRR(rr);
		return rr;
	}
	
	public void addRR(final RecognitionResult rr) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				System.out.println("Hola!");
				String label = rr.getNumber()+" n: "+rr.getPlateImages().size()+" tmpst: "+rr.getTimestamp();
				addImage(rr.getSinglePlateImage(), label, 3);
				repaint();
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
		
//		pp.pack();
		
		
		File dir = new File(args[1]);
		PlateProcessor pp = new PlateProcessor(nn);
		pp.setSize(800, 600);
		pp.setVisible(true);
		
		int total = plates.size();
		int correct = 0; int nomatches = 0;
		
		for (Plate p : plates) {
			RecognitionResult rr = pl.processPlate(p);
			if (rr.getNumber().isEmpty())
				nomatches++;
			else if (!rr.getNumber().contains("*"))
				correct++;
			pp.addRR(rr);
		}
		
		System.out.println("Total: "+total+" correct: "+correct+" no matches: "+nomatches);

	}

}
