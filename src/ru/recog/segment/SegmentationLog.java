package ru.recog.segment;

import java.io.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.*;
import ru.recog.imgproc.Segmenter;
import ru.recog.imgproc.ShapeFilter;
import ru.recog.nn.NNAnalysis;
import ru.recog.segment.*;
import ru.recog.ui.FrameProcessor;

public class SegmentationLog {
	
	public static class SegmentationLogEntry {
		private String filename;
		private List<Rect> rectangles;
		private String result;
		
		public SegmentationLogEntry(String line) {
			int colonIndex = line.indexOf(";");
			filename = line.substring(0, colonIndex);
			if (line.endsWith(FrameProcessor.RFAULT) || line.endsWith(FrameProcessor.SFAULT)) {
				result = line.substring(colonIndex+1);
				rectangles = Collections.emptyList();
			} else {
				result = "SUCCESS";
				String rectString = line.substring(line.indexOf(";",colonIndex+1)+1);
				rectangles = string2rect(rectString);
			}
		}

		public String getFilename() {
			return filename;
		}

		public List<Rect> getRectangles() {
			return rectangles;
		}

		public String getResult() {
			return result;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("SLE(").append(filename).append(")[").append(getResult()).append("] -");
			sb.append(rectangles.toString());
			return sb.toString();
		}
		
		public String toSeglogString() {
			StringBuilder sb = new StringBuilder();
			sb.append(filename).append(";").append(getResult()).append(";");
			for (Rect r : rectangles)
				sb.append(r.x).append(";").append(r.y).append(";")
				.append(r.width).append(";").append(r.height).append(";");

			return sb.toString();
		}
		
		
	}
	
	
	public static boolean isValidSegmentation(List<Rect> symbolsList, List<Integer> cutPointsList){
		if ( isSegmentation(symbolsList, cutPointsList) == -1) return false;
		return true;	
		
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
	
	public static void testAll(String picRoot, String seglogRoot) throws Exception {
		
		testSegmenter(SegmentationFactory.getLegacySegmentation(), 
				properPath(picRoot,"processed047"), properPath(seglogRoot, "seglog047.txt") );
		testSegmenter(SegmentationFactory.getLegacySegmentation(), 
				properPath(picRoot,"processed049"), properPath(seglogRoot, "seglog049.txt"));
		testSegmenter(SegmentationFactory.getLegacySegmentation(), 
				properPath(picRoot,"processed050"), properPath(seglogRoot, "seglog050.txt"));
		
		testSegmenter(SegmentationFactory.getMarkovSegmentation(), 
				properPath(picRoot,"processed047"), properPath(seglogRoot, "seglog047.txt"));
		testSegmenter(SegmentationFactory.getMarkovSegmentation(), 
				properPath(picRoot,"processed049"), properPath(seglogRoot, "seglog049.txt"));
		testSegmenter(SegmentationFactory.getMarkovSegmentation(), 
				properPath(picRoot,"processed050"), properPath(seglogRoot, "seglog050.txt"));
	}
	
	
	public static void testSegmenter(Segmentation segmenter, String picFolder, String seglogFilename) 
			throws Exception {
		LabelFrame lf = new LabelFrame(picFolder);
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
		
			List<Integer> cutPoints = new ArrayList<Integer>();

			cutPoints.add(0);
			cutPoints.addAll(sr.getCutPoints());
			boolean isValid = isValidSegmentation(entry.getRectangles(), cutPoints);
			if (!isValid) {

				lf.addImage(ImageUtils.drawSegLines(m, sr), "segmentation", 3);
				
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
	
	public static void testShit(String picFolder, String seglogFilename) throws Exception {
		
		
		LabelFrame lf = new LabelFrame(picFolder);
		
		
		File picDir = new File(picFolder);
		if (!picDir.exists() || !picDir.isDirectory())
			throw new IllegalArgumentException("Not a folder: "+picFolder);
		List<SegmentationLogEntry> entries = readSegmentationLog(seglogFilename);
		int total = 0;
		int wrong = 0;
		LegacySegmentation ls = new LegacySegmentation();
		for (SegmentationLogEntry entry : entries) {
			if (!entry.getResult().equals("SUCCESS")) continue;
			total++;
			
//			System.out.println(entry);
			String name = entry.getFilename().substring(entry.getFilename().lastIndexOf("\\")+1);
//			System.out.println(name);

			Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name), 
					Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//			System.out.println(m.size());
//			SegmentationResult sr = Segmenter.shapesegment(m);
//			SegmentationResult sr = SBSegmenter.segment(m);
//			SegmentationResult sr = Segmenter.segment(m);
			SegmentationResult sr = MarkovSegmentation.multisegment(m);
//			SegmentationResult sr = ls.segment(m);
		
			List<Integer> cutPoints = new ArrayList<Integer>();

			cutPoints.add(0);
			cutPoints.addAll(sr.getCutPoints());
			boolean isValid = isValidSegmentation(entry.getRectangles(), cutPoints);
			if (!isValid) {
//				System.out.println("Problem with: "+name);
//				System.out.println(" cutPoints " + cutPoints);
				Mat b6 = ImageUtils.localbin(m, 0.6);
//				List<BinShape> shapes = ShapeBasedSegmenter.getFinalShapes(b6, ShapeFilter.WEAK);
				double lengthEstimate =  0.66 * (sr.getLowerBound() - sr.getUpperBound());
				ShapeFilter one = new ShapeFilter(ShapeFilter.WEAK);
				one.setWidthMin(3);
				one.setWidthMax((int)Math.round(lengthEstimate*1.2));
				
				ShapeFilter two = new ShapeFilter(ShapeFilter.WEAK);
				two.setWidthMin(one.getWidthMax()+1);
				two.setWidthMax((int)Math.round(lengthEstimate*2.4));
				
				ShapeFilter three = new ShapeFilter(ShapeFilter.WEAK);
				three.setWidthMin(two.getWidthMax()+1);
				three.setWidthMax((int)Math.round(lengthEstimate*3.3));

				
//				Mat c6 = ImageUtils.bin2color(b6.submat(sr.getUpperBound(), sr.getLowerBound()+1, 0, b6.cols()));
//				for (BinShape shape : sr.shapes) {
//					Scalar color = one.accept(shape)? new Scalar(0,255,0) :
//						two.accept(shape)? new Scalar(255,0,0) : 
//						three.accept(shape)? new Scalar(0,0,255) : new Scalar(125,125,125);
//					Imgproc.rectangle(c6, shape.getULPoint(), shape.getLRPoint(), color);
//				}
//				lf.addImage(c6, "bin 0.6", 3);
				
//				isValidSegmentation(entry.getRectangles(), cutPoints);
				lf.addImage(ImageUtils.drawSegLines(m, sr), "segmentation", 3);
				
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
	

	public static void main(String[] args) throws Exception {
		testAll(args[0], args[1]);
//	    testShit("/Users/pps/dev/test/frames/processed047", "/Users/pps/dev/seglog/seglog047.txt");
		
	}


    /**  
    does some tests */
	private static boolean test1Passed(){
		
		boolean temp = true;
		List<Rect> symbolsList = new ArrayList<Rect>();
		//Rect testRect = new Rect(9, 11, 11, 10); symbolsList.add(testRect)

		List<Integer> cutPointsList = new ArrayList<Integer>();
			
		return temp;
	}
	
	private static String properPath(String parent, String child) {
		return new File(parent, child).getAbsolutePath();
	}
	
}
