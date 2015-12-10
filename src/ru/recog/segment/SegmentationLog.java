package ru.recog.segment;

import java.io.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ru.recog.*;
import ru.recog.nn.NNAnalysis;
import ru.recog.repo.SegLogEntry;
import ru.recog.ui.FrameProcessor;

public class SegmentationLog {
	
	public static List<Integer> rect2List(List<Rect> rects) {
		List<Integer> list = new ArrayList<Integer>();
		for (Rect r : rects) {
			list.add(r.x);
			if (rects.indexOf(r) == rects.size()-1) list.add(r.x+r.width);
		}
		return list;
	}
	
	private static int exactSegmentation(List<Integer> proper, List<Integer> tested) {
		int pIndex = tested.indexOf(proper.get(0));
		if (pIndex < 0 || pIndex+proper.size() > tested.size()) return -1;
		for (int i = 1; i < proper.size(); i++)
			if (!proper.get(i).equals(tested.get(pIndex+i))) return -1;
		
		return pIndex;
	}
	
	public static boolean weakBorderEquals(List<Integer> proper, List<Integer> tested) {
		int pIndex = exactSegmentation(proper.subList(1, proper.size()-1), tested);
//		System.out.println("i="+pIndex+" prop "+proper+" total "+tested);
		if (pIndex < 1 || pIndex+proper.size()-1 > tested.size()) return false;
		int left = tested.get(pIndex-1); int properLeft = proper.get(0);
		int right = tested.get(pIndex+proper.size()-1-1); int properRight = proper.get(proper.size()-1);
		double AL = getAverageLength(proper);
		double ld = Math.abs(left-properLeft)/AL;
		double rd = Math.abs(right-properRight)/AL;

		
//		System.out.println("left "+left+" pL "+properLeft);
//		System.out.println("right "+right+" pL "+properRight);
//		System.out.println("AL "+AL+" LD "+ld+ " RD "+rd);
		return ld<=0.4 && rd<=0.4;
		

//		double AL = getAverageLength(proper);
		
//		return (double)Math.abs(left - properLeft)/AL <= 0.4 && (double)Math.abs(right - properRight)/ AL <= 0.4;
	}
	
	public static boolean weakBorderTest(SegmentationLogEntry sle, List<Integer> tested) {
		return weakBorderEquals(rect2List(sle.getRectangles()), tested);
	}
	
	public static boolean weakBorderTest(SegLogEntry sle, List<Integer> tested) {
		return weakBorderEquals(rect2List(sle.getRectangles()), tested);
	}
	
	public static boolean exactSegmentation(SegmentationLogEntry sle, List<Integer> tested) {
		return exactSegmentation(rect2List(sle.getRectangles()), tested) > -1;
	}
	
	public static boolean isValidSegmentation(List<Rect> symbolsList, List<Integer> cutPointsList){
		if ( isSegmentation(symbolsList, cutPointsList) == -1) return false;
		return true;	
		
	}
	
