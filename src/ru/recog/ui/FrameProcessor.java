package ru.recog.ui;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import ru.recog.Utils;

public class FrameProcessor {
	
	File dir;
	File destination;
	File recDir, segDir, nnDir;
	
//	int[] charCounters = new int[Utils.FULL_CHARACTERS_SET.size()];
	
	PrintWriter logwriter; 
	
	int position = 0;
	List<String> files;
	List<String> processedFiles;
	
	
	public static final String RFAULT = "RFAULT";
	public static final String SFAULT = "SFAULT";
	public static final String LOGNAME = "seglog.txt";
	
	public FrameProcessor(String dirName, String dest) {
		this.dir = new File(dirName);
		
		if (!dir.exists() || !dir.isDirectory())
			throw new IllegalArgumentException(dirName+" is not a directory or doesn't exist");
		
		setupFolders(dest);
		
		files = new ArrayList<String>(Arrays.asList(dir.list(Utils.FILTER_BMP_PNG)));
		int total = files.size();
		if (processedFiles!=null)
			files.removeAll(processedFiles);
		
		Collections.sort(files, new Comparator<String>() {
			public int compare(String o1, String o2) {
				int i1 = Integer.valueOf(o1.substring(5, o1.indexOf(".")) );
				int i2 = Integer.valueOf(o2.substring(5, o2.indexOf(".")) );
				return Integer.compare(i1, i2);
			}
			
		});
		System.out.println("Total: "+total+" loaded files: "+files.size());
		
	}
	
	private void setupFolders(String dest) {
		destination = new File(dest);
		recDir = new File(destination, RFAULT);
		segDir = new File(destination, SFAULT);
		nnDir = new File(destination, "NN");
		boolean makeDirs = !destination.exists();
		
		if (makeDirs) {
			destination.mkdirs();
			recDir.mkdir();
			segDir.mkdir();
			nnDir.mkdir();
		} else {
			processedFiles = new ArrayList<String>();
			try {
				LineNumberReader lnr = new LineNumberReader(new FileReader(new File(dest, LOGNAME)));
				for (String line; (line = lnr.readLine()) != null;) {
					String fn = line.substring(0, line.indexOf(";"));
					processedFiles.add(fn.substring(fn.lastIndexOf(File.separator)+1, fn.length()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		for (char c : Utils.FULL_CHARACTERS_SET) {
				File cf = new File(nnDir, String.valueOf(c));
				if (makeDirs) cf.mkdir();
		}
		
//		Arrays.fill(charCounters, 0);

		try {
			logwriter = new PrintWriter(new FileWriter(new File(dest, LOGNAME), true), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	void saveSegmentedPanels(List<PlatePanel> panels, String number) {
		for (PlatePanel panel : panels)
			saveSegmentedPanel(panel, number);
	}
	
	void saveSegmentedPanel(PlatePanel panel, String number) {
		//
		//get segments 
		List<Mat> segments = panel.getSegmentationResult().getRevisedSegments();
		List<Rect> rectangles = panel.getSegmentationResult().getRevisedRectangles();
		//add to log file
		StringBuilder sb = new StringBuilder(panel.getFilename());
		sb.append(";").append(number).append(";");

		int charNum = 0;

		for (int i = panel.getFirstSegmentIndex(); i < panel.getFirstSegmentIndex()+6; 
				i++, charNum++) {
			Rect r = rectangles.get(i);
			sb.append(r.x).append(";").append(r.y).append(";")
			.append(r.width).append(";").append(r.height).append(";");
			
			saveChar(segments.get(i), panel.getFilename(), number.charAt(charNum));

		}
		
		addToLog(sb.toString());
		//add to segmented folder 
		// add 6 segments to nn training folder
		System.out.println(number+" Saving: "+panel.getFilename());

	}
	
	private void saveChar(Mat m, String origFilename, char c) {
//		int index = Utils.FULL_CHARACTERS_SET.indexOf(c);
//		String filename = String.valueOf(charCounters[index]).concat(".bmp");
		
		String fname = origFilename.substring(origFilename.lastIndexOf(File.separator)+1,
				origFilename.lastIndexOf("."));
		int counter = 1;
		String filename;
		do {
			filename = fname.concat("_").concat(String.valueOf(counter)).concat(".bmp");
			counter++;
		} while (new File(Utils.fullPath(new File(nnDir, String.valueOf(c)), 
				filename)).exists());
		
		
		Imgcodecs.imwrite(Utils.fullPath(new File(nnDir, String.valueOf(c)), filename), m);
//		charCounters[index]++;
		
	}
	
	void saveSegFault(PlatePanel panel) throws IOException {
		//add to log file
		addToLog(panel.getFilename().concat(";").concat(SFAULT));
		//save to seg fault folder
		copy(panel.getFilename(), segDir);
		System.out.println(SFAULT+": "+panel.getFilename());

	}
	
	void saveRecFault(PlatePanel panel) throws IOException {
		//add to log file
		addToLog(panel.getFilename().concat(";").concat(RFAULT));
		//save to recfault
		copy(panel.getFilename(), recDir);
		
		System.out.println(RFAULT+": "+panel.getFilename());
		
	}
	
	
	private void addToLog(String logstring) {
		logwriter.println(logstring);
	}
	
	private void copy(String filename, File dest) throws IOException {
		File f = new File(filename);
		Files.copy(f.toPath(), new File(dest, f.getName()).toPath(),
				StandardCopyOption.REPLACE_EXISTING);
	}
	
	public List<String> moreFrames(int number) {
		List<String> l = new ArrayList<String>();
		for (int i = position; i < position+number && i < files.size(); i++)
			l.add(new File(dir, files.get(i)).getAbsolutePath() );
		position = position+number;
		
		return l;
		
	}
}
