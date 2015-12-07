package ru.recog.segment;

import java.util.*;

import org.opencv.core.Rect;

import ru.recog.ui.FrameProcessor;

public class SegmentationLogEntry {
	private String filename;
	private List<Rect> rectangles;
	private String result;
	private String plate;
	
	public SegmentationLogEntry(String line) {
		int colonIndex = line.indexOf(";");
		filename = line.substring(0, colonIndex);
		if (line.endsWith(FrameProcessor.RFAULT) || line.endsWith(FrameProcessor.SFAULT)) {
			result = line.substring(colonIndex+1);
			rectangles = Collections.emptyList();
		} else {
			result = "SUCCESS";
			int nextColonIndex = line.indexOf(";",colonIndex+1);
			String rectString = line.substring(line.indexOf(";",colonIndex+1)+1);
			plate = line.substring(colonIndex+1, nextColonIndex);
			rectangles = SegmentationLog.string2rect(rectString);
		}
	}

	public String getFilename() {
		return filename;
	}

	public List<Rect> getRectangles() {
		return rectangles;
	}
	
	public List<Integer> getCuts() {
		List<Integer> cuts = new ArrayList<Integer>();
		for (Rect r : rectangles) cuts.add(r.x);
		if (rectangles.size() > 0) {
			Rect r = rectangles.get(rectangles.size()-1);
			cuts.add(r.x+r.width);
		}
		
		return cuts;
		
	}
	
	public String getPlate() {
		return plate;
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