package ru.recog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.feature.*;
import ru.recog.imgproc.*;
import ru.recog.nn.NNAnalysis;
import ru.recog.nn.NNWrapper;
import ru.recog.segment.SegmentationFactory;
import ru.recog.segment.SegmentationResult;

public class PlateProcessorTest {
	
	
	private NNWrapper nn;
	private CompoundImageProcessor cip;
//	private MultipleFeatureExtractor mfx;
	
	public PlateProcessorTest(NNWrapper nn, CompoundImageProcessor cip) {
		this.nn = nn;
		this.cip = cip;
//		this.mfx = mfx;
	}
	
	
	public static String nnOutputToSymbol_test(double[] nnoutput) {
		StringBuffer sb = new StringBuffer("");
		int found = 0;
		char ch = 0;
		for (int i = 0; i < nnoutput.length; i ++) {
			if (nnoutput[i]> 0.9) {
				found++;
				ch = NNAnalysis.getChar(i);
			}
		}
		if (found == 1 )
			sb.append(ch);
		else sb.append("*");
		
		return sb.toString();
	}
	
	public String getLPString(List<Mat> pieces) {
		StringBuilder sb = new StringBuilder();
		
		for (Mat piece : pieces) {
//			Mat proc = cip.processImage(piece);
			Mat proc  = piece.clone();
			
//			Mat scaled = ImageUtils.scaleUp(proc, 3);
			sb.append(nnOutputToSymbol_test(nn.getNNOutputArray(proc)));
//			lf.addImage(scaled, NNAnalysis.convertNNOutputToString(nn.getNNOutputArray(scaled)),1);
		}
		
		return sb.toString();
	}

	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		if (args.length < 2) {
			System.out.println("Usage: PlateProcessor nnFile plateSourceFolder");
		}
		
//		NNWrapper nn = new NNWrapper(args[0],
//				new MultipleFeatureExtractor(new AreaFeatureExtractor(),
//						new GravityGridFeatureExtractor(4, 7),
//						new SymmetryFeatureExtractor(),
//						new EdgeIntersectionFeatureExtractor(3, 3)));
		
		NNWrapper nn = new NNWrapper(args[0],
		new MultipleFeatureExtractor(
				new OverlapGradientGridFeatureExtractor()));
		
		
		
		CompoundImageProcessor cip = new CompoundImageProcessor();
//		cip.addImageProcessor(new Binarization(40, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU));
//		cip.addImageProcessor(new Cropper());
		
		PlateProcessorTest pl = new PlateProcessorTest(nn, cip);
		
		
		List<Plate>  lOfPlates = AggregatePlates.readFormattedFolder(args[1]);
		int countRecog = 0;
		int partiallyRecog = 0;
		for (Plate pn : lOfPlates)
		{
			List<String> plateSymbols = new ArrayList<String>();
			Sequencer seq = new Sequencer();
			System.out.println("N "+lOfPlates.indexOf(pn));
			for (int xx = 0; xx < pn.getLength(); xx++ )
			{
				
				Mat m = pn.getPlateImage(xx);
				try {
					SegmentationResult result = SegmentationFactory.getLegacySegmentation().segment(m);
					
					List<Mat> pieces = result.getRevisedSegments(); //FIXME
					

					System.out.println(pl.getLPString(pieces));
					plateSymbols.add(pl.getLPString(pieces));
					
				} catch (ArrayIndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	     
			}
			
			System.out.println("Time "+ pn.getTimeOfRecord(0));
			String resString = seq.doSequence(plateSymbols);
			System.out.println("Sequenced: "+resString);
			//if ( resString != "")
			//	countRecog++;

			if ( ( resString != "")  && !(resString.contains("*")) )
				countRecog++;
			
			if ( ( resString != "") )
				partiallyRecog++;

		
		}
		System.out.println("Recognised N= : "+ countRecog);
		System.out.println("Partially recog + recog N= : "+ partiallyRecog);
	}

}
