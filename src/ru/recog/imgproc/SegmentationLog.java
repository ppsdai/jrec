package ru.recog.imgproc;

import java.io.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ru.recog.*;
import ru.recog.nn.NNAnalysis;
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
		
		
	}
	
//	public static enum Result {
//		FrameProcessor.RFAULT,
//		FrameProcessor.SFAULT
//	}
	
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
		for( int i = 0;  i < cutPointsList.size() ; i++ ){
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
			if (otherPoints > cutPointsList.size())
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
				            || (( cutPointsList.get(otherPoints) + 2 ) <= xPoint) )
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
			
			if (line.endsWith(FrameProcessor.RFAULT))
			
			
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
	
	public static void testAll() throws Exception {
//		testShit("/Users/pps/dev/test/frames/detect41", "/Users/pps/dev/seglog/seglog041.txt");
		testShit("/Users/pps/dev/test/frames/processed047", "/Users/pps/dev/seglog/seglog047.txt");
		testShit("/Users/pps/dev/test/frames/processed049", "/Users/pps/dev/seglog/seglog049.txt");
		testShit("/Users/pps/dev/test/frames/processed050", "/Users/pps/dev/seglog/seglog050.txt");
	}
	
	public static void testShit(String picFolder, String seglogFilename) throws Exception {
		LabelFrame lf = new LabelFrame("pidarasy!!!!!!!!!!!!!!!!!!!!!!!");
		
		
		File picDir = new File(picFolder);
		if (!picDir.exists() || !picDir.isDirectory())
			throw new IllegalArgumentException("Not a folder: "+picFolder);
		List<SegmentationLogEntry> entries = readSegmentationLog(seglogFilename);
		int total = 0;
		int wrong = 0;
		for (SegmentationLogEntry entry : entries) {
			if (!entry.getResult().equals("SUCCESS")) continue;
			total++;
			
//			System.out.println(entry);
			String name = entry.getFilename().substring(entry.getFilename().lastIndexOf("\\")+1);
//			System.out.println(name);

			Mat m = Imgcodecs.imread(Utils.fullPath(picDir, name), 
					Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//			System.out.println(m.size());
			SegmentationResult sr = Segmenter.segment(m);
			List<Integer> cutPoints = new ArrayList<Integer>();

			cutPoints.add(0);
			cutPoints.addAll(sr.getCutPoints());
			boolean isValid = isValidSegmentation(entry.getRectangles(), cutPoints);
			if (!isValid) {
				System.out.println("Problem with: "+name);
				lf.addImage(ImageUtils.drawSegLines(m, sr), "segmentation", 3);
				Mat c = ImageUtils.bin2color(m);
				for (Rect r : entry.getRectangles())
					Imgproc.rectangle(c, r.tl(), r.br(), new Scalar(0,255,0));
				lf.addImage(c, "SLE",3);
				wrong++;
			}
		}
		System.out.println("Total: "+total+" wrong: "+wrong);
		lf.pack();
		lf.setVisible(true);
		
		
		
	}
	
	public static void main(String[] args) throws Exception {
		testAll();
//		testShit("/Users/pps/dev/detect41", "/Users/pps/dev/seglog/seglog.txt");
//		List<SegmentationLogEntry> list = readSegmentationLog("C:\\dev\\frames\\segmented050\\seglog050.txt");
//		for (SegmentationLogEntry entry : list)
//			System.out.println(entry);
//		
//		LabelFrame lf = new LabelFrame("GOOD", true);
//		
//		for (SegmentationLogEntry entry : list)
//		{
//			if (entry.getResult() == "SUCCESS")
//			{
//				String filestr = entry.getFilename();
//				Mat m = Imgcodecs.imread(filestr, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//				Mat m1 = Imgcodecs.imread(filestr, Imgcodecs.CV_LOAD_IMAGE_COLOR);
//				Mat m2 = Imgcodecs.imread(filestr, Imgcodecs.CV_LOAD_IMAGE_COLOR);
//				
//				SegmentationResult result = Segmenter.segment(m);
//				
//				/*
//				SegmentationResult result = new SegmentationResult();
//				result = NewSegmenter.segment(m, result);
//				if (result == null) {
//					System.out.println("Some problem happened");
//					continue;
//				}
//				*/
//				
//				// Validate cut points
//				List<Rect> symbolsList = entry.getRectangles();
//				List<Integer> cutPointsList = result.getCutPoints();
//				
//
//				int temp = isSegmentation( symbolsList, cutPointsList);
//				//System.out.println("PROBLEMS " + temp); 
//				if (temp!= -1) {
//					for( int i = temp;  i <= ( temp + 6) ; i++ )
//					{
//						Imgproc.line(m2, new Point(cutPointsList.get(i), 0), 
//								new Point(cutPointsList.get(i), m2.rows()-1), new Scalar(0,0,255));	
//					}
//				}
//				else
//					System.out.println("Is Not VALID, FILE:  " + filestr);
//				
//				
//				
//				// End of cut points, PUT it into a method later
//				for (int p : result.getCutPoints())
//						Imgproc.line(m1, new Point(p, 0), new Point(p, m1.rows()-1), new Scalar(0,255,0));
//				
//				
//					
//				lf.addImage(m1, filestr, 3);
//				lf.addImage(m2, filestr, 3);
//			}
//					
//		}
//		
//		lf.pack();
//		lf.setVisible(true);*/
//		
//		
	}

}
