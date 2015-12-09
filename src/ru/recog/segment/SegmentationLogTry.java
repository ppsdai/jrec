

package ru.recog.segment;

import java.io.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.*;
import ru.recog.feature.MultipleFeatureExtractor;
import ru.recog.feature.OverlapGradientGridFeatureExtractor;
import ru.recog.imgproc.ShapeFilter;
import ru.recog.nn.NNAnalysis;
import ru.recog.nn.NNWrapper;
import ru.recog.ui.FrameProcessor;

public class SegmentationLogTry {
	

	public static void generateNegatives(String picFolder, String seglogFilename) throws Exception {
		
		
		//NNWrapper nn = new NNWrapper("/Users/pps/AllSegmented/NN/BSS724021.nnet",
		NNWrapper nn = new NNWrapper("C:\\dev\\frames\\AllSegmented\\NN\\BSS724021.nnet",
				new MultipleFeatureExtractor(
			new OverlapGradientGridFeatureExtractor()));
		
		LabelFrame lf = new LabelFrame(picFolder);
		
		File picDir = new File(picFolder);
		if (!picDir.exists() || !picDir.isDirectory())
			throw new IllegalArgumentException("Not a folder: "+picFolder);
		List<SegmentationLogEntry> entries = SegmentationLog.readSegmentationLog(seglogFilename);// readSegmentationLog(seglogFilename);
		LegacySegmentation ls = new LegacySegmentation();
		int count = 0;
		int total = 0;
		int wrong = 0;
		for (SegmentationLogEntry entry : entries) {
			count++;
			if (count > 4000) break;
			if (!entry.getResult().equals("SUCCESS")) continue;
			
			String name = entry.getFilename().substring(entry.getFilename().lastIndexOf("\\")+1);

			Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name), 
					Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//			SegmentationResult markov = SegmentationFactory.getMarkovSegmentation().segment(m);
//			SegmentationResult legacy = SegmentationFactory.getLegacySegmentation().segment(m);

			
		

			
			List<Mat> pieces = new ArrayList<>();
			int avLength = 0;
			for (Rect r : entry.getRectangles()) {
 				pieces.add(m.submat(r.y, r.y+r.height+1,r.x,r.x+r.width+1));
			    avLength = avLength + r.width;
			}
			avLength = avLength / 6;
			
			// get more pieces
			String strNew;
			List<Mat> piecesNew = new ArrayList<>();
			Rect r0 =  entry.getRectangles().get(0);
 			
			if ( ( r0.x - avLength ) >= 0 ) 
			{ 
			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x - avLength,r0.x));
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(0));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(0)) || (strNew.charAt(0) == '1')) 
 				piecesNew.remove(piecesNew.size()-1);
 			
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x ,r0.x +r0.width/2+1));
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(0));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(0)) || (strNew.charAt(0) == '1'))  
 				piecesNew.remove(piecesNew.size()-1);
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x + r0.width/2 ,r0.x + r0.width+1));	
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(0));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(0)) || (strNew.charAt(0) == '1'))  
 				piecesNew.remove(piecesNew.size()-1);
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x + r0.width/2 ,r0.x + r0.width/2 + avLength +1));
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(0));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(0)) || (strNew.charAt(0) == '1'))  
 				piecesNew.remove(piecesNew.size()-1);
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x - avLength/2 ,r0.x + avLength/2 +1));
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(0));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(0)) || (strNew.charAt(0) == '1'))  
 				piecesNew.remove(piecesNew.size()-1);
			
			}
 // another one			
 			r0 =  entry.getRectangles().get(5);
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x ,r0.x +r0.width/2+1));
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(5));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(5)) || (strNew.charAt(0) == '1'))  
 				piecesNew.remove(piecesNew.size()-1);
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x + r0.width/2 ,r0.x + r0.width+1));	
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(5));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(5)) || (strNew.charAt(0) == '1')) 
 				piecesNew.remove(piecesNew.size()-1);
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x + r0.width/2 - avLength,r0.x + r0.width/2));
 			
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(5));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(5)) || (strNew.charAt(0) == '1')) 
 				piecesNew.remove(piecesNew.size()-1);
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x - avLength ,r0.x + r0.width/2 +1));
 			
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));
 			System.out.println(strNew + " " + entry.getPlate().charAt(5));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(5)) || (strNew.charAt(0) == '1')) 
 				piecesNew.remove(piecesNew.size()-1);
 			
 			piecesNew.add(m.submat(r0.y, r0.y+r0.height+1,r0.x + r0.width ,r0.x + + r0.width + avLength +1));
 			
 			strNew = nn.getLPString(piecesNew.subList( piecesNew.size() - 1  , piecesNew.size() ));;
 			System.out.println(strNew + " " + entry.getPlate().charAt(5));
 			if ((strNew.charAt(0) == entry.getPlate().charAt(5)) || (strNew.charAt(0) == '1')) 
 				piecesNew.remove(piecesNew.size()-1);
 			
 			//name.
 			//System.out.println(name);
 			//System.out.println(name.substring(0, name.lastIndexOf(".")));
			String fname = "c:\\dev\\Negatives\\";
			for ( int i=0; i< piecesNew.size(); i++) {
			  String filename = fname.concat(name.substring(0, name.lastIndexOf("."))).concat(String.valueOf(i)).concat(".bmp");
			  Imgcodecs.imwrite(filename  , piecesNew.get(i));
			}
			
			String s = nn.getLPString(pieces);
			for (int i = 0; i < s.length(); i++) {
				total++;
				if ('*' == s.charAt(i)) wrong++;
			}
			
			lf.addImage(m, nn.getLPString(pieces)+" MAIN", 4);
			SegmentationResult markov = SegmentationFactory.getMarkovSegmentation().segment(m,0.1);
			CutData cut = Piece.findBestCut(markov, nn);
			List<Mat> mpieces = markov.getRevisedSegments(cut);
			List<Double> probs = nn.probList(mpieces);
			double prob = 1;
			for (double d : probs) prob=prob*d;
			lf.addImage(ImageUtils.drawSegRectangles(m, markov, cut),nn.getLPString(mpieces)+" "+prob+" "+probs, 3);
//			for (CutData cut : markov.getPossibleCuts()) {
//				lf.addImage(ImageUtils.drawSegLines(m, cut), nn.getLPString(markov.getRevisedSegments(cut)), 3);
//			}

			
//			for (Mat piece : pieces)
//				lf.addImage(piece, nn.getNNOutput(piece).toString(), 3);
//			for (CutData cut : markov.getPossibleCuts(3)) 
//				lf.addImage(ImageUtils.drawSegLines(m, cut), nn.getLPString(markov.getRevisedSegments(cut)), 3);
			

		}
		
		lf.pack();
		lf.setVisible(true);
		
		System.out.println("Total: "+total+" wrong "+wrong+" % "+(double)wrong/total);
	}
	

	
}
