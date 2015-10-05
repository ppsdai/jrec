package ru.recog.imgproc;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.*;

import org.opencv.core.Rect;

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
	
	public static void main(String[] args) throws Exception {
		List<SegmentationLogEntry> list = readSegmentationLog("/Users/pps/dev/seglog/seglog.txt");
		for (SegmentationLogEntry entry : list)
			System.out.println(entry);
	}

}
