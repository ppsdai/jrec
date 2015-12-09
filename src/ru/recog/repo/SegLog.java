package ru.recog.repo;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import ru.recog.XML;
import ru.recog.segment.SegmentationLog;
import ru.recog.segment.SegmentationLogEntry;

public class SegLog {

	private String videoId;
	public String getVideoId() {
		return videoId;
	}

	public String getPlatesLocation() {
		return platesLocation;
	}

	public List<SegLogEntry> getEntries() {
		return entries;
	}


	private String platesLocation;
	
	private List<SegLogEntry> entries = new ArrayList<SegLogEntry>();
	
	public SegLog(String videoId, String platesLocation) {
		this.videoId = videoId;
		this.platesLocation = platesLocation;
	}
	
	public void addEntry(SegLogEntry entry) {
		entries.add(entry);
	}
	
	
	public static void main(String[] args) throws Exception {
		
//		SegLog sl = new SegLog("video-047.avi", "047");
//		
//		List<SegmentationLogEntry> list = SegmentationLog.readSegmentationLog("/Users/pps/dev/seglog/seglog047.txt");
//		
//		for (int i = 0; i < list.size(); i++) {
//			SegmentationLogEntry sle = list.get(i);
////			ResultEnum re = "SUCCESS".equals(sle.getResult())? ResultEnum.SUCCESS : 
////				"RFAULT".equals(sle.getResult())? ResultEnum.RFAULT : ResultEnum.SFAULT;
////			sl.addEntry(new SegLogEntry(sle.getFilename(), sle.getRectangles(), re, sle.getPlate()));
//			sl.addEntry(SegLog.convertSLE(sle));
//		}
////		XML.toXML(sl, new FileWriter(new File("/Users/pps/dev/11seglog.xml")));
//		XML.toXML(sl, System.out);
		
		convertSeglog("seglog047.xml", "video-047.avi", "047", "/Users/pps/dev/seglog/seglog047.txt");
		convertSeglog("seglog049.xml", "video-049.avi", "049", "/Users/pps/dev/seglog/seglog049.txt");
		convertSeglog("seglog050.xml", "video-050.avi", "050", "/Users/pps/dev/seglog/seglog050.txt");
		convertSeglog("seglog051.xml", "video-051.avi", "051", "/Users/pps/dev/seglog/seglog051.txt");



	}
	
	private static void convertSeglog(String newName, String videoId, String platesLocation, String legacySeglogLocation) throws Exception {
		SegLog sl = new SegLog(videoId, platesLocation);
		
		for (SegmentationLogEntry sle : SegmentationLog.readSegmentationLog(legacySeglogLocation))
			sl.addEntry(convertSLE(sle));

		XML.toXML(sl, new FileWriter(new File(Repository.seglogFolderFile, newName)));
	}
	
	private static SegLogEntry convertSLE(SegmentationLogEntry sle) {
		ResultEnum re = "SUCCESS".equals(sle.getResult())? ResultEnum.SUCCESS : 
			"RFAULT".equals(sle.getResult())? ResultEnum.RFAULT : ResultEnum.SFAULT;
		String name = sle.getFilename().substring(sle.getFilename().lastIndexOf("\\")+1);
		return new SegLogEntry(name, sle.getRectangles(), re, sle.getPlate());
	}
	
}