	public static double getAverageLength(List<Integer> cuts) {
		return ((double) (cuts.get(cuts.size()-1) - cuts.get(0)))/(cuts.size()-1);
	}
	
	
	public static int isSegmentation(List<Rect> symbolsList, List<Integer> cutPointsList){
	
		boolean isValid = true; 
		if (cutPointsList.size() < 6) {
			System.out.println("Not enough of cutPoints");
			isValid = false;
			return -1;
		}
						
		//find first point
		int xPoint = symbolsList.get(0).x;
		int firstPoint = -1;
		for( int i = (cutPointsList.size()-1);  i >=0; i-- ){
		   if ( (( cutPointsList.get(i) - 2 ) <= xPoint) && (( cutPointsList.get(i) + 2 ) >= xPoint) )
		   {
			   firstPoint = i;
			   break;
		   }
		}
		if (firstPoint == -1) 
		{
			isValid = false;
			return -1;
		}	
		
		// other points
		int otherPoints = firstPoint;
		for( int i = 1; i <= 5 ; i++){
			otherPoints++;
			if (otherPoints >= cutPointsList.size()) //FIXME make sure this is right
			   {
				   isValid = false;
				   break;
			   }	
			xPoint = symbolsList.get(i).x;
			if ( (( cutPointsList.get(otherPoints) - 2 ) >= xPoint) 
					            || (( cutPointsList.get(otherPoints) + 2 ) <= xPoint) )
			   {
				   isValid = false;
				   break;
			   }
		}
		
		// last point
		otherPoints++;
		if (otherPoints >= cutPointsList.size())
		   {
			   isValid = false;
			   return -1;
		   }	
		xPoint = symbolsList.get(5).x + symbolsList.get(5).width ;
		if ( (( cutPointsList.get(otherPoints) - 2 ) >= xPoint) 
				            || (( cutPointsList.get(otherPoints) + 6 ) <= xPoint) )
		   {
			   isValid = false;
			   return -1;
		   }
			
		if (!isValid) return -1;
			return firstPoint;
		
	}
	
	
	public static List<SegmentationLogEntry> readSegmentationLog(String seglogFilename) throws Exception {
		List<SegmentationLogEntry> list = new ArrayList<SegmentationLogEntry>();
		
		List<String> segLines = new ArrayList<String>();
		LineNumberReader lnr = new LineNumberReader(new FileReader(seglogFilename));
		for (String line; (line = lnr.readLine()) != null;) {
			list.add(new SegmentationLogEntry(line));
			
			if (!line.endsWith(FrameProcessor.RFAULT) && !line.endsWith(FrameProcessor.SFAULT)) {
				int firstindex = line.indexOf(";");
				segLines.add(line.substring(line.indexOf(";",firstindex+1)+1));
			}
		}
		lnr.close();
		return list;
	}

	public static List<Rect> string2rect(String seglogString) /*throws Exception*/ {
		List<Rect> rectangles = new ArrayList<Rect>();
		List<Integer> rects = NNAnalysis.parseToInteger(seglogString);
		if (rects.size() % 4 != 0) throw new IllegalArgumentException("amount of numbers should be dividable by 4");
		for (int i = 0; i < rects.size() / 4; i++) {
			int x = rects.get(i*4);
			int y = rects.get(i*4+1);
			int width  =rects.get(i*4+2);
			int height = rects.get(i*4+3);
			Rect r = new Rect(x, y, width, height);
			rectangles.add(r);
		}
		return rectangles;
		
	}
	
	@DataProvider(name = "testAll")
	public Object[][] getRoots() {
		return new Object[][] {
				{"/Users/pps/dev/test/frames", "/Users/pps/dev/seglog"}
		};
	}

	public static void testAll(String picRoot, String seglogRoot) throws Exception {
		
		System.out.println(picRoot);
		System.out.println(seglogRoot);
		testSegmenter(SegmentationFactory.getLegacySegmentation(), 
				Utils.fullPath(picRoot,"processed047"), Utils.fullPath(seglogRoot, "seglog047.txt"), false);
		testSegmenter(SegmentationFactory.getLegacySegmentation(), 
				Utils.fullPath(picRoot,"processed049"), Utils.fullPath(seglogRoot, "seglog049.txt"), false);
		testSegmenter(SegmentationFactory.getLegacySegmentation(), 
				Utils.fullPath(picRoot,"processed050"), Utils.fullPath(seglogRoot, "seglog050.txt"), false);
		
//		testSegmenter(SegmentationFactory.getMarkovSegmentation(), 
//				properPath(picRoot,"processed047"), properPath(seglogRoot, "seglog047.txt"));
//		testSegmenter(SegmentationFactory.getMarkovSegmentation(), 
//				properPath(picRoot,"processed049"), properPath(seglogRoot, "seglog049.txt"));
//		testSegmenter(SegmentationFactory.getMarkovSegmentation(), 
//				properPath(picRoot,"processed050"), properPath(seglogRoot, "seglog050.txt"));
//		
//		testMultipleCuts(SegmentationFactory.getMarkovSegmentation(), 
//				properPath(picRoot,"processed047"), properPath(seglogRoot, "seglog047.txt"));
//		testMultipleCuts(SegmentationFactory.getMarkovSegmentation(), 
//				properPath(picRoot,"processed049"), properPath(seglogRoot, "seglog049.txt"));
//		testMultipleCuts(SegmentationFactory.getMarkovSegmentation(), 
//				properPath(picRoot,"processed050"), properPath(seglogRoot, "seglog050.txt"));

		
	}
	
