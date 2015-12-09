package ru.recog.repo;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Rect;

public class SegLogEntry {
	private String filename;
	private List<Rect> rectangles;
	private ResultEnum result;
	private String plate;
	
	public SegLogEntry(String filename, List<Rect> rectangles, ResultEnum result, String plate) {
		this.filename = filename;
		this.rectangles = rectangles;
		this.result = result;
		this.plate = plate;
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

	public ResultEnum getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SLE(").append(filename).append(")[").append(getResult()).append("] -");
		sb.append(rectangles.toString());
		return sb.toString();
	}
	
//	public String toSeglogString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append(filename).append(";").append(getResult()).append(";");
//		for (Rect r : rectangles)
//			sb.append(r.x).append(";").append(r.y).append(";")
//			.append(r.width).append(";").append(r.height).append(";");
//
//		return sb.toString();
//	}
	
	
}