	public static void main(String[] args) throws Exception {
		testSegmenter(SegmentationFactory.getLegacySegmentation(), 
				Utils.fullPath("/Users/pps/dev/test/frames","processed051"), Utils.fullPath("/Users/pps/dev/seglog", "seglog051.txt"), true);
	}
	
	
	public static void testSegmenter(Segmentation segmenter, String picFolder, String seglogFilename, boolean showErrors) 
			throws Exception {
		LabelFrame lf = null;
		if (showErrors) {
			lf = new LabelFrame(picFolder);
			lf.pack();
			lf.setVisible(true);
		}
		File picDir = new File(picFolder);
		if (!picDir.exists() || !picDir.isDirectory())
			throw new IllegalArgumentException("Not a folder: "+picFolder);
		
		System.out.println("Checking "+seglogFilename+" with "+segmenter);
		
		int total = 0;
		int wrong = 0;
		for (SegmentationLogEntry entry : readSegmentationLog(seglogFilename)) {
			if (!entry.getResult().equals("SUCCESS")) continue;
			total++;
			
			String name = entry.getFilename().substring(entry.getFilename().lastIndexOf("\\")+1);

			Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name), 
					Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			SegmentationResult sr = segmenter.segment(m);
		
			boolean isValid = weakBorderTest(entry, sr.getCutPoints());
			if (!isValid) {
				if (showErrors) {
					lf.addImage(ImageUtils.drawSegLines(m, sr), "segmentation", 3);
					
					Mat c = ImageUtils.bin2color(m);
					for (Rect r : entry.getRectangles())
						Imgproc.rectangle(c, r.tl(), r.br(), new Scalar(0,255,0));
					lf.addImage(c, entry.toString(), 3);
				}
				wrong++;
			}
		}
		Assert.assertEquals(wrong, 0, "Expected 0 mistakes, but got "+wrong+" out of "+total);
		System.out.println("Total: "+total+" wrong: "+wrong);

	}
	
	public static void testMultipleCuts(Segmentation segmenter, String picFolder, String seglogFilename) 
			throws Exception {
		LabelFrame lf = new LabelFrame(picFolder);
		File picDir = new File(picFolder);
		if (!picDir.exists() || !picDir.isDirectory())
			throw new IllegalArgumentException("Not a folder: "+picFolder);
		
		final int NCuts = 3;
		
		System.out.println("Checking "+seglogFilename+" with "+segmenter);
		
		int total = 0;
		int wrong = 0;
		for (SegmentationLogEntry entry : readSegmentationLog(seglogFilename)) {
			if (!entry.getResult().equals("SUCCESS")) continue;
			total++;
			
			String name = entry.getFilename().substring(entry.getFilename().lastIndexOf("\\")+1);

			Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name), 
					Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			SegmentationResult sr = segmenter.segment(m);
			
			List<Boolean> b = new ArrayList<Boolean>();
			for (int i = 0; i < sr.getPossibleCuts().size() && i < NCuts; i++)
				b.add(weakBorderTest(entry, sr.getPossibleCuts().get(i).getCutPoints()));
				

			boolean valid =  b.contains(true);

		
//			boolean isValid = weakBorderTest(entry, sr.getCutPoints());
			if (!valid) {
//				System.out.println(entry);
//				System.out.println("INV: "+sr.getCutPoints());
//				System.out.println("R: "+rect2List(entry.getRectangles()));

				for (int i = 0; i < sr.getPossibleCuts().size(); i++)
					lf.addImage(ImageUtils.drawSegLines(m, sr.getPossibleCuts().get(i)), 
							String.valueOf(i), 3);
				
				
				Mat c = ImageUtils.bin2color(m);
				for (Rect r : entry.getRectangles())
					Imgproc.rectangle(c, r.tl(), r.br(), new Scalar(0,255,0));
				lf.addImage(c, entry.toString(), 3);
				
				wrong++;
			}
		}
		System.out.println("Total: "+total+" wrong: "+wrong);
		lf.pack();
		lf.setVisible(true);

	}
	
	public static boolean  testOneImage(Segmentation segmenter, List<Integer> properLines, Mat m) {
		SegmentationResult sr = segmenter.segment(m);
		System.out.println("After segm: "+sr.getCutPoints());
		LabelFrame lf = new LabelFrame("GG");
		Mat proper = m.clone();
		ImageUtils.drawLines(proper, properLines);
		lf.addImage(proper, "proper", 3);
		Mat seg = m.clone();
		ImageUtils.drawLines(seg, sr.getCutPoints());
		lf.addImage(seg, "seg", 3);
		lf.pack();
		lf.setVisible(true);
		return weakBorderEquals(properLines, sr.getCutPoints());
		
	}
	
	public static void testIsEqual(String picFolder, String seglogFilename) throws Exception {
		LabelFrame lf = new LabelFrame(picFolder);
		
		
		File picDir = new File(picFolder);
		if (!picDir.exists() || !picDir.isDirectory())
			throw new IllegalArgumentException("Not a folder: "+picFolder);
		List<SegmentationLogEntry> entries = readSegmentationLog(seglogFilename);
		int total = 0;
		int wrong = 0;
		for (SegmentationLogEntry entry : entries) {
			if (!entry.getResult().equals("SUCCESS")) continue;
			total++;
			
			String name = entry.getFilename().substring(entry.getFilename().lastIndexOf("\\")+1);

			Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name), 
					Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
			SegmentationResult sr = SegmentationFactory.getMarkovSegmentation().segment(m);
		
			List<Integer> cutPoints = new ArrayList<Integer>();

			cutPoints.add(0);
			cutPoints.addAll(sr.getCutPoints());
			boolean isValid = isValidSegmentation(entry.getRectangles(), cutPoints);
			if (!isValid) {
				
				for (CutData cut : sr.getPossibleCuts()) {
					Mat pm = m.clone();
					ImageUtils.drawLines(pm, cut);
					lf.addImage(pm, "equal? "+cut.isEqual(entry.getCuts())+" sle:"+entry.getCuts()+" cut:"+cut.getCutPoints(), 3);
				}

//				double contrast = -1;
//				double energy = -1;

//				if (!sr.getCutPoints().isEmpty()) {
//				Mat cutMat = m.submat(sr.getData().getUpperBound(), sr.getData().getLowerBound(),
//						sr.getCutPoints().get(0), sr.getCutPoints().get(sr.getCutPoints().size()-1));
//				 contrast = ImageUtils.contrastRMS(cutMat);
//				 energy = sr.getPossibleCuts().get(0).calcEnergy(sr.getData());
//				}
//				lf.addImage(ImageUtils.drawSegLines(m, sr), "c="+contrast+" e="+energy+" e/c="+energy/contrast, 3);
//				
//				Mat c = ImageUtils.bin2color(m);
//				for (Rect r : entry.getRectangles())
//					Imgproc.rectangle(c, r.tl(), r.br(), new Scalar(0,255,0));
//				lf.addImage(c, entry.toString(), 3);
				wrong++;
			}
		}
		System.out.println("Total: "+total+" wrong: "+wrong);
		lf.pack();
		lf.setVisible(true);
	}
	
}